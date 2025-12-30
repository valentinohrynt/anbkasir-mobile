package com.anekabaru.anbkasir.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID
import com.google.gson.annotations.SerializedName

@Entity(tableName = "products")
data class ProductEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val name: String,
    val buyPrice: Double,
    val sellPrice: Double,
    val wholesalePrice: Double,
    val wholesaleThreshold: Int,
    val stock: Int,
    val category: String,
    val barcode: String?,
    val updatedAt: Long = System.currentTimeMillis(),
    val isSynced: Boolean = false
)

@Entity(tableName = "transactions")
data class TransactionEntity(
    @PrimaryKey
    @SerializedName("id")
    val id: String,

    @SerializedName("totalAmount", alternate = ["total_amount"])
    val totalAmount: Double,

    @SerializedName("cashierName", alternate = ["cashier_name", "user_name"])
    val cashierName: String,

    val date: Long,
    @SerializedName("paymentMethod", alternate = ["payment_method", "payment_type"])
    val paymentMethod: String = "CASH",

    @SerializedName("amountPaid", alternate = ["amount_paid", "paid_amount"])
    val amountPaid: Double = 0.0,

    @SerializedName("changeAmount", alternate = ["change_amount", "kembalian"])
    val changeAmount: Double = 0.0,

    val discount: Double = 0.0,

    val isSynced: Boolean = false
)

// ... (Sisa file tetap sama)

@Entity(tableName = "transaction_items")
data class TransactionItemEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val transactionId: String,
    val productId: String,
    val productName: String,
    val quantity: Int,
    val priceSnapshot: Double,
    val subtotal: Double
)

@Entity(tableName = "suppliers")
data class SupplierEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val name: String,
    val phone: String,
    val address: String,
    val isSynced: Boolean = false
)

@Entity(tableName = "purchases")
data class PurchaseEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val supplierId: String,
    val productId: String,
    val quantity: Int,
    val totalCost: Double,
    val date: Long = System.currentTimeMillis(),
    val isSynced: Boolean = false
)