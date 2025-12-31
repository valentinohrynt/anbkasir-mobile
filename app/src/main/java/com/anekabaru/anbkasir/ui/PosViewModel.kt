package com.anekabaru.anbkasir.ui

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.anekabaru.anbkasir.data.PosRepository
import com.anekabaru.anbkasir.data.ProductEntity
import com.anekabaru.anbkasir.data.TransactionEntity
import com.anekabaru.anbkasir.data.TransactionItemEntity
import com.anekabaru.anbkasir.util.toRupiah
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID
import javax.inject.Inject
import kotlin.math.abs

data class CartItem(
    val product: ProductEntity,
    val quantity: Int,
    val selectedUnit: String,
    val activePrice: Double
) {
    val total: Double get() = activePrice * quantity
}

@HiltViewModel
class PosViewModel @Inject constructor(
    private val repository: PosRepository
) : ViewModel() {

    var currentUserRole by mutableStateOf("Cashier")
        private set
    var currentUserName by mutableStateOf("Staff")
        private set

    fun login(pin: String): Boolean {
        return when(pin) {
            "1234" -> {
                currentUserRole = "Cashier"
                currentUserName = "Kasir 1"
                true
            }
            "9999" -> {
                currentUserRole = "Owner"
                currentUserName = "Boss"
                true
            }
            else -> false
        }
    }

    fun logout() {
        currentUserRole = "Cashier"
        currentUserName = "Staff"
    }

    val products = repository.products.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val salesHistory = repository.salesHistory.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _cart = MutableStateFlow<List<CartItem>>(emptyList())
    val cart = _cart.asStateFlow()

    private val _isSyncing = MutableStateFlow(false)
    val isSyncing = _isSyncing.asStateFlow()

    var discountAmount by mutableStateOf(0.0)
        private set

    fun setDiscount(amount: Double) {
        discountAmount = amount
    }

    val grandTotal = _cart.combine(snapshotFlow { discountAmount }) { cartItems, discount ->
        val subtotal = cartItems.sumOf { it.total }
        (subtotal - discount).coerceAtLeast(0.0)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    private val _receiptText = MutableStateFlow<String?>(null)
    val receiptText = _receiptText.asStateFlow()

    private fun calculatePrice(product: ProductEntity, qty: Int, unit: String): Double {
        val normalUnitPrice = product.unitPrices[unit] ?: product.sellPrice
        val isBaseUnit = abs(normalUnitPrice - product.sellPrice) < 0.001

        val isWholesaleEligible = (
                qty >= product.wholesaleThreshold &&
                        product.wholesalePrice > 0 &&
                        isBaseUnit
                )

        return if (isWholesaleEligible) {
            if (product.wholesalePrice < normalUnitPrice) {
                product.wholesalePrice
            } else {
                normalUnitPrice
            }
        } else {
            normalUnitPrice
        }
    }

    fun addToCart(p: ProductEntity) {
        val list = _cart.value.toMutableList()
        val idx = list.indexOfFirst { it.product.id == p.id }

        if (idx != -1) {
            val existing = list[idx]
            val newQty = existing.quantity + 1
            if (newQty <= p.stock) {
                val newPrice = calculatePrice(p, newQty, existing.selectedUnit)
                list[idx] = existing.copy(quantity = newQty, activePrice = newPrice)
            }
        } else {
            val defaultUnit = p.unitPrices.entries.find { abs(it.value - p.sellPrice) < 0.001 }?.key
                ?: p.unitPrices.keys.firstOrNull()
                ?: "Pcs"
            val price = calculatePrice(p, 1, defaultUnit)
            list.add(CartItem(p, 1, defaultUnit, price))
        }
        _cart.value = list
    }

    fun changeCartItemUnit(productId: String, newUnit: String) {
        val list = _cart.value.toMutableList()
        val idx = list.indexOfFirst { it.product.id == productId }
        if (idx != -1) {
            val item = list[idx]
            if (item.product.unitPrices.containsKey(newUnit)) {
                val newPrice = calculatePrice(item.product, item.quantity, newUnit)
                list[idx] = item.copy(selectedUnit = newUnit, activePrice = newPrice)
                _cart.value = list
            }
        }
    }

    fun updateCartQuantity(productId: String, delta: Int) {
        val list = _cart.value.toMutableList()
        val idx = list.indexOfFirst { it.product.id == productId }
        if (idx != -1) {
            val existing = list[idx]
            val newQty = existing.quantity + delta

            if (newQty <= 0) {
                list.removeAt(idx)
            } else if (newQty <= existing.product.stock) {
                val newPrice = calculatePrice(existing.product, newQty, existing.selectedUnit)
                list[idx] = existing.copy(quantity = newQty, activePrice = newPrice)
            }
            _cart.value = list
        }
    }

    fun closeReceipt() { _receiptText.value = null }

    fun addProduct(name: String, buy: Double, stock: Int, cat: String, bar: String, unitPrices: Map<String, Double>, wholesalePrice: Double = 0.0, wholesaleThreshold: Int = 0) {
        viewModelScope.launch {
            val defaultPrice = unitPrices.values.firstOrNull() ?: 0.0
            repository.saveProduct(ProductEntity(name = name, buyPrice = buy, sellPrice = defaultPrice, wholesalePrice = wholesalePrice, wholesaleThreshold = wholesaleThreshold, stock = stock, category = cat, barcode = bar, unitPrices = unitPrices))
        }
    }

    fun updateProduct(product: ProductEntity) {
        viewModelScope.launch { repository.updateProduct(product) }
    }

    fun deleteProduct(id: String) {
        viewModelScope.launch {
            // Memastikan repository menangani deletedAt (Long?)
            repository.deleteProduct(id)
        }
    }
    fun getSalesTotal(start: Long, end: Long) = repository.getSalesTotal(start, end)
    fun getTxCount(start: Long, end: Long) = repository.getTxCount(start, end)

    suspend fun getProductById(id: String): ProductEntity? = products.value.find { it.id == id }

    var selectedProduct by mutableStateOf<ProductEntity?>(null)
        private set
    fun selectProduct(product: ProductEntity?) { selectedProduct = product }

    var selectedTransaction by mutableStateOf<TransactionEntity?>(null)
        private set
    var selectedTransactionItems by mutableStateOf<List<TransactionItemEntity>>(emptyList())
        private set

    fun openTransactionDetail(tx: TransactionEntity) {
        selectedTransaction = tx
        viewModelScope.launch { selectedTransactionItems = repository.getTransactionItems(tx.id) }
    }

    var paymentMethod by mutableStateOf("CASH")
    var amountPaidInput by mutableStateOf("")

    val changeAmount: Double
        get() {
            val paid = amountPaidInput.toDoubleOrNull() ?: 0.0
            val total = grandTotal.value
            return if (paid >= total) paid - total else 0.0
        }

    fun setPaymentType(type: String) {
        paymentMethod = type
        if (type != "CASH") amountPaidInput = grandTotal.value.toInt().toString()
        else amountPaidInput = ""
    }

    private fun generateReceipt(txId: String, timestamp: Long, cashier: String, items: List<CartItem>, total: Double, payMethod: String, paid: Double, change: Double, discount: Double): String {
        val sb = StringBuilder()
        val dateStr = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date(timestamp))
        fun center(text: String): String {
            val padding = (32 - text.length) / 2
            return " ".repeat(padding.coerceAtLeast(0)) + text + "\n"
        }
        fun line() = "--------------------------------\n"
        fun row(label: String, value: Double): String {
            val vStr = value.toRupiah()
            val sp = 32 - label.length - vStr.length
            return label + " ".repeat(sp.coerceAtLeast(0)) + vStr + "\n"
        }

        sb.append(center("TOKO ANEKA BARU"))
        sb.append(center("Kebaman - Srono"))
        sb.append(center("Telp: 0812-3456-7890"))
        sb.append(line())
        sb.append("No: ${txId.take(8).uppercase()}\n")
        sb.append("Date: $dateStr\n")
        sb.append("Cashier: $cashier\n")
        sb.append(line())

        var subTotalCalc = 0.0
        items.forEach { item ->
            val totalItem = item.total
            subTotalCalc += totalItem
            sb.append("${item.product.name} (${item.selectedUnit})\n")
            val qtyPrice = "${item.quantity} x ${item.activePrice.toRupiah()}"
            val subTotalStr = totalItem.toRupiah().replace("Rp", "")
            val spaces = 32 - qtyPrice.length - subTotalStr.length
            sb.append(qtyPrice + " ".repeat(spaces.coerceAtLeast(0)) + subTotalStr + "\n")
        }

        sb.append(line())
        sb.append(row("SUBTOTAL", subTotalCalc))
        if (discount > 0) sb.append(row("DISCOUNT", -discount))
        sb.append(row("GRAND TOTAL", total))
        sb.append(line())
        sb.append(row("PAYMENT ($payMethod)", paid))
        sb.append(row("CHANGE", change))
        sb.append(line())
        sb.append(center("Thank You!"))
        sb.append(center("Please Come Again"))
        sb.append("\n\n")
        return sb.toString()
    }

    fun checkout() {
        val paid = amountPaidInput.toDoubleOrNull() ?: 0.0
        val total = grandTotal.value
        if (paid < total && paymentMethod == "CASH") return
        val currentItems = _cart.value
        val txId = UUID.randomUUID().toString()
        val dateLong = System.currentTimeMillis()
        val currentDiscount = discountAmount

        viewModelScope.launch {
            val transaction = TransactionEntity(
                id = txId,
                totalAmount = total,
                date = dateLong,
                cashierName = currentUserName,
                paymentMethod = paymentMethod,
                amountPaid = paid,
                changeAmount = changeAmount,
                discount = currentDiscount,
                deletedAt = null // Memastikan data baru bukan data terhapus
            )

            val transactionItems = currentItems.map { item ->
                TransactionItemEntity(
                    id = UUID.randomUUID().toString(),
                    transactionId = txId,
                    productId = item.product.id,
                    productName = item.product.name,
                    quantity = item.quantity,
                    unit = item.selectedUnit,
                    priceSnapshot = item.activePrice,
                    subtotal = item.total
                )
            }

            val updatedProducts = currentItems.map { item ->
                item.product.copy(
                    stock = item.product.stock - item.quantity,
                    updatedAt = System.currentTimeMillis(),
                    isSynced = false
                )
            }

            repository.completeTransaction(transaction, transactionItems, updatedProducts)
            val receipt = generateReceipt(txId, dateLong, currentUserName, currentItems, total, paymentMethod, paid, changeAmount, currentDiscount)
            _receiptText.value = receipt

            // Reset state keranjang & input
            _cart.value = emptyList()
            amountPaidInput = ""
            discountAmount = 0.0
            paymentMethod = "CASH"

            // Trigger sinkronisasi otomatis
            repository.syncData()
        }
    }

    val topProducts = repository.getTopProducts().asLiveData(viewModelScope.coroutineContext)
    val totalProfit = repository.getTotalProfit().asLiveData(viewModelScope.coroutineContext)
    val salesTrend = repository.getDailySalesTrend().asLiveData(viewModelScope.coroutineContext)

    fun sync() {
        viewModelScope.launch {
            if (_isSyncing.value) return@launch
            _isSyncing.value = true
            try {
                repository.syncData()
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                _isSyncing.value = false
            }
        }
    }

    fun triggerAutoSync() {
        sync()
    }
}