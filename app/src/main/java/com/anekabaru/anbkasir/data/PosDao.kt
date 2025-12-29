package com.anekabaru.anbkasir.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface PosDao {
    // --- PRODUCTS ---
    @Query("SELECT * FROM products ORDER BY name ASC")
    fun getAllProducts(): Flow<List<ProductEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProduct(product: ProductEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProducts(products: List<ProductEntity>)

    // NEW: Update specific product fields
    @Query("UPDATE products SET name=:n, buyPrice=:b, sellPrice=:s, wholesalePrice=:w, wholesaleThreshold=:t, stock=:st, category=:c, barcode=:bar, updatedAt=:u, isSynced=0 WHERE id=:id")
    suspend fun updateProduct(id: String, n: String, b: Double, s: Double, w: Double, t: Int, st: Int, c: String, bar: String?, u: Long)

    // NEW: Delete product
    @Query("DELETE FROM products WHERE id = :id")
    suspend fun deleteProduct(id: String)

    @Query("SELECT * FROM products WHERE isSynced = 0")
    suspend fun getUnsyncedProducts(): List<ProductEntity>

    @Query("UPDATE products SET isSynced = 1 WHERE id IN (:ids)")
    suspend fun markProductsSynced(ids: List<String>)

    // --- TRANSACTIONS ---
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaction(transaction: TransactionEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransactionItems(items: List<TransactionItemEntity>)

    // NEW: Get Sales History
    @Query("SELECT * FROM transactions ORDER BY date DESC")
    fun getAllTransactions(): Flow<List<TransactionEntity>>

    @Query("SELECT * FROM transactions WHERE isSynced = 0")
    suspend fun getUnsyncedTransactions(): List<TransactionEntity>

    @Query("UPDATE transactions SET isSynced = 1 WHERE id IN (:ids)")
    suspend fun markTransactionsSynced(ids: List<String>)

    @Query("SELECT * FROM transaction_items WHERE transactionId = :txId")
    suspend fun getItemsForTransaction(txId: String): List<TransactionItemEntity>

    // --- REPORTING ---
    @Query("SELECT SUM(totalAmount) FROM transactions WHERE date BETWEEN :start AND :end")
    fun getSalesTotal(start: Long, end: Long): Flow<Double?>

    @Query("SELECT COUNT(*) FROM transactions WHERE date BETWEEN :start AND :end")
    fun getTxCount(start: Long, end: Long): Flow<Int>

    // --- SUPPLIERS ---
    @Query("SELECT * FROM suppliers")
    fun getSuppliers(): Flow<List<SupplierEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSupplier(s: SupplierEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSuppliers(s: List<SupplierEntity>)

    @Query("SELECT * FROM suppliers WHERE isSynced = 0")
    suspend fun getUnsyncedSuppliers(): List<SupplierEntity>

    @Query("UPDATE suppliers SET isSynced = 1 WHERE id IN (:ids)")
    suspend fun markSuppliersSynced(ids: List<String>)

    // --- PURCHASES ---
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPurchase(p: PurchaseEntity)

    @Query("SELECT * FROM purchases WHERE isSynced = 0")
    suspend fun getUnsyncedPurchases(): List<PurchaseEntity>

    @Query("UPDATE purchases SET isSynced = 1 WHERE id IN (:ids)")
    suspend fun markPurchasesSynced(ids: List<String>)
}