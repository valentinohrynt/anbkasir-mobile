package com.anekabaru.anbkasir.data

import android.util.Log
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
        CoroutineScope(Dispatchers.IO).launch {
            try {
                // Optional: api.deleteProduct(id)
            } catch (e: Exception) { Log.e(TAG, "Delete failed: ${e.message}") }
        }
    }

    suspend fun saveSupplier(s: SupplierEntity) {
        db.posDao().insertSupplier(s)
        safeSync()
    }

    suspend fun saveTransaction(tx: TransactionEntity, items: List<TransactionItemEntity>) {
        db.posDao().insertTransaction(tx)
        db.posDao().insertTransactionItems(items)
        safeSync()
    }

    private fun safeSync() {
        CoroutineScope(Dispatchers.IO).launch {
            try { syncData() } catch (e: Exception) { Log.e(TAG, "Sync failed: ${e.message}") }
        }
    }

    suspend fun syncData() {
        Log.d(TAG, "--- SYNC STARTED ---")
        try {
            // 1. PUSH
            val unsyncedProd = db.posDao().getUnsyncedProducts()
            if (unsyncedProd.isNotEmpty()) {
                val res = api.pushProducts(unsyncedProd)
                if (res.status == "success") db.posDao().markProductsSynced(res.syncedIds)
            }

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
                        discount = tx.discount, // [FIXED] Tambahkan parameter discount di sini
                        items = items.map {
                            TransactionItemSyncPayload(
                                it.id,
                                it.productId,
                                it.productName,
                                it.quantity,
                                it.unit,
                                it.priceSnapshot,
                                it.subtotal
                            )
                        }
                    )
                }
                val res = api.pushTransactions(payload)
                if (res.status == "success") db.posDao().markTransactionsSynced(res.syncedIds)
            }

            val unsyncedSup = db.posDao().getUnsyncedSuppliers()
            if (unsyncedSup.isNotEmpty()) {
                val res = api.pushSuppliers(unsyncedSup)
                if (res.status == "success") db.posDao().markSuppliersSynced(res.syncedIds)
            }

            // 2. PULL
            val serverProds = api.getProducts()
            if (serverProds.isNotEmpty()) {
                db.posDao().deleteSyncedProducts()

                db.posDao().insertProducts(serverProds.map {
                    it.copy(
                        isSynced = true,
                        unitPrices = it.unitPrices ?: emptyMap()
                    )
                })
            }

            val serverSups = api.getSuppliers()
            db.posDao().deleteSyncedSuppliers()
            if (serverSups.isNotEmpty()) {
                db.posDao().insertSuppliers(serverSups.map { it.copy(isSynced = true) })
            }

            val serverTransactions = api.getSalesHistory()
            db.posDao().deleteSyncedTransactionItems()
            db.posDao().deleteSyncedTransactions()
            if (serverTransactions.isNotEmpty()) {
                serverTransactions.forEach { payload ->
                    val safeTx = TransactionEntity(
                        id = payload.id,
                        totalAmount = payload.totalAmount,
                        cashierName = payload.cashierName ?: "Cashier",
                        date = payload.date,
                        paymentMethod = payload.paymentMethod ?: "CASH",
                        amountPaid = payload.amountPaid,
                        changeAmount = payload.changeAmount,
                        discount = payload.discount, // [FIXED] Gunakan discount dari payload server
                        isSynced = true
                    )
                    db.posDao().insertTransaction(safeTx)

                    if (payload.items.isNotEmpty()) {
                        val items = payload.items.map {
                            TransactionItemEntity(
                                id = it.id ?: UUID.randomUUID().toString(),
                                transactionId = payload.id,
                                productId = it.productId ?: "UNKNOWN",
                                productName = it.productName ?: "Unknown",
                                quantity = it.quantity,
                                unit = it.unit ?: "Pcs",
                                priceSnapshot = it.priceSnapshot,
                                subtotal = it.subtotal
                            )
                        }
                        db.posDao().insertTransactionItems(items)
                    }
                }
            }
            Log.d(TAG, "--- SYNC COMPLETED ---")
        } catch (e: Exception) {
            Log.e(TAG, "Sync Error: ${e.message}")
            e.printStackTrace()
        }
    }

    suspend fun getTransactionItems(txId: String) = db.posDao().getItemsForTransaction(txId)

    // [FIXED] Parameter sudah Long, tinggal teruskan ke DAO
    fun getSalesTotal(start: Long, end: Long): Flow<Double?> = db.posDao().getSalesTotal(start, end)
    fun getTxCount(start: Long, end: Long): Flow<Int> = db.posDao().getTxCount(start, end)
}