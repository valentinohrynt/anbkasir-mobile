package com.anekabaru.anbkasir.ui

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.anekabaru.anbkasir.data.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class CartItem(
    val product: ProductEntity,
    val quantity: Int,
    val manualPrice: Double? = null
) {
    val activePrice: Double get() = manualPrice ?: if (quantity >= product.wholesaleThreshold) product.wholesalePrice else product.sellPrice
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

    val grandTotal = _cart.map { it.sumOf { item -> item.total } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    private val _receiptText = MutableStateFlow<String?>(null)
    val receiptText = _receiptText.asStateFlow()

    fun addToCart(p: ProductEntity) {
        val list = _cart.value.toMutableList()
        val idx = list.indexOfFirst { it.product.id == p.id }
        if (idx != -1) list[idx] = list[idx].copy(quantity = list[idx].quantity + 1)
        else list.add(CartItem(p, 1))
        _cart.value = list
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

    fun checkout() {
        viewModelScope.launch {
            repository.saveTransaction(grandTotal.value, currentUserName, _cart.value)
            _receiptText.value = "PAYMENT SUCCESS\nTotal: ${grandTotal.value}"
            _cart.value = emptyList()
        }
    }

    fun closeReceipt() { _receiptText.value = null }

    fun addProduct(name: String, buy: Double, sell: Double, wholesale: Double, thresh: Int, stock: Int, cat: String, bar: String) {
        viewModelScope.launch {
            repository.saveProduct(ProductEntity(name=name, buyPrice=buy, sellPrice=sell, wholesalePrice=wholesale, wholesaleThreshold=thresh, stock=stock, category=cat, barcode=bar))
        }
    }

    fun updateProduct(id: String, name: String, buy: Double, sell: Double, wholesale: Double, thresh: Int, stock: Int, cat: String, bar: String) {
        viewModelScope.launch {
            val p = ProductEntity(id=id, name=name, buyPrice=buy, sellPrice=sell, wholesalePrice=wholesale, wholesaleThreshold=thresh, stock=stock, category=cat, barcode=bar)
            repository.updateProduct(p)
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

    var selectedProduct by mutableStateOf<ProductEntity?>(null)
        private set

    fun selectProduct(product: ProductEntity?) {
        selectedProduct = product
    }

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
}