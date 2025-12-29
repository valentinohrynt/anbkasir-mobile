package com.anekabaru.anbkasir.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface PosDao {
    // PRODUCTS
    @Query("SELECT * FROM products ORDER BY name ASC") fun getAllProducts(): Flow<List<ProductEntity>>
    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun insertProduct(p: ProductEntity)
    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun insertProducts(p: List<ProductEntity>)
    @Query("SELECT * FROM products WHERE isSynced = 0") suspend fun getUnsyncedProducts(): List<ProductEntity>
    @Query("UPDATE products SET isSynced = 1 WHERE id IN (:ids)") suspend fun markProductsSynced(ids: List<String>)

    // TRANSACTIONS
    @Insert suspend fun insertTransaction(t: TransactionEntity)
    @Insert suspend fun insertTransactionItems(items: List<TransactionItemEntity>)
    @Query("SELECT * FROM transactions WHERE isSynced = 0") suspend fun getUnsyncedTransactions(): List<TransactionEntity>
    @Query("SELECT * FROM transaction_items WHERE transactionId = :txId") suspend fun getItemsForTransaction(txId: String): List<TransactionItemEntity>
    @Query("UPDATE transactions SET isSynced = 1 WHERE id IN (:ids)") suspend fun markTransactionsSynced(ids: List<String>)

    // REPORTS
    @Query("SELECT SUM(totalAmount) FROM transactions WHERE date BETWEEN :start AND :end")
    fun getSalesTotal(start: Long, end: Long): Flow<Double?>

    @Query("SELECT COUNT(*) FROM transactions WHERE date BETWEEN :start AND :end")
    fun getTxCount(start: Long, end: Long): Flow<Int>

    // SUPPLIERS & PURCHASES
    @Query("SELECT * FROM suppliers") fun getSuppliers(): Flow<List<SupplierEntity>>
    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun insertSupplier(s: SupplierEntity)
    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun insertSuppliers(s: List<SupplierEntity>)
    @Insert suspend fun insertPurchase(p: PurchaseEntity)
    @Query("SELECT * FROM suppliers WHERE isSynced = 0") suspend fun getUnsyncedSuppliers(): List<SupplierEntity>
    @Query("UPDATE suppliers SET isSynced = 1 WHERE id IN (:ids)") suspend fun markSuppliersSynced(ids: List<String>)

    @Query("SELECT * FROM purchases WHERE isSynced = 0") suspend fun getUnsyncedPurchases(): List<PurchaseEntity>
    @Query("UPDATE purchases SET isSynced = 1 WHERE id IN (:ids)") suspend fun markPurchasesSynced(ids: List<String>)
}