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
    @Query("SELECT * FROM products WHERE isDeleted = 0 ORDER BY name ASC")
    fun getAllProducts(): Flow<List<ProductEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProduct(product: ProductEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProducts(products: List<ProductEntity>)

    @Update
    suspend fun updateProduct(product: ProductEntity)

    @Query("UPDATE products SET isDeleted = 1, isSynced = 0 WHERE id = :id")
    suspend fun deleteProductSoft(id: String)

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

    @Query("SELECT * FROM transactions WHERE isDeleted = 0 ORDER BY date DESC")
    fun getAllTransactions(): Flow<List<TransactionEntity>>

    @Query("SELECT * FROM transactions WHERE isSynced = 0")
    suspend fun getUnsyncedTransactions(): List<TransactionEntity>

    @Query("UPDATE transactions SET isSynced = 1 WHERE id IN (:ids)")
    suspend fun markTransactionsSynced(ids: List<String>)

    @Query("SELECT * FROM transaction_items WHERE transactionId = :txId")
    suspend fun getItemsForTransaction(txId: String): List<TransactionItemEntity>

    // [PERBAIKAN] Parameter start dan end sekarang Long agar match dengan Entity
    @Query("SELECT SUM(totalAmount) FROM transactions WHERE date BETWEEN :start AND :end AND isDeleted = 0")
    fun getSalesTotal(start: Long, end: Long): Flow<Double?>

    @Query("SELECT COUNT(*) FROM transactions WHERE date BETWEEN :start AND :end AND isDeleted = 0")
    fun getTxCount(start: Long, end: Long): Flow<Int>

    // --- SUPPLIERS ---
    @Query("SELECT * FROM suppliers WHERE isDeleted = 0")
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

    // --- CLEANUP ---
    @Query("DELETE FROM products WHERE isSynced = 1")
    suspend fun deleteSyncedProducts()

    @Query("DELETE FROM suppliers WHERE isSynced = 1")
    suspend fun deleteSyncedSuppliers()

    @Query("DELETE FROM transactions WHERE isSynced = 1")
    suspend fun deleteSyncedTransactions()

    @Query("DELETE FROM transaction_items WHERE transactionId IN (SELECT id FROM transactions WHERE isSynced = 1)")
    suspend fun deleteSyncedTransactionItems()
}