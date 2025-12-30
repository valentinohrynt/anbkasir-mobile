package com.anekabaru.anbkasir.ui

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.anekabaru.anbkasir.data.PosRepository
import com.anekabaru.anbkasir.data.ProductEntity
import com.anekabaru.anbkasir.data.TransactionEntity
import com.anekabaru.anbkasir.data.TransactionItemEntity
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
import javax.inject.Inject

// [MODIFIKASI] CartItem sekarang menyimpan selectedUnit
data class CartItem(
    val product: ProductEntity,
    val quantity: Int,
    val selectedUnit: String
) {
    // Harga aktif diambil dari map unitPrices berdasarkan unit yang dipilih
    // Jika tidak ada di map, fallback ke sellPrice utama
    val activePrice: Double get() = product.unitPrices[selectedUnit] ?: product.sellPrice
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

    // --- STATE DISKON ---
    var discountAmount by mutableStateOf(0.0)
        private set

    fun setDiscount(amount: Double) {
        discountAmount = amount
    }

    // Grand Total dikurangi diskon
    val grandTotal = _cart.combine(snapshotFlow { discountAmount }) { cartItems, discount ->
        val subtotal = cartItems.sumOf { it.total }
        (subtotal - discount).coerceAtLeast(0.0)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    private val _receiptText = MutableStateFlow<String?>(null)
    val receiptText = _receiptText.asStateFlow()

    // [MODIFIKASI] Add to Cart dengan logika Default Unit
    fun addToCart(p: ProductEntity) {
        val list = _cart.value.toMutableList()
        val idx = list.indexOfFirst { it.product.id == p.id }

        if (idx != -1) {
            // Jika produk sudah ada, tambah qty (unit tetap sama)
            list[idx] = list[idx].copy(quantity = list[idx].quantity + 1)
        } else {
            // Jika baru, ambil unit pertama sebagai default, atau "Pcs" jika kosong
            val defaultUnit = p.unitPrices.keys.firstOrNull() ?: "Pcs"
            list.add(CartItem(p, 1, defaultUnit))
        }
        _cart.value = list
    }

    // [BARU] Fungsi untuk mengubah satuan item di keranjang
    fun changeCartItemUnit(productId: String, newUnit: String) {
        val list = _cart.value.toMutableList()
        val idx = list.indexOfFirst { it.product.id == productId }
        if (idx != -1) {
            list[idx] = list[idx].copy(selectedUnit = newUnit)
            _cart.value = list
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
            } else {
                list[idx] = existing.copy(quantity = newQty)
            }
            _cart.value = list
        }
    }

    fun closeReceipt() { _receiptText.value = null }

    fun addProduct(
        name: String,
        buy: Double,
        stock: Int,
        cat: String,
        bar: String,
        unitPrices: Map<String, Double>,
        wholesalePrice: Double = 0.0,     // Parameter baru
        wholesaleThreshold: Int = 0       // Parameter baru
    ) {
        viewModelScope.launch {
            // Set sellPrice default ke harga unit pertama
            val defaultPrice = unitPrices.values.firstOrNull() ?: 0.0

            repository.saveProduct(
                ProductEntity(
                    name = name,
                    buyPrice = buy,
                    sellPrice = defaultPrice,
                    wholesalePrice = wholesalePrice, // Gunakan parameter
                    wholesaleThreshold = wholesaleThreshold, // Gunakan parameter
                    stock = stock,
                    category = cat,
                    barcode = bar,
                    unitPrices = unitPrices
                )
            )
        }
    }

    // [MODIFIKASI] Update Product menerima object entity utuh
    fun updateProduct(product: ProductEntity) {
        viewModelScope.launch {
            repository.updateProduct(product)
        }
    }

    fun deleteProduct(id: String) {
        viewModelScope.launch {
            repository.deleteProduct(id)
        }
    }

    fun sync() {
        viewModelScope.launch {
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

    fun getSalesTotal(start: Long, end: Long) = repository.getSalesTotal(start, end)
    fun getTxCount(start: Long, end: Long) = repository.getTxCount(start, end)

    // Helper untuk edit produk (mengambil data produk by ID secara sync/async sederhana)
    suspend fun getProductById(id: Int): ProductEntity? {
        // Karena di Repository getAllProducts berupa Flow, kita filter dari value terakhir products
        // Catatan: id di Entity adalah String UUID, tapi di UI mungkin ada yang kirim Int (perlu disesuaikan jika masih pakai Int di UI lama)
        // Di sini saya asumsikan flow 'products' sudah terisi.
        // Untuk amannya sebaiknya ambil by ID langsung dari DAO jika perlu, tapi filter list juga cukup untuk skala kecil.
        return products.value.find { it.id == id.toString() } // Sesuaikan jika ID integer
    }
    // Overload untuk String ID (rekomendasi)
    suspend fun getProductById(id: String): ProductEntity? {
        return products.value.find { it.id == id }
    }

    var selectedProduct by mutableStateOf<ProductEntity?>(null)
        private set
    fun selectProduct(product: ProductEntity?) { selectedProduct = product }

    var selectedTransaction by mutableStateOf<TransactionEntity?>(null)
        private set
    var selectedTransactionItems by mutableStateOf<List<TransactionItemEntity>>(emptyList())
        private set

    fun openTransactionDetail(tx: TransactionEntity) {
        selectedTransaction = tx
        viewModelScope.launch {
            selectedTransactionItems = repository.getTransactionItems(tx.id)
        }
    }

    // Payment Logic
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
        if (type != "CASH") {
            amountPaidInput = grandTotal.value.toInt().toString()
        } else {
            amountPaidInput = ""
        }
    }

    private fun generateReceipt(
        txId: String,
        date: Long,
        cashier: String,
        items: List<CartItem>,
        total: Double,
        payMethod: String,
        paid: Double,
        change: Double,
        discount: Double
    ): String {
        val sb = StringBuilder()
        val dateStr = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date(date))

        fun center(text: String): String {
            val padding = (32 - text.length) / 2
            return " ".repeat(padding.coerceAtLeast(0)) + text + "\n"
        }

        fun line() = "--------------------------------\n"

        fun row(label: String, value: Double): String {
            val vStr = "Rp${"%.0f".format(value)}"
            val sp = 32 - label.length - vStr.length
            return label + " ".repeat(sp.coerceAtLeast(0)) + vStr + "\n"
        }

        sb.append(center("TOKO ANEKA BARU"))
        sb.append(center("Kebaman - Srono"))
        sb.append(center("Telp: 0812-3456-7890"))
        sb.append(line())
        sb.append("No: ${txId.take(8)}\n")
        sb.append("Date: $dateStr\n")
        sb.append("Cashier: $cashier\n")
        sb.append(line())

        var subTotalCalc = 0.0
        items.forEach { item ->
            // activePrice sudah handle harga per unit
            val totalItem = item.total
            subTotalCalc += totalItem
            // Tampilkan Nama + Unit
            sb.append("${item.product.name} (${item.selectedUnit})\n")
            val qtyPrice = "${item.quantity} x ${"%.0f".format(item.activePrice)}"
            val subTotalStr = "%.0f".format(totalItem)
            val spaces = 32 - qtyPrice.length - subTotalStr.length
            sb.append(qtyPrice + " ".repeat(spaces.coerceAtLeast(0)) + subTotalStr + "\n")
        }

        sb.append(line())
        sb.append(row("SUBTOTAL", subTotalCalc))
        if (discount > 0) {
            sb.append(row("DISCOUNT", -discount))
        }
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
        val txId = java.util.UUID.randomUUID().toString()
        val now = System.currentTimeMillis()
        val currentDiscount = discountAmount

        viewModelScope.launch {
            repository.saveTransaction(
                total = total,
                cashier = currentUserName,
                items = currentItems,
                paymentMethod = paymentMethod,
                amountPaid = paid,
                changeAmount = changeAmount,
                discount = currentDiscount
            )

            // Generate receipt
            val receipt = generateReceipt(txId, now, currentUserName, currentItems, total, paymentMethod, paid, changeAmount, currentDiscount)
            _receiptText.value = receipt

            // Reset state
            _cart.value = emptyList()
            amountPaidInput = ""
            discountAmount = 0.0
            paymentMethod = "CASH"
        }
    }
}