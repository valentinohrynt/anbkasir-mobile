package com.anekabaru.anbkasir.data

import android.util.Log
import androidx.room.withTransaction
import com.anekabaru.anbkasir.data.remote.ApiService
import com.anekabaru.anbkasir.data.remote.TransactionItemSyncPayload
import com.anekabaru.anbkasir.data.remote.TransactionSyncPayload
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.Flow
import java.util.UUID
import javax.inject.Inject

class PosRepository @Inject constructor(
    private val db: AppDatabase,
    private val api: ApiService
) {
    private val TAG = "PosRepository"

    val products = db.posDao().getAllProducts()
    val suppliers = db.posDao().getSuppliers()
    val salesHistory = db.posDao().getAllTransactions()

    suspend fun saveProduct(p: ProductEntity) {
        db.posDao().insertProduct(p)
        safeSync()
    }

    suspend fun updateProduct(p: ProductEntity) {
        db.posDao().updateProduct(p)
        safeSync()
    }

    suspend fun deleteProduct(id: String) {
        db.posDao().deleteProductSoft(id)
        safeSync()
    }

    suspend fun completeTransaction(
        tx: TransactionEntity,
        items: List<TransactionItemEntity>,
        products: List<ProductEntity>
    ) {
        db.withTransaction {
            db.posDao().insertTransaction(tx)
            db.posDao().insertTransactionItems(items)
            products.forEach { db.posDao().updateProduct(it) }
        }
    }

    private fun safeSync() {
        CoroutineScope(Dispatchers.IO).launch {
            try { syncData() } catch (e: Exception) { Log.e(TAG, "Sync failed: ${e.message}") }
        }
    }

    suspend fun syncData() {
        Log.d(TAG, "--- SYNC STARTED ---")

        try {
            val unsyncedProd = db.posDao().getUnsyncedProducts()
            if (unsyncedProd.isNotEmpty()) {
                val res = api.pushProducts(unsyncedProd)
                if (res.status == "success") db.posDao().markProductsSynced(res.syncedIds)
            }
        } catch (e: Exception) { Log.e(TAG, "Prod Push Error: ${e.message}") }

        try {
            val unsyncedTx = db.posDao().getUnsyncedTransactions()
            if (unsyncedTx.isNotEmpty()) {
                val payload = unsyncedTx.map { tx ->
                    val items = db.posDao().getItemsForTransaction(tx.id)
                    TransactionSyncPayload(
                        id = tx.id,
                        totalAmount = tx.totalAmount,
                        date = tx.date,
                        cashierName = tx.cashierName,
                        paymentMethod = tx.paymentMethod,
                        amountPaid = tx.amountPaid,
                        changeAmount = tx.changeAmount,
                        discount = tx.discount,
                        items = items.map {
                            TransactionItemSyncPayload(
                                it.id, it.productId, it.productName, it.quantity, it.unit, it.priceSnapshot, it.subtotal
                            )
                        }
                    )
                }
                val res = api.pushTransactions(payload)
                if (res.status == "success") db.posDao().markTransactionsSynced(res.syncedIds)
            }
        } catch (e: Exception) { Log.e(TAG, "Tx Push Error: ${e.message}") }

        try {
            val serverProds = api.getProducts()
            if (serverProds.isNotEmpty()) {
                db.posDao().deleteSyncedProducts()
                db.posDao().insertProducts(serverProds.map {
                    it.copy(isSynced = true, unitPrices = it.unitPrices ?: emptyMap())
                })
            }
        } catch (e: Exception) { Log.e(TAG, "Prod Pull Error: ${e.message}") }

        Log.d(TAG, "--- SYNC COMPLETED ---")
    }

    suspend fun getTransactionItems(txId: String) = db.posDao().getItemsForTransaction(txId)
    fun getSalesTotal(start: Long, end: Long): Flow<Double?> = db.posDao().getSalesTotal(start, end)
    fun getTxCount(start: Long, end: Long): Flow<Int> = db.posDao().getTxCount(start, end)
}