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
    // NEW: Expose Sales History from DB
    val salesHistory = db.posDao().getAllTransactions()

    // --- WRITE OPERATIONS ---

    suspend fun saveProduct(p: ProductEntity) {
        db.posDao().insertProduct(p)
        safeSync()
    }

    // NEW: Update Product
    suspend fun updateProduct(p: ProductEntity) {
        db.posDao().updateProduct(
            p.id, p.name, p.buyPrice, p.sellPrice,
            p.wholesalePrice, p.wholesaleThreshold, p.stock,
            p.category, p.barcode, System.currentTimeMillis()
        )
        safeSync()
    }

    // NEW: Delete Product
    suspend fun deleteProduct(id: String) {
        db.posDao().deleteProduct(id)
        // Try deleting from cloud immediately (fire & forget)
        CoroutineScope(Dispatchers.IO).launch {
            try { api.deleteProduct(id) } catch(e: Exception) { e.printStackTrace() }
        }
    }

    suspend fun saveSupplier(s: SupplierEntity) {
        db.posDao().insertSupplier(s)
        safeSync()
    }

    suspend fun recordPurchase(supplierId: String, productId: String, qty: Int, cost: Double) {
        val purchase = PurchaseEntity(
            supplierId = supplierId, productId = productId, quantity = qty, totalCost = cost, isSynced = false
        )
        db.posDao().insertPurchase(purchase)
        safeSync()
    }

    // MODIFIED: saveTransaction to include payment details
    suspend fun saveTransaction(
        total: Double,
        cashier: String,
        items: List<com.anekabaru.anbkasir.ui.CartItem>,
        paymentMethod: String,
        amountPaid: Double,
        changeAmount: Double
    ) {
        val txId = UUID.randomUUID().toString()
        val tx = TransactionEntity(
            id = txId,
            totalAmount = total,
            cashierName = cashier,
            date = System.currentTimeMillis(),
            paymentMethod = paymentMethod,
            amountPaid = amountPaid,
            changeAmount = changeAmount,
            isSynced = false
        )
        db.posDao().insertTransaction(tx)

        val txItems = items.map {
            TransactionItemEntity(
                transactionId = txId, productId = it.product.id, productName = it.product.name,
                quantity = it.quantity, priceSnapshot = it.activePrice, subtotal = it.total
            )
        }
        db.posDao().insertTransactionItems(txItems)

        // Reduce Local Stock Immediately
        items.forEach {
            val p = it.product
            val newStock = p.stock - it.quantity
            db.posDao().updateProduct(
                p.id, p.name, p.buyPrice, p.sellPrice,
                p.wholesalePrice, p.wholesaleThreshold, newStock,
                p.category, p.barcode, System.currentTimeMillis()
            )
        }

        safeSync()
    }

    // --- SYNC ENGINE ---

    private fun safeSync() {
        CoroutineScope(Dispatchers.IO).launch {
            syncData()
        }
    }

    suspend fun syncData() {
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
                        items = items.map {
                            TransactionItemSyncPayload(it.id, it.productId, it.productName, it.quantity, it.priceSnapshot, it.subtotal)
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

            val unsyncedPur = db.posDao().getUnsyncedPurchases()
            if (unsyncedPur.isNotEmpty()) {
                val payload = unsyncedPur.map { PurchaseSyncPayload(it.id, it.supplierId, it.productId, it.quantity, it.totalCost, it.date) }
                val res = api.pushPurchases(payload)
                if (res.status == "success") db.posDao().markPurchasesSynced(res.syncedIds)
            }

            // 2. PULL (Get updates from Cloud)
            val serverProds = api.getProducts()
            db.posDao().insertProducts(serverProds.map { it.copy(isSynced = true) })

            val serverSups = api.getSuppliers()
            db.posDao().insertSuppliers(serverSups.map { it.copy(isSynced = true) })

            val serverTransactions = api.getSalesHistory()
            if (serverTransactions.isNotEmpty()) {
                db.posDao().insertTransactions(serverTransactions.map { it.copy(isSynced = true) })
            }

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    suspend fun getTransactionItems(txId: String): List<TransactionItemEntity> {
        return db.posDao().getItemsForTransaction(txId)
    }
    fun getSalesTotal(start: Long, end: Long) = db.posDao().getSalesTotal(start, end)
    fun getTxCount(start: Long, end: Long) = db.posDao().getTxCount(start, end)
}