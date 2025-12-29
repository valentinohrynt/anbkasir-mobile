package com.anekabaru.anbkasir.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

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
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val totalAmount: Double,
    val date: Long = System.currentTimeMillis(),
    val cashierName: String,
    val isSynced: Boolean = false
)

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