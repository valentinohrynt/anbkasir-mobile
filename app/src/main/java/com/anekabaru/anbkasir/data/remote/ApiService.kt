package com.anekabaru.anbkasir.data.remote

import com.anekabaru.anbkasir.data.*
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.DELETE
import retrofit2.http.Path

interface ApiService {
    // Products
    @GET("api/products")
    suspend fun getProducts(): List<ProductEntity>

    @POST("api/products/sync")
    suspend fun pushProducts(@Body products: List<ProductEntity>): SyncResponse

    // NEW: Delete Product
    @DELETE("api/products/{id}")
    suspend fun deleteProduct(@Path("id") id: String): SyncResponse

    // Transactions
    @POST("api/transactions/sync")
    suspend fun pushTransactions(@Body transactions: List<TransactionSyncPayload>): SyncResponse

    // NEW: Get History (Optional if you rely on local DB, but good to have)
    @GET("api/transactions")
    suspend fun getSalesHistory(): List<TransactionEntity>

    // Suppliers
    @GET("api/suppliers")
    suspend fun getSuppliers(): List<SupplierEntity>

    @POST("api/suppliers/sync")
    suspend fun pushSuppliers(@Body suppliers: List<SupplierEntity>): SyncResponse

    // Purchases
    @POST("api/purchases/sync")
    suspend fun pushPurchases(@Body purchases: List<PurchaseSyncPayload>): SyncResponse
}