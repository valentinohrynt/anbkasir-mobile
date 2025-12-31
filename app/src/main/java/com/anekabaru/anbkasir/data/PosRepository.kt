package com.anekabaru.anbkasir.data

import android.util.Log
import androidx.room.withTransaction
import com.anekabaru.anbkasir.data.remote.ApiService
import com.anekabaru.anbkasir.data.remote.TransactionItemSyncPayload
import com.anekabaru.anbkasir.data.remote.TransactionSyncPayload
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import javax.inject.Inject

class PosRepository @Inject constructor(
    private val db: AppDatabase,
    private val api: ApiService
) {
    private val TAG = "PosRepository"

    val products = db.posDao().getAllProducts()
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
        db.posDao().deleteProductSoft(id, System.currentTimeMillis())
        safeSync()
    }

    suspend fun completeTransaction(tx: TransactionEntity, items: List<TransactionItemEntity>, updatedProducts: List<ProductEntity>) {
        db.withTransaction {
            db.posDao().insertTransaction(tx)
            db.posDao().insertTransactionItems(items)
            updatedProducts.forEach { db.posDao().updateProduct(it) }
        }
    }

    private fun safeSync() {
        CoroutineScope(Dispatchers.IO).launch {
            try { syncData() } catch (e: Exception) { Log.e(TAG, "Sync failed: ${e.message}") }
        }
    }

    suspend fun syncData() {
        // 1. PUSH PRODUCTS
        try {
            val unsyncedProd = db.posDao().getUnsyncedProducts()
            if (unsyncedProd.isNotEmpty()) {
                val res = api.pushProducts(unsyncedProd)
                if (res.status == "success") db.posDao().markProductsSynced(res.syncedIds)
            }
        } catch (e: Exception) { Log.e(TAG, "Prod push error: ${e.message}") }

        // 2. PUSH TRANSACTIONS
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
                        deletedAt = tx.deletedAt,
                        items = items.map {
                            TransactionItemSyncPayload(
                                id = it.id,
                                productId = it.productId,
                                productName = it.productName,
                                quantity = it.quantity,
                                unit = it.unit,
                                priceSnapshot = it.priceSnapshot,
                                subtotal = it.subtotal
                            )
                        }
                    )
                }
                val res = api.pushTransactions(payload)
                if (res.status == "success") db.posDao().markTransactionsSynced(res.syncedIds)
            }
        } catch (e: Exception) { Log.e(TAG, "Tx push error: ${e.message}") }

        // 3. PULL TRANSACTIONS
        try {
            val serverSales = api.getSalesHistory()
            if (serverSales.isNotEmpty()) {
                db.posDao().deleteSyncedDeletedTransactions()
                serverSales.forEach { payload ->
                    val transaction = TransactionEntity(
                        id = payload.id,
                        totalAmount = payload.totalAmount,
                        cashierName = payload.cashierName,
                        date = payload.date,
                        paymentMethod = payload.paymentMethod,
                        amountPaid = payload.amountPaid,
                        changeAmount = payload.changeAmount,
                        discount = payload.discount,
                        isSynced = true,
                        deletedAt = payload.deletedAt
                    )
                    val items = payload.items.map {
                        TransactionItemEntity(
                            id = it.id,
                            transactionId = payload.id,
                            productId = it.productId,
                            productName = it.productName,
                            quantity = it.quantity,
                            unit = it.unit,
                            priceSnapshot = it.priceSnapshot,
                            subtotal = it.subtotal
                        )
                    }
                    db.posDao().insertTransaction(transaction)
                    db.posDao().insertTransactionItems(items)
                }
            }
        } catch (e: Exception) { Log.e(TAG, "Pull Sales Error: ${e.message}") }

        // 4. PULL PRODUCTS
        try {
            val serverProds = api.getProducts()
            if (serverProds.isNotEmpty()) {
                db.posDao().deleteSyncedDeletedProducts()
                db.posDao().insertProducts(serverProds.map {
                    it.copy(isSynced = true)
                })
            }
        } catch (e: Exception) { Log.e(TAG, "Pull Prod error: ${e.message}") }
    }

    suspend fun getTransactionItems(txId: String) = db.posDao().getItemsForTransaction(txId)

    fun getSalesTotal(start: Long, end: Long): Flow<Double?> = db.posDao().getSalesTotal(start, end)

    fun getTxCount(start: Long, end: Long): Flow<Int> = db.posDao().getTxCount(start, end)

    // --- REVISI ANALYTICS ---

    fun getTopProducts(): Flow<List<TopProduct>> = db.posDao().getTopProducts()

    fun getTotalProfit(): Flow<Double?> = db.posDao().getTotalProfit()

    // Mengubah List<DailySales> dari DAO menjadi Map<String, Double> untuk UI
    fun getDailySalesTrend(): Flow<Map<String, Double>> {
        return db.posDao().getDailySalesTrend().map { list ->
            list.associate { it.day to it.dailyTotal }
        }
    }
}