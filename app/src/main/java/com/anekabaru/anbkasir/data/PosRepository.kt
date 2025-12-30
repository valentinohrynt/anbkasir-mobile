package com.anekabaru.anbkasir.data

import android.util.Log
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
    private val TAG = "PosRepository"

    val products = db.posDao().getAllProducts()
    val suppliers = db.posDao().getSuppliers()
    val salesHistory = db.posDao().getAllTransactions()

    // --- WRITE OPERATIONS ---

    suspend fun saveProduct(p: ProductEntity) {
        db.posDao().insertProduct(p)
        safeSync()
    }

    suspend fun updateProduct(p: ProductEntity) {
        db.posDao().updateProduct(
            id = p.id,
            n = p.name,
            b = p.buyPrice,
            s = p.sellPrice,
            w = p.wholesalePrice,
            t = p.wholesaleThreshold,
            st = p.stock,
            c = p.category,
            bar = p.barcode,
            up = p.unitPrices,
            u = System.currentTimeMillis()
        )
        safeSync()
    }

    suspend fun deleteProduct(id: String) {
        db.posDao().deleteProduct(id)
        CoroutineScope(Dispatchers.IO).launch {
            try {
                api.deleteProduct(id)
                Log.d(TAG, "Product $id deleted from server")
            } catch (e: Exception) {
                Log.e(TAG, "Failed to delete product from server: ${e.message}")
            }
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

    suspend fun saveTransaction(
        total: Double,
        cashier: String,
        items: List<com.anekabaru.anbkasir.ui.CartItem>,
        paymentMethod: String,
        amountPaid: Double,
        changeAmount: Double,
        discount: Double
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
            discount = discount,
            isSynced = false
        )
        db.posDao().insertTransaction(tx)

        val txItems = items.map {
            TransactionItemEntity(
                transactionId = txId,
                productId = it.product.id,
                productName = it.product.name,
                quantity = it.quantity,
                unit = it.selectedUnit,
                priceSnapshot = it.activePrice,
                subtotal = it.total
            )
        }
        db.posDao().insertTransactionItems(txItems)

        items.forEach {
            val p = it.product
            val newStock = p.stock - it.quantity
            val updatedP = p.copy(stock = newStock, updatedAt = System.currentTimeMillis())
            updateProduct(updatedP)
        }

        safeSync()
    }

    private fun safeSync() {
        CoroutineScope(Dispatchers.IO).launch {
            syncData()
        }
    }

    suspend fun syncData() {
        Log.d(TAG, "--- SYNC STARTED ---")
        try {
            // ==========================================
            // 1. PUSH (UPLOAD DATA LOKAL KE SERVER)
            // ==========================================
            // Bagian ini tidak berubah (tetap upload data offline dulu)

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
                        tx.id, tx.totalAmount, tx.date, tx.cashierName, tx.paymentMethod,
                        tx.amountPaid, tx.changeAmount,
                        items.map { TransactionItemSyncPayload(it.id, it.productId, it.productName, it.quantity, it.unit, it.priceSnapshot, it.subtotal) }
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

            // ==========================================
            // 2. PULL (MIRROR SYNC - DELETE THEN INSERT)
            // ==========================================

            // --- A. PRODUCTS (MIRROR) ---
            Log.d(TAG, "Pulling Products...")
            val serverProds = api.getProducts()

            // 1. HAPUS SEMUA produk lokal yang sudah sync (tanpa syarat)
            db.posDao().deleteSyncedProducts()

            // 2. Masukkan yang baru (jika ada)
            if (serverProds.isNotEmpty()) {
                val safeProducts = serverProds.map { p ->
                    val safeUnitPrices = p.unitPrices ?: emptyMap()
                    p.copy(isSynced = true, unitPrices = safeUnitPrices)
                }
                db.posDao().insertProducts(safeProducts)
                Log.d(TAG, "Products mirrored: ${safeProducts.size} items.")
            } else {
                Log.d(TAG, "Products mirrored: Server empty, local cleared.")
            }


            // --- B. SUPPLIERS (MIRROR) ---
            Log.d(TAG, "Pulling Suppliers...")
            val serverSups = api.getSuppliers()

            // 1. HAPUS SEMUA supplier lokal yang sudah sync
            db.posDao().deleteSyncedSuppliers()

            // 2. Masukkan yang baru (jika ada)
            if (serverSups.isNotEmpty()) {
                db.posDao().insertSuppliers(serverSups.map { it.copy(isSynced = true) })
                Log.d(TAG, "Suppliers mirrored: ${serverSups.size} items.")
            }


            // --- C. SALES HISTORY (MIRROR) ---
            Log.d(TAG, "Pulling Sales History...")
            val serverTransactions = api.getSalesHistory()

            // 1. HAPUS SEMUA history lokal yang sudah sync
            db.posDao().deleteSyncedTransactionItems()
            db.posDao().deleteSyncedTransactions()

            // 2. Masukkan yang baru (jika ada)
            if (serverTransactions.isNotEmpty()) {
                serverTransactions.forEach { payload ->
                    // Sanitasi Data (Cegah Crash NullPointer)
                    val safeCashier = payload.cashierName ?: "Cashier"
                    val safePayment = payload.paymentMethod ?: "CASH"

                    val safeTx = TransactionEntity(
                        id = payload.id,
                        totalAmount = payload.totalAmount,
                        cashierName = safeCashier,
                        date = payload.date,
                        paymentMethod = safePayment,
                        amountPaid = payload.amountPaid,
                        changeAmount = payload.changeAmount,
                        discount = 0.0,
                        isSynced = true
                    )
                    db.posDao().insertTransaction(safeTx)

                    if (payload.items.isNotEmpty()) {
                        val itemEntities = payload.items.map { itemDto ->
                            val safeItemId = itemDto.id ?: UUID.randomUUID().toString()
                            val safeProductId = itemDto.productId ?: "UNKNOWN"
                            val safeProductName = itemDto.productName ?: "Unknown Product"
                            val safeUnit = itemDto.unit ?: "Pcs"

                            TransactionItemEntity(
                                id = safeItemId,
                                transactionId = payload.id,
                                productId = safeProductId,
                                productName = safeProductName,
                                quantity = itemDto.quantity,
                                unit = safeUnit,
                                priceSnapshot = itemDto.priceSnapshot,
                                subtotal = itemDto.subtotal
                            )
                        }
                        db.posDao().insertTransactionItems(itemEntities)
                    }
                }
                Log.d(TAG, "Sales History mirrored: ${serverTransactions.size} transactions.")
            } else {
                Log.d(TAG, "Sales History mirrored: Server empty, local cleared.")
            }

            Log.d(TAG, "--- SYNC COMPLETED SUCCESSFULLY ---")

        } catch (e: Exception) {
            Log.e(TAG, "SYNC ERROR: ${e.message}", e)
            e.printStackTrace()
        }
    }

    suspend fun getTransactionItems(txId: String) = db.posDao().getItemsForTransaction(txId)
    fun getSalesTotal(start: Long, end: Long) = db.posDao().getSalesTotal(start, end)
    fun getTxCount(start: Long, end: Long) = db.posDao().getTxCount(start, end)
}