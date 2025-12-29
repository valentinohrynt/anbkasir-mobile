package com.anekabaru.anbkasir.data.remote

import com.anekabaru.anbkasir.data.*
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface ApiService {
    @GET("api/products") suspend fun getProducts(): List<ProductEntity>
    @POST("api/products/sync") suspend fun pushProducts(@Body products: List<ProductEntity>): SyncResponse

    @POST("api/transactions/sync") suspend fun pushTransactions(@Body txs: List<TransactionSyncPayload>): SyncResponse

    @GET("api/suppliers") suspend fun getSuppliers(): List<SupplierEntity>
    @POST("api/suppliers/sync") suspend fun pushSuppliers(@Body suppliers: List<SupplierEntity>): SyncResponse

    @POST("api/purchases/sync") suspend fun pushPurchases(@Body purchases: List<PurchaseSyncPayload>): SyncResponse
}