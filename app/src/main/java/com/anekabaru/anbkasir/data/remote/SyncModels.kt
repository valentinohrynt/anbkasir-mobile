package com.anekabaru.anbkasir.data.remote

import com.google.gson.annotations.SerializedName

data class SyncResponse(
    val status: String,
    @SerializedName("syncedIds")
    val syncedIds: List<String> = emptyList()
)

data class TransactionSyncPayload(
    val id: String,
    @SerializedName("total_amount")
    val totalAmount: Double,
    val date: Long,
    @SerializedName("cashier_name")
    val cashierName: String,
    @SerializedName("payment_method")
    val paymentMethod: String,
    @SerializedName("amount_paid")
    val amountPaid: Double,
    @SerializedName("change_amount")
    val changeAmount: Double,
    val discount: Double,
    // Tambahkan deleted_at agar server tahu jika transaksi dihapus
    @SerializedName("deleted_at")
    val deletedAt: Long? = null,
    val items: List<TransactionItemSyncPayload>
)

data class TransactionItemSyncPayload(
    val id: String,
    @SerializedName("product_id")
    val productId: String,
    @SerializedName("product_name")
    val productName: String,
    val quantity: Int,
    val unit: String,
    @SerializedName("price_snapshot")
    val priceSnapshot: Double,
    // Tambahkan @SerializedName jika backend menggunakan snake_case untuk subtotal
    @SerializedName("subtotal")
    val subtotal: Double
)

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