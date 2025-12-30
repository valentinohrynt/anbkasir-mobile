package com.anekabaru.anbkasir.data.remote

import com.anekabaru.anbkasir.data.ProductEntity
import com.anekabaru.anbkasir.data.SupplierEntity
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface ApiService {

    // --- PRODUCTS ---
    @GET("api/products")
    suspend fun getProducts(): List<ProductEntity>

    @POST("api/products/sync")
    suspend fun pushProducts(@Body products: List<ProductEntity>): SyncResponse

    @DELETE("api/products/{id}")
    suspend fun deleteProduct(@Path("id") id: String): SyncResponse

    // --- TRANSACTIONS ---
    @POST("api/transactions/sync")
    suspend fun pushTransactions(@Body transactions: List<TransactionSyncPayload>): SyncResponse

    // Pastikan return type-nya adalah List<TransactionSyncPayload>, BUKAN List<TransactionEntity>
    @GET("api/transactions")
    suspend fun getSalesHistory(): List<TransactionSyncPayload>

    // --- SUPPLIERS ---
    @GET("api/suppliers")
    suspend fun getSuppliers(): List<SupplierEntity>

    @POST("api/suppliers/sync")
    suspend fun pushSuppliers(@Body suppliers: List<SupplierEntity>): SyncResponse

    // --- PURCHASES ---
    @POST("api/purchases/sync")
    suspend fun pushPurchases(@Body purchases: List<PurchaseSyncPayload>): SyncResponse
}