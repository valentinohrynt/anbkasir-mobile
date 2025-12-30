package com.anekabaru.anbkasir.data.remote

import com.google.gson.annotations.SerializedName

// Response dari server saat PUSH data
data class SyncResponse(
    val status: String,

    @SerializedName("syncedIds") // Server mungkin kirim 'syncedIds' (sesuaikan dgn index.js)
    val syncedIds: List<String>
)

// Payload untuk Transaksi (Server <-> Android)
data class TransactionSyncPayload(
    @SerializedName("id")
    val id: String,

    @SerializedName("total_amount") // SERVER KIRIM SNAKE_CASE
    val totalAmount: Double,

    @SerializedName("date")
    val date: Long,

    @SerializedName("cashier_name") // SERVER KIRIM SNAKE_CASE
    val cashierName: String,

    @SerializedName("payment_method") // SERVER KIRIM SNAKE_CASE
    val paymentMethod: String,

    @SerializedName("amount_paid") // SERVER KIRIM SNAKE_CASE
    val amountPaid: Double,

    @SerializedName("change_amount") // SERVER KIRIM SNAKE_CASE
    val changeAmount: Double,

    @SerializedName("items")
    val items: List<TransactionItemSyncPayload>
)

// Payload untuk Item Transaksi
data class TransactionItemSyncPayload(
    @SerializedName("id")
    val id: String,

    @SerializedName("product_id") // SERVER KIRIM SNAKE_CASE
    val productId: String,

    @SerializedName("product_name") // SERVER KIRIM SNAKE_CASE
    val productName: String,

    val quantity: Int,

    val unit: String, // Pastikan ini ada

    @SerializedName("price_snapshot") // SERVER KIRIM SNAKE_CASE
    val priceSnapshot: Double,

    val subtotal: Double
)

// Payload untuk Purchase (Pembelian ke Supplier)
data class PurchaseSyncPayload(
    val id: String,

    @SerializedName("supplier_id")
    val supplierId: String,

    @SerializedName("product_id")
    val productId: String,

    val quantity: Int,

    @SerializedName("total_cost")
    val totalCost: Double,

    val date: Long
)