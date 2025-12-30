package com.anekabaru.anbkasir.ui.pos

import android.Manifest
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Store
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.anekabaru.anbkasir.data.ProductEntity
import com.anekabaru.anbkasir.ui.PosViewModel
import com.anekabaru.anbkasir.ui.components.BarcodeScanner
import com.anekabaru.anbkasir.ui.components.PullToRefreshLayout
import com.anekabaru.anbkasir.ui.theme.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PosScreen(
    viewModel: PosViewModel,
    onBack: () -> Unit,
    onViewCart: () -> Unit
) {
    val products by viewModel.products.collectAsState()
    val cart by viewModel.cart.collectAsState()
    val total by viewModel.grandTotal.collectAsState()
    val isSyncing by viewModel.isSyncing.collectAsState()

    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf<String?>(null) }

    // [BARU] State Scanner
    var showScanner by remember { mutableStateOf(false) }

    // [BARU] Snackbar State
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    fun showFeedback(message: String) {
        scope.launch {
            snackbarHostState.currentSnackbarData?.dismiss()
            snackbarHostState.showSnackbar(message, duration = SnackbarDuration.Short, withDismissAction = true)
        }
    }

    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) showScanner = true
        else showFeedback("Izin kamera diperlukan")
    }

    fun onBarcodeDetected(code: String) {
        val product = products.find { it.barcode == code }
        if (product != null) {
            viewModel.addToCart(product)
            showScanner = false
            showFeedback("✅ ${product.name} ditambahkan")
        } else {
            searchQuery = code
            showScanner = false
            showFeedback("⚠️ Produk tidak ditemukan")
        }
    }

    val categories = remember(products) {
        products.map { it.category }.distinct().sorted()
    }

    val filteredProducts = remember(products, searchQuery, selectedCategory) {
        products.filter { product ->
            val matchesSearch = if (searchQuery.isBlank()) true else {
                product.name.contains(searchQuery, ignoreCase = true) ||
                        product.category.contains(searchQuery, ignoreCase = true) ||
                        (product.barcode ?: "").contains(searchQuery)
            }
            val matchesCategory = selectedCategory == null || product.category == selectedCategory
            matchesSearch && matchesCategory
        }
    }

    val itemCount = cart.sumOf { it.quantity }

    Scaffold(
        // [BARU] Pasang Snackbar Host
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState) { data ->
                Snackbar(
                    snackbarData = data,
                    containerColor = BrandBlue,
                    contentColor = White,
                    shape = RoundedCornerShape(12.dp)
                )
            }
        },
        bottomBar = {
            if (itemCount > 0) {
                Surface(
                    shadowElevation = 16.dp,
                    color = White,
                    modifier = Modifier.fillMaxWidth().clickable { onViewCart() }
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                            .background(BrandGreen, RoundedCornerShape(12.dp))
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("$itemCount Items", color = White, style = MaterialTheme.typography.labelSmall)
                            Text("Total: Rp${"%.2f".format(total)}", color = White, style = MaterialTheme.typography.titleMedium)
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("View Cart", color = White, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.width(8.dp))
                            Icon(Icons.Default.ShoppingCart, null, tint = White)
                        }
                    }
                }
            }
        },
        containerColor = BackgroundApp
    ) { padding ->
        PullToRefreshLayout(
            isRefreshing = isSyncing,
            onRefresh = { viewModel.sync() },
            modifier = Modifier.padding(padding)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                Column(modifier = Modifier.fillMaxWidth().background(White)) {
                    Column(modifier = Modifier.padding(top = 20.dp, start = 20.dp, end = 20.dp, bottom = 12.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = TextPrimary, modifier = Modifier.size(20.dp)) }
                                Column {
                                    Text("Point of Sale", style = MaterialTheme.typography.titleLarge, color = TextPrimary)
                                    Text("${filteredProducts.size} Products", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                                }
                            }
                            Box(modifier = Modifier.size(36.dp).clip(CircleShape).background(SurfaceGreen), contentAlignment = Alignment.Center) {
                                Icon(Icons.Default.Store, null, tint = BrandGreen, modifier = Modifier.size(20.dp))
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            modifier = Modifier.fillMaxWidth(),
                            placeholder = { Text("Search or Scan...", style = MaterialTheme.typography.bodyMedium, color = TextTertiary) },
                            leadingIcon = { Icon(Icons.Default.Search, null, tint = TextTertiary, modifier = Modifier.size(20.dp)) },
                            // [BARU] Tombol Scanner
                            trailingIcon = {
                                IconButton(onClick = { cameraPermissionLauncher.launch(Manifest.permission.CAMERA) }) {
                                    Icon(Icons.Default.QrCodeScanner, "Scan", tint = BrandBlue)
                                }
                            },
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = BrandBlue,
                                unfocusedBorderColor = BorderColor,
                                focusedContainerColor = BackgroundApp,
                                unfocusedContainerColor = BackgroundApp
                            ),
                            singleLine = true
                        )
                    }

                    LazyRow(modifier = Modifier.fillMaxWidth(), contentPadding = PaddingValues(horizontal = 20.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        item { CategoryChipPOS(label = "All", isSelected = selectedCategory == null, onClick = { selectedCategory = null }) }
                        items(categories) { category -> CategoryChipPOS(label = category, isSelected = selectedCategory == category, onClick = { selectedCategory = category }) }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }

                Spacer(modifier = Modifier.height(8.dp))

                if (filteredProducts.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("No products found.", color = TextSecondary, style = MaterialTheme.typography.bodyMedium)
                    }
                } else {
                    LazyVerticalGrid(
                        columns = GridCells.Adaptive(minSize = 160.dp),
                        modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
                        contentPadding = PaddingValues(bottom = 100.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(filteredProducts) { product ->
                            val qtyInCart = cart.find { it.product.id == product.id }?.quantity ?: 0
                            ProductCardSimple(
                                product = product,
                                qty = qtyInCart,
                                onAdd = { viewModel.updateCartQuantity(product.id, 1) },
                                onRemove = { viewModel.updateCartQuantity(product.id, -1) },
                                onClickInitial = { viewModel.addToCart(product) }
                            )
                        }
                    }
                }
            }
        }
    }

    // [BARU] Dialog Scanner Fullscreen
    if (showScanner) {
        Dialog(onDismissRequest = { showScanner = false }, properties = DialogProperties(usePlatformDefaultWidth = false)) {
            Surface(modifier = Modifier.fillMaxSize()) {
                BarcodeScanner(
                    onCodeScanned = { code -> onBarcodeDetected(code) },
                    onClose = { showScanner = false }
                )
            }
        }
    }
}

// ... (CategoryChipPOS & ProductCardSimple tetap sama seperti kode Anda sebelumnya)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoryChipPOS(label: String, isSelected: Boolean, onClick: () -> Unit) {
    FilterChip(
        selected = isSelected,
        onClick = onClick,
        label = { Text(label, style = MaterialTheme.typography.labelMedium) },
        colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = BrandGreen,
            selectedLabelColor = White,
            containerColor = BackgroundApp,
            labelColor = TextSecondary
        ),
        border = FilterChipDefaults.filterChipBorder(enabled = true, selected = isSelected, borderColor = if (isSelected) BrandGreen else BorderColor),
        shape = RoundedCornerShape(20.dp)
    )
}

@Composable
fun ProductCardSimple(product: ProductEntity, qty: Int, onAdd: () -> Unit, onRemove: () -> Unit, onClickInitial: () -> Unit) {
    val isSelected = qty > 0
    Card(
        modifier = Modifier.fillMaxWidth().height(150.dp).clickable(enabled = !isSelected) { onClickInitial() },
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        colors = CardDefaults.cardColors(containerColor = White),
        shape = RoundedCornerShape(12.dp),
        border = if (isSelected) androidx.compose.foundation.BorderStroke(2.dp, BrandGreen) else null
    ) {
        Column(modifier = Modifier.fillMaxSize().padding(12.dp), verticalArrangement = Arrangement.SpaceBetween) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Surface(color = SurfaceBlue, shape = RoundedCornerShape(6.dp)) {
                    Text(product.category.uppercase(), modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp), style = MaterialTheme.typography.labelSmall, color = BrandBlue, fontSize = 10.sp)
                }
                Text(product.name, style = MaterialTheme.typography.titleSmall, maxLines = 2, overflow = TextOverflow.Ellipsis, color = TextPrimary)
            }
            if (isSelected) {
                Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                    Surface(modifier = Modifier.size(28.dp).clickable { onRemove() }, shape = RoundedCornerShape(8.dp), color = BackgroundApp) { Box(contentAlignment = Alignment.Center) { Icon(Icons.Default.Remove, null, tint = SystemRed, modifier = Modifier.size(16.dp)) } }
                    Text("$qty", style = MaterialTheme.typography.titleMedium, color = TextPrimary)
                    Surface(modifier = Modifier.size(28.dp).clickable { onAdd() }, shape = RoundedCornerShape(8.dp), color = BrandGreen) { Box(contentAlignment = Alignment.Center) { Icon(Icons.Default.Add, null, tint = White, modifier = Modifier.size(16.dp)) } }
                }
            } else {
                Text("Rp${product.sellPrice}", style = MaterialTheme.typography.titleMedium, color = BrandGreen)
            }
        }
    }
}