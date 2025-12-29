package com.anekabaru.anbkasir.data

import com.anekabaru.anbkasir.data.remote.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

class PosRepository @Inject constructor(
    private val db: AppDatabase,
    private val api: ApiService
) {
    val products = db.posDao().getAllProducts()
    val suppliers = db.posDao().getSuppliers()

    // --- WRITE OPERATIONS (Modified for Auto-Sync) ---

    // 1. Save Product -> Trigger Sync
    suspend fun saveProduct(p: ProductEntity) {
        db.posDao().insertProduct(p)
        // Auto-Sync: Immediately try to push this to the cloud
        safeSync()
    }

    // 2. Save Supplier -> Trigger Sync
    suspend fun saveSupplier(s: SupplierEntity) {
        db.posDao().insertSupplier(s)
        safeSync()
    }

    // 3. Record Purchase (Stock In) -> Trigger Sync
    suspend fun recordPurchase(supplierId: String, productId: String, qty: Int, cost: Double) {
        val purchase = PurchaseEntity(
            supplierId = supplierId, productId = productId, quantity = qty, totalCost = cost, isSynced = false
        )
        db.posDao().insertPurchase(purchase)
        safeSync()
    }

    // 4. Save Transaction (Sale) -> Trigger Sync
    suspend fun saveTransaction(total: Double, cashier: String, items: List<com.anekabaru.anbkasir.ui.CartItem>) {
        val txId = UUID.randomUUID().toString()
        val tx = TransactionEntity(id = txId, totalAmount = total, cashierName = cashier, isSynced = false)
        db.posDao().insertTransaction(tx)

        val txItems = items.map {
            TransactionItemEntity(
                transactionId = txId, productId = it.product.id, productName = it.product.name,
                quantity = it.quantity, priceSnapshot = it.activePrice, subtotal = it.total
            )
        }
        db.posDao().insertTransactionItems(txItems)

        // Auto-Sync: Upload sale immediately so Web DB shows stock reduction
        safeSync()
    }

    // --- SYNC ENGINE ---

    // Wrapper to run sync without blocking the UI if network is slow
    private fun safeSync() {
        CoroutineScope(Dispatchers.IO).launch {
            syncData()
        }
    }

    suspend fun syncData() {
        try {
            // 1. PUSH PRODUCTS (New & Edited)
            val unsyncedProd = db.posDao().getUnsyncedProducts()
            if (unsyncedProd.isNotEmpty()) {
                val res = api.pushProducts(unsyncedProd)
                if (res.status == "success") db.posDao().markProductsSynced(res.syncedIds)
            }

            // 2. PUSH TRANSACTIONS (Sales)
            val unsyncedTx = db.posDao().getUnsyncedTransactions()
            if (unsyncedTx.isNotEmpty()) {
                val payload = unsyncedTx.map { tx ->
                    val items = db.posDao().getItemsForTransaction(tx.id)
                    TransactionSyncPayload(tx.id, tx.totalAmount, tx.date, items.map {
                        TransactionItemSyncPayload(it.id, it.productId, it.productName, it.quantity, it.priceSnapshot, it.subtotal)
                    })
                }
                val res = api.pushTransactions(payload)
                if (res.status == "success") db.posDao().markTransactionsSynced(res.syncedIds)
            }

            // 3. PUSH SUPPLIERS
            val unsyncedSup = db.posDao().getUnsyncedSuppliers()
            if (unsyncedSup.isNotEmpty()) {
                val res = api.pushSuppliers(unsyncedSup)
                if (res.status == "success") db.posDao().markSuppliersSynced(res.syncedIds)
            }

            // 4. PUSH PURCHASES
            val unsyncedPur = db.posDao().getUnsyncedPurchases()
            if (unsyncedPur.isNotEmpty()) {
                val payload = unsyncedPur.map { PurchaseSyncPayload(it.id, it.supplierId, it.productId, it.quantity, it.totalCost, it.date) }
                val res = api.pushPurchases(payload)
                if (res.status == "success") db.posDao().markPurchasesSynced(res.syncedIds)
            }

            // 5. PULL UPDATES (Get latest stock/products from other devices)
            val serverProds = api.getProducts()
            db.posDao().insertProducts(serverProds.map { it.copy(isSynced = true) })

            val serverSups = api.getSuppliers()
            db.posDao().insertSuppliers(serverSups.map { it.copy(isSynced = true) })

        } catch (e: Exception) {
            e.printStackTrace()
            // If offline, we just ignore the error.
            // The data is safe in Local DB and will sync next time!
        }
    }

    fun getSalesTotal(start: Long, end: Long) = db.posDao().getSalesTotal(start, end)
    fun getTxCount(start: Long, end: Long) = db.posDao().getTxCount(start, end)
}