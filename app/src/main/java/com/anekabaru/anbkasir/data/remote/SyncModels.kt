package com.anekabaru.anbkasir.data.remote

data class SyncResponse(
    val status: String,
    val syncedIds: List<String> = emptyList()
)

data class TransactionSyncPayload(
    val id: String,
    val totalAmount: Double,
    val date: Long,
    val cashierName: String,
    val paymentMethod: String,
    val amountPaid: Double,
    val changeAmount: Double,
    val items: List<TransactionItemSyncPayload>
)

data class TransactionItemSyncPayload(
    val id: String,
    val productId: String,
    val productName: String,
    val quantity: Int,
    val priceSnapshot: Double,
    val subtotal: Double
)

data class PurchaseSyncPayload(
    val id: String,
    val supplierId: String,
    val productId: String,
    val quantity: Int,
    val totalCost: Double,
    val date: Long
)