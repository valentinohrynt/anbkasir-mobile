package com.anekabaru.anbkasir.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import com.google.gson.reflect.TypeToken
import java.util.UUID

class Converters {
    private val gson = Gson()
    @TypeConverter
    fun fromStringMap(value: String?): Map<String, Double> {
        if (value.isNullOrEmpty()) return emptyMap()
        val type = object : TypeToken<Map<String, Double>>() {}.type
        return try { gson.fromJson(value, type) } catch (e: Exception) { emptyMap() }
    }
    @TypeConverter
    fun fromMap(map: Map<String, Double>?): String = gson.toJson(map ?: emptyMap<String, Double>())
}

@Entity(tableName = "products")
data class ProductEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val name: String,
    @SerializedName("buy_price") val buyPrice: Double,
    @SerializedName("sell_price") val sellPrice: Double,
    @SerializedName("wholesale_price") val wholesalePrice: Double,
    @SerializedName("wholesale_threshold") val wholesaleThreshold: Int,
    val stock: Int,
    val category: String,
    val barcode: String? = null,
    @SerializedName("unit_prices") val unitPrices: Map<String, Double> = emptyMap(),
    @SerializedName("updated_at") val updatedAt: Long = System.currentTimeMillis(),
    @SerializedName("is_synced") val isSynced: Boolean = false,
    @SerializedName("is_deleted") val isDeleted: Boolean = false
)

@Entity(tableName = "transactions")
data class TransactionEntity(
    @PrimaryKey val id: String,
    @SerializedName("total_amount") val totalAmount: Double,
    @SerializedName("cashier_name") val cashierName: String,

    // [PERBAIKAN] Ubah dari String ke Long agar match dengan SyncModel
    val date: Long,

    @SerializedName("payment_method") val paymentMethod: String = "CASH",
    @SerializedName("amount_paid") val amountPaid: Double = 0.0,
    @SerializedName("change_amount") val changeAmount: Double = 0.0,
    val discount: Double = 0.0,
    @SerializedName("is_synced") val isSynced: Boolean = false,
    @SerializedName("is_deleted") val isDeleted: Boolean = false
)

@Entity(tableName = "transaction_items")
data class TransactionItemEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    @SerializedName("transaction_id") val transactionId: String,
    @SerializedName("product_id") val productId: String,
    @SerializedName("product_name") val productName: String,
    val quantity: Int,
    val unit: String,
    @SerializedName("price_snapshot") val priceSnapshot: Double,
    val subtotal: Double
)

@Entity(tableName = "suppliers")
data class SupplierEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val name: String,
    val contact: String,
    val address: String,
    @SerializedName("updated_at") val updatedAt: Long = System.currentTimeMillis(),
    @SerializedName("is_synced") val isSynced: Boolean = false,
    @SerializedName("is_deleted") val isDeleted: Boolean = false
)

@Entity(tableName = "purchases")
data class PurchaseEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    @SerializedName("supplier_id") val supplierId: String,
    @SerializedName("product_id") val productId: String,
    val quantity: Int,
    @SerializedName("total_cost") val totalCost: Double,
    val date: Long = System.currentTimeMillis(),
    @SerializedName("is_synced") val isSynced: Boolean = false
)