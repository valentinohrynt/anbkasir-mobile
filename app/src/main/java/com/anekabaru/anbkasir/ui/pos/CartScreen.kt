package com.anekabaru.anbkasir.ui.pos

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Money
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.anekabaru.anbkasir.data.ProductEntity
import com.anekabaru.anbkasir.ui.CartItem
import com.anekabaru.anbkasir.ui.PosViewModel
import com.anekabaru.anbkasir.ui.theme.BackgroundApp
import com.anekabaru.anbkasir.ui.theme.BorderColor
import com.anekabaru.anbkasir.ui.theme.BrandBlue
import com.anekabaru.anbkasir.ui.theme.BrandGreen
import com.anekabaru.anbkasir.ui.theme.BrandOrange
import com.anekabaru.anbkasir.ui.theme.SystemRed
import com.anekabaru.anbkasir.ui.theme.TextPrimary
import com.anekabaru.anbkasir.ui.theme.TextSecondary
import com.anekabaru.anbkasir.ui.theme.TextTertiary
import com.anekabaru.anbkasir.ui.theme.White

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CartScreen(viewModel: PosViewModel, onBack: () -> Unit) {
    val cart by viewModel.cart.collectAsState()
    val total by viewModel.grandTotal.collectAsState()
    val receipt by viewModel.receiptText.collectAsState()

    val allProducts by viewModel.products.collectAsState()

    var discountInput by remember { mutableStateOf(if(viewModel.discountAmount > 0) viewModel.discountAmount.toInt().toString() else "") }
    var showAddProductDialog by remember { mutableStateOf(false) }

    val paymentMethod = viewModel.paymentMethod
    val amountPaid = viewModel.amountPaidInput
    val change = viewModel.changeAmount
    val isPaymentValid = (amountPaid.toDoubleOrNull() ?: 0.0) >= total

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Review Order", style = MaterialTheme.typography.titleLarge) },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = TextPrimary) } },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = White)
            )
        },
        containerColor = BackgroundApp
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
        ) {
            // --- LIST BARANG (TAMPILAN NOTA) ---
            Box(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                if (cart.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.ShoppingCart, null, tint = TextTertiary, modifier = Modifier.size(48.dp))
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("Keranjang Kosong", color = TextSecondary)
                            Spacer(modifier = Modifier.height(16.dp))
                            Button(onClick = { showAddProductDialog = true }, colors = ButtonDefaults.buttonColors(containerColor = BrandBlue)) {
                                Text("Tambah Produk")
                            }
                        }
                    }
                } else {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = White),
                        shape = RoundedCornerShape(12.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        Column {
                            // Tombol Tambah Produk (Atas)
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { showAddProductDialog = true }
                                    .background(BrandBlue.copy(alpha = 0.05f))
                                    .padding(12.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.AddCircle, null, tint = BrandBlue)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Tambah Item Lain", color = BrandBlue, fontWeight = FontWeight.Bold)
                                }
                            }

                            Divider(color = BrandBlue.copy(alpha = 0.1f))

                            // [UPDATE] Header Tabel dengan Kolom Satuan
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(BrandGreen.copy(alpha = 0.1f))
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Item", modifier = Modifier.weight(1.5f), style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = BrandGreen)
                                Text("Satuan", modifier = Modifier.weight(0.8f), style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = BrandGreen) // Kolom Baru
                                Text("Qty", modifier = Modifier.weight(1f), style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = BrandGreen, textAlign = TextAlign.Center)
                                Text("Total", modifier = Modifier.weight(1.2f), style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = BrandGreen, textAlign = TextAlign.End)
                            }

                            Divider(color = BrandGreen.copy(alpha = 0.2f))

                            LazyColumn(
                                modifier = Modifier.weight(1f),
                                contentPadding = PaddingValues(bottom = 12.dp)
                            ) {
                                items(cart) { item ->
                                    ReceiptItemRow(
                                        item = item,
                                        onPlus = { viewModel.updateCartQuantity(item.product.id, 1) },
                                        onMinus = { viewModel.updateCartQuantity(item.product.id, -1) },
                                        onUnitChange = { newUnit -> viewModel.changeCartItemUnit(item.product.id, newUnit) }
                                    )
                                    Divider(color = BorderColor, modifier = Modifier.padding(horizontal = 12.dp))
                                }
                            }
                        }
                    }
                }
            }

            // --- PEMBAYARAN ---
            Surface(
                shadowElevation = 20.dp,
                color = White,
                shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(24.dp).verticalScroll(rememberScrollState())) {

                    Text("Metode Pembayaran", style = MaterialTheme.typography.labelMedium, color = TextSecondary)
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        PaymentOptionButton("Cash", Icons.Default.Money, paymentMethod == "CASH", { viewModel.setPaymentType("CASH") }, Modifier.weight(1f))
                        PaymentOptionButton("QRIS", Icons.Default.QrCode, paymentMethod == "QRIS", { viewModel.setPaymentType("QRIS") }, Modifier.weight(1f))
                        PaymentOptionButton("Transfer", Icons.Default.AccountBalance, paymentMethod == "TRANSFER", { viewModel.setPaymentType("TRANSFER") }, Modifier.weight(1f))
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        OutlinedTextField(
                            value = discountInput,
                            onValueChange = {
                                discountInput = it
                                viewModel.setDiscount(it.toDoubleOrNull() ?: 0.0)
                            },
                            label = { Text("Diskon") },
                            prefix = { Text("Rp", style = MaterialTheme.typography.bodySmall) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            textStyle = MaterialTheme.typography.bodyMedium
                        )

                        OutlinedTextField(
                            value = amountPaid,
                            onValueChange = { viewModel.amountPaidInput = it },
                            label = { Text("Terima") },
                            prefix = { Text("Rp", style = MaterialTheme.typography.bodySmall) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            textStyle = MaterialTheme.typography.bodyMedium,
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = BrandGreen, focusedLabelColor = BrandGreen)
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Text("Kembali", style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
                        Text("Rp${"%.0f".format(change)}", style = MaterialTheme.typography.titleMedium, color = if(change >= 0) BrandGreen else SystemRed, fontWeight = FontWeight.Bold)
                    }

                    Divider(modifier = Modifier.padding(vertical = 12.dp), color = BorderColor)

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Column {
                            Text("Grand Total", style = MaterialTheme.typography.titleMedium, color = TextPrimary)
                            if (viewModel.discountAmount > 0) {
                                Text("Hemat Rp${"%.0f".format(viewModel.discountAmount)}", style = MaterialTheme.typography.labelSmall, color = BrandOrange)
                            }
                        }
                        Text("Rp${"%.0f".format(total)}", style = MaterialTheme.typography.headlineMedium, color = BrandGreen, fontWeight = FontWeight.Bold)
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = { viewModel.checkout() },
                        modifier = Modifier.fillMaxWidth().height(56.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = BrandGreen, disabledContainerColor = TextTertiary),
                        enabled = cart.isNotEmpty() && isPaymentValid
                    ) {
                        Text(if (isPaymentValid) "CONFIRM PAYMENT" else "UANG KURANG", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                    }
                }
            }
        }
    }






    if (showAddProductDialog) {
        ProductSelectionDialog(
            allProducts = allProducts,
            onDismiss = { showAddProductDialog = false },
            onProductSelected = { product ->
                viewModel.addToCart(product)
                showAddProductDialog = false
            }
        )
    }

    if (receipt != null) {
        val context = LocalContext.current
        AlertDialog(
            onDismissRequest = { viewModel.closeReceipt() },
            containerColor = White,
            title = { Text("Transaksi Sukses!", style = MaterialTheme.typography.titleLarge, color = BrandGreen) },
            text = {
                Surface(color = BackgroundApp, shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth().heightIn(max = 400.dp)) {
                    Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                        Text(receipt!!, fontFamily = FontFamily.Monospace, modifier = Modifier.padding(16.dp), color = TextPrimary, style = MaterialTheme.typography.bodySmall, fontSize = 12.sp)
                    }
                }
            },
            confirmButton = {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = {
                            val intent = android.content.Intent(android.content.Intent.ACTION_SEND).apply {
                                type = "text/plain"
                                putExtra(android.content.Intent.EXTRA_TEXT, receipt)
                            }
                            context.startActivity(android.content.Intent.createChooser(intent, "Print Receipt"))
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = BrandBlue)
                    ) {
                        Icon(Icons.Default.Print, null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Print")
                    }
                    Button(onClick = { viewModel.closeReceipt(); onBack() }, colors = ButtonDefaults.buttonColors(containerColor = BrandGreen)) { Text("Selesai") }
                }
            }
        )
    }
}

// --- KOMPONEN BARIS ITEM (PEMISAHAN KOLOM UNIT) ---
@Composable
fun ReceiptItemRow(
    item: CartItem,
    onPlus: () -> Unit,
    onMinus: () -> Unit,
    onUnitChange: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 12.dp), // Sedikit lebih renggang
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Kolom 1: Nama Produk (1.5f)
        Column(modifier = Modifier.weight(1.5f)) {
            Text(
                item.product.name,
                style = MaterialTheme.typography.bodyMedium,
                color = TextPrimary,
                fontWeight = FontWeight.SemiBold,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            // Harga per unit kecil di bawah nama
            Text("@ Rp${"%.0f".format(item.activePrice)}", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
        }

        // Kolom 2: Satuan (0.8f) - DEDICATED COLUMN
        Box(modifier = Modifier.weight(0.8f)) {
            Row(
                modifier = Modifier
                    .clickable { expanded = true }
                    .background(BrandBlue.copy(alpha = 0.1f), RoundedCornerShape(4.dp))
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    item.selectedUnit,
                    style = MaterialTheme.typography.labelSmall,
                    color = BrandBlue,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Icon(Icons.Default.ArrowDropDown, null, tint = BrandBlue, modifier = Modifier.size(14.dp))
            }

            DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }, containerColor = White) {
                item.product.unitPrices.keys.forEach { unit ->
                    DropdownMenuItem(
                        text = {
                            val price = item.product.unitPrices[unit] ?: 0.0
                            Text("$unit (Rp${"%.0f".format(price)})")
                        },
                        onClick = { onUnitChange(unit); expanded = false }
                    )
                }
            }
        }

        // Kolom 3: Quantity (1f)
        Row(modifier = Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
            Surface(
                modifier = Modifier.size(24.dp).clickable { onMinus() },
                shape = CircleShape, color = BackgroundApp, border = androidx.compose.foundation.BorderStroke(1.dp, BorderColor)
            ) { Icon(Icons.Default.Remove, null, tint = TextSecondary, modifier = Modifier.padding(4.dp)) }

            Text("${item.quantity}", modifier = Modifier.padding(horizontal = 6.dp), style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)

            Surface(
                modifier = Modifier.size(24.dp).clickable { onPlus() },
                shape = CircleShape, color = BrandGreen
            ) { Icon(Icons.Default.Add, null, tint = White, modifier = Modifier.padding(4.dp)) }
        }

        // Kolom 4: Total (1.2f)
        Text(
            "Rp${"%.0f".format(item.total)}",
            modifier = Modifier.weight(1.2f),
            style = MaterialTheme.typography.bodyMedium,
            color = TextPrimary,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.End
        )
    }
}

// --- KOMPONEN DIALOG PENCARIAN PRODUK (FIXED) ---
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductSelectionDialog(
    allProducts: List<ProductEntity>,
    onDismiss: () -> Unit,
    onProductSelected: (ProductEntity) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    val context = LocalContext.current // [BARU] Butuh context buat Toast

    val filteredProducts = remember(allProducts, searchQuery) {
        if (searchQuery.isBlank()) allProducts else {
            allProducts.filter {
                it.name.contains(searchQuery, ignoreCase = true) ||
                        (it.barcode ?: "").contains(searchQuery, ignoreCase = true)
            }
        }
    }

    Dialog(onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp),
            color = BackgroundApp
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Tambah Item", style = MaterialTheme.typography.titleLarge)
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, "Close")
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Cari nama atau barcode...") },
                    leadingIcon = { Icon(Icons.Default.Search, null) },
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = White,
                        unfocusedContainerColor = White
                    ),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(12.dp))

                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(filteredProducts) { product ->
                        // [LOGIC BARU] Cek Stok
                        val isOutOfStock = product.stock <= 0

                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    if (isOutOfStock) {
                                        // Tampilkan pesan error jika stok habis
                                        android.widget.Toast.makeText(context, "Stok ${product.name} habis!", android.widget.Toast.LENGTH_SHORT).show()
                                    } else {
                                        // Lanjut jika aman
                                        onProductSelected(product)
                                    }
                                },
                            colors = CardDefaults.cardColors(
                                // Warnanya jadi agak abu jika habis
                                containerColor = if (isOutOfStock) Color(0xFFF3F4F6) else White
                            ),
                            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        product.name,
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isOutOfStock) TextSecondary else TextPrimary // Text jadi abu jika habis
                                    )
                                    Text(
                                        "Stok: ${product.stock}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = if (isOutOfStock) SystemRed else BrandGreen
                                    )
                                }

                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = if (isOutOfStock) Color.LightGray else BrandBlue.copy(alpha = 0.1f)
                                ) {
                                    Text(
                                        "Rp${"%.0f".format(product.sellPrice)}",
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                        style = MaterialTheme.typography.labelSmall,
                                        color = if (isOutOfStock) Color.DarkGray else BrandBlue,
                                        fontWeight = FontWeight.Bold
                                    )
                                }

                                Spacer(modifier = Modifier.width(12.dp))

                                // Icon jadi abu jika habis
                                Icon(
                                    Icons.Default.AddCircle,
                                    null,
                                    tint = if (isOutOfStock) Color.LightGray else BrandGreen
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun PaymentOptionButton(
    label: String,
    icon: ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onClick,
        modifier = modifier.height(60.dp),
        shape = RoundedCornerShape(12.dp),
        color = if (isSelected) BrandGreen.copy(alpha = 0.1f) else White,
        border = androidx.compose.foundation.BorderStroke(
            width = if (isSelected) 2.dp else 1.dp,
            color = if (isSelected) BrandGreen else BorderColor
        )
    ) {
        Column(
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (isSelected) BrandGreen else TextSecondary,
                modifier = Modifier.size(20.dp)
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = if (isSelected) BrandGreen else TextSecondary,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
            )
        }
    }
}