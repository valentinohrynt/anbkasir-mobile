package com.anekabaru.anbkasir.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface PosDao {
    // --- PRODUCTS ---
    @Query("SELECT * FROM products WHERE deletedAt IS NULL ORDER BY name ASC")
    fun getAllProducts(): Flow<List<ProductEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProduct(product: ProductEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProducts(products: List<ProductEntity>)

    @Update
    suspend fun updateProduct(product: ProductEntity)

    @Query("UPDATE products SET deletedAt = :timestamp, isSynced = 0 WHERE id = :id")
    suspend fun deleteProductSoft(id: String, timestamp: Long = System.currentTimeMillis())

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

    @Query("SELECT * FROM transactions WHERE deletedAt IS NULL ORDER BY date DESC")
    fun getAllTransactions(): Flow<List<TransactionEntity>>

    @Query("SELECT * FROM transactions WHERE isSynced = 0")
    suspend fun getUnsyncedTransactions(): List<TransactionEntity>

    @Query("UPDATE transactions SET isSynced = 1 WHERE id IN (:ids)")
    suspend fun markTransactionsSynced(ids: List<String>)

    @Query("SELECT * FROM transaction_items WHERE transactionId = :txId")
    suspend fun getItemsForTransaction(txId: String): List<TransactionItemEntity>

    @Query("SELECT SUM(totalAmount) FROM transactions WHERE date BETWEEN :start AND :end AND deletedAt IS NULL")
    fun getSalesTotal(start: Long, end: Long): Flow<Double?>

    @Query("SELECT COUNT(*) FROM transactions WHERE date BETWEEN :start AND :end AND deletedAt IS NULL")
    fun getTxCount(start: Long, end: Long): Flow<Int>

    // --- SUPPLIERS ---
    @Query("SELECT * FROM suppliers WHERE deletedAt IS NULL")
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

    // --- CLEANUP (Gunakan isSynced dan deletedAt) ---
    @Query("DELETE FROM products WHERE isSynced = 1 AND deletedAt IS NOT NULL")
    suspend fun deleteSyncedDeletedProducts()

    @Query("DELETE FROM suppliers WHERE isSynced = 1 AND deletedAt IS NOT NULL")
    suspend fun deleteSyncedDeletedSuppliers()

    @Query("DELETE FROM transactions WHERE isSynced = 1 AND deletedAt IS NOT NULL")
    suspend fun deleteSyncedDeletedTransactions()
}