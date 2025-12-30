package com.anekabaru.anbkasir.ui.admin

import android.Manifest
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.outlined.Inventory2
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InventoryScreen(
    viewModel: PosViewModel,
    onBack: () -> Unit,
    onNavigateToDetail: () -> Unit,
    onNavigateToForm: () -> Unit
) {
    val context = LocalContext.current
    val products by viewModel.products.collectAsState()
    val isSyncing by viewModel.isSyncing.collectAsState()

    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf<String?>(null) }
    var showScanner by remember { mutableStateOf(false) }

    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) showScanner = true
        else Toast.makeText(context, "Izin kamera diperlukan", Toast.LENGTH_SHORT).show()
    }

    fun onBarcodeDetected(code: String) {
        searchQuery = code
        showScanner = false
        Toast.makeText(context, "Barcode Terdeteksi: $code", Toast.LENGTH_SHORT).show()
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

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    viewModel.selectProduct(null)
                    onNavigateToForm()
                },
                containerColor = BrandBlue, contentColor = White, shape = RoundedCornerShape(16.dp)
            ) { Icon(Icons.Default.Add, "Add Product") }
        },
        containerColor = BackgroundApp
    ) { padding ->
        // [PENTING] Menggunakan PullToRefreshLayout yang tadi Anda buat
        PullToRefreshLayout(
            isRefreshing = isSyncing,
            onRefresh = { viewModel.sync() },
            modifier = Modifier.padding(padding)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Header & Search
                Column(modifier = Modifier.fillMaxWidth().background(White)) {
                    Column(modifier = Modifier.padding(top = 20.dp, start = 20.dp, end = 20.dp, bottom = 12.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = TextPrimary, modifier = Modifier.size(20.dp)) }
                                Column {
                                    Text("Inventory", style = MaterialTheme.typography.titleLarge, color = TextPrimary)
                                    Text("${filteredProducts.size} Products", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                                }
                            }
                            Box(modifier = Modifier.size(36.dp).clip(CircleShape).background(SurfaceBlue), contentAlignment = Alignment.Center) {
                                // Jika Inventory2 error (butuh library extended), ganti Icons.Default.Inventory
                                Icon(Icons.Outlined.Inventory2, null, tint = BrandBlue, modifier = Modifier.size(20.dp))
                            }
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            modifier = Modifier.fillMaxWidth(),
                            placeholder = { Text("Search or Scan...", style = MaterialTheme.typography.bodyMedium, color = TextTertiary) },
                            leadingIcon = { Icon(Icons.Default.Search, null, tint = TextTertiary, modifier = Modifier.size(20.dp)) },
                            trailingIcon = {
                                IconButton(onClick = { cameraPermissionLauncher.launch(Manifest.permission.CAMERA) }) {
                                    Icon(Icons.Default.QrCodeScanner, "Scan", tint = BrandBlue)
                                }
                            },
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = BrandBlue, unfocusedBorderColor = BorderColor,
                                focusedContainerColor = BackgroundApp, unfocusedContainerColor = BackgroundApp
                            ),
                            singleLine = true
                        )
                    }
                    LazyRow(modifier = Modifier.fillMaxWidth(), contentPadding = PaddingValues(horizontal = 20.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        item { CategoryChip("All", selectedCategory == null) { selectedCategory = null } }
                        items(categories) { category -> CategoryChip(category, selectedCategory == category) { selectedCategory = category } }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }

                Spacer(modifier = Modifier.height(8.dp))

                if (filteredProducts.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("No products found", style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxWidth().fillMaxHeight().padding(horizontal = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        contentPadding = PaddingValues(bottom = 80.dp)
                    ) {
                        items(filteredProducts) { product ->
                            CompactProductCard(product, onClick = {
                                viewModel.selectProduct(product)
                                onNavigateToDetail()
                            })
                        }
                        item { Spacer(modifier = Modifier.height(8.dp)) }
                    }
                }
            }
        }
    }

    if (showScanner) {
        Dialog(onDismissRequest = { showScanner = false }, properties = DialogProperties(usePlatformDefaultWidth = false)) {
            Surface(modifier = Modifier.fillMaxSize()) {
                BarcodeScanner(onCodeScanned = { code -> onBarcodeDetected(code) }, onClose = { showScanner = false })
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoryChip(label: String, isSelected: Boolean, onClick: () -> Unit) {
    FilterChip(
        selected = isSelected, onClick = onClick,
        label = { Text(label, style = MaterialTheme.typography.labelMedium) },
        colors = FilterChipDefaults.filterChipColors(selectedContainerColor = BrandBlue, selectedLabelColor = White, containerColor = BackgroundApp, labelColor = TextSecondary),
        border = FilterChipDefaults.filterChipBorder(enabled = true, selected = isSelected, borderColor = if (isSelected) BrandBlue else BorderColor),
        shape = RoundedCornerShape(20.dp)
    )
}

@Composable
fun CompactProductCard(product: ProductEntity, onClick: () -> Unit) {
    val isLowStock = product.stock <= product.wholesaleThreshold

    // [IMPROVEMENT] Mencari nama unit dari harga default (sellPrice)
    // Jika tidak ketemu di map, default ke "Pcs" atau ambil key pertama
    val defaultUnit = product.unitPrices.entries.find { it.value == product.sellPrice }?.key
        ?: product.unitPrices.keys.firstOrNull()
        ?: "Pcs"

    Card(
        modifier = Modifier.fillMaxWidth().clickable { onClick() },
        colors = CardDefaults.cardColors(
            containerColor = if (isLowStock) Color(0xFFFFF0F0) else White
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        shape = RoundedCornerShape(12.dp),
        border = if (isLowStock) androidx.compose.foundation.BorderStroke(1.dp, SystemRed.copy(alpha = 0.5f)) else null
    ) {
        Row(
            modifier = Modifier.padding(14.dp).fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(product.name, style = MaterialTheme.typography.titleSmall, color = TextPrimary, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Spacer(modifier = Modifier.height(6.dp))
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Surface(color = SurfaceBlue, shape = RoundedCornerShape(6.dp)) {
                        Text(product.category, modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp), style = MaterialTheme.typography.labelSmall, color = BrandBlue)
                    }
                    // Tampilkan Harga + Satuan
                    Text(
                        "Rp${"%.0f".format(product.sellPrice)} / $defaultUnit",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        color = BrandGreen
                    )
                }
            }
            Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text("Stock", style = MaterialTheme.typography.labelSmall, color = TextTertiary)

                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (isLowStock) {
                        Icon(Icons.Default.Warning, contentDescription = "Low", tint = SystemRed, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                    }
                    Text("${product.stock}", style = MaterialTheme.typography.titleMedium, color = if(isLowStock) SystemRed else TextPrimary, fontWeight = FontWeight.Bold)
                }

                if (isLowStock) {
                    Text("Restock!", style = MaterialTheme.typography.labelSmall, color = SystemRed, fontSize = 10.sp)
                }
            }
        }
    }
}