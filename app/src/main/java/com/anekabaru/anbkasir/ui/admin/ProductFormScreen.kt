package com.anekabaru.anbkasir.ui.admin

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.anekabaru.anbkasir.ui.PosViewModel
import com.anekabaru.anbkasir.ui.components.AppSnackbar
import com.anekabaru.anbkasir.ui.components.BarcodeScanner
import com.anekabaru.anbkasir.ui.components.SnackbarType
import com.anekabaru.anbkasir.ui.components.RupiahTextField
import com.anekabaru.anbkasir.ui.theme.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductFormScreen(
    viewModel: PosViewModel,
    onBack: () -> Unit
) {
    val productToEdit = viewModel.selectedProduct
    val isEditMode = productToEdit != null

    var name by remember { mutableStateOf(productToEdit?.name ?: "") }
    var category by remember { mutableStateOf(productToEdit?.category ?: "General") }
    var barcode by remember { mutableStateOf(productToEdit?.barcode ?: "") }
    var stock by remember { mutableStateOf(productToEdit?.stock?.toString() ?: "0") }
    var buyPrice by remember { mutableStateOf(productToEdit?.buyPrice?.toInt()?.toString() ?: "0") }
    var wholesalePrice by remember { mutableStateOf(productToEdit?.wholesalePrice?.toInt()?.toString() ?: "0") }
    var wholesaleThreshold by remember { mutableStateOf(productToEdit?.wholesaleThreshold?.toString() ?: "0") }

    var unitPriceList by remember {
        mutableStateOf(
            if (productToEdit != null && productToEdit.unitPrices.isNotEmpty()) {
                productToEdit.unitPrices.map { it.key to "%.0f".format(it.value) }
            } else {
                listOf("Pcs" to "0")
            }
        )
    }

    var showScanner by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    var snackbarType by remember { mutableStateOf(SnackbarType.INFO) }

    fun showFeedback(message: String, type: SnackbarType) {
        snackbarType = type
        scope.launch {
            snackbarHostState.currentSnackbarData?.dismiss()
            snackbarHostState.showSnackbar(message, duration = SnackbarDuration.Short, withDismissAction = true)
        }
    }

    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) showScanner = true
        else showFeedback("Izin kamera diperlukan", SnackbarType.ERROR)
    }

    Scaffold(
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState) { data ->
                AppSnackbar(snackbarData = data, type = snackbarType)
            }
        },
        topBar = {
            TopAppBar(
                title = { Text(if (isEditMode) "Edit Product" else "New Product", style = MaterialTheme.typography.titleLarge) },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = TextPrimary) } },
                actions = {
                    IconButton(onClick = {
                        if (name.isEmpty()) { showFeedback("Nama produk wajib diisi", SnackbarType.ERROR); return@IconButton }
                        val unitPricesMap = unitPriceList.associate { it.first to (it.second.toDoubleOrNull() ?: 0.0) }
                        val defaultSellPrice = unitPricesMap["Pcs"] ?: unitPricesMap.values.firstOrNull() ?: 0.0

                        if (isEditMode && productToEdit != null) {
                            viewModel.updateProduct(productToEdit.copy(name = name, category = category, barcode = barcode, stock = stock.toIntOrNull() ?: 0, buyPrice = buyPrice.toDoubleOrNull() ?: 0.0, sellPrice = defaultSellPrice, wholesalePrice = wholesalePrice.toDoubleOrNull() ?: 0.0, wholesaleThreshold = wholesaleThreshold.toIntOrNull() ?: 0, unitPrices = unitPricesMap, updatedAt = System.currentTimeMillis(), isSynced = false))
                        } else {
                            viewModel.addProduct(name = name, buy = buyPrice.toDoubleOrNull() ?: 0.0, stock = stock.toIntOrNull() ?: 0, cat = category, bar = barcode, unitPrices = unitPricesMap, wholesalePrice = wholesalePrice.toDoubleOrNull() ?: 0.0, wholesaleThreshold = wholesaleThreshold.toIntOrNull() ?: 0)
                        }
                        onBack()
                    }) { Icon(Icons.Default.Check, "Save", tint = BrandBlue) }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = White)
            )
        },
        containerColor = BackgroundApp
    ) { padding ->
        Column(modifier = Modifier.padding(padding).verticalScroll(rememberScrollState()).padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Card(colors = CardDefaults.cardColors(containerColor = White), elevation = CardDefaults.cardElevation(defaultElevation = 1.dp), shape = RoundedCornerShape(12.dp)) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Product Name") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        OutlinedTextField(value = category, onValueChange = { category = it }, label = { Text("Category") }, modifier = Modifier.weight(1f), shape = RoundedCornerShape(12.dp))
                        OutlinedTextField(value = barcode, onValueChange = { barcode = it }, label = { Text("Barcode") }, modifier = Modifier.weight(1f), shape = RoundedCornerShape(12.dp), trailingIcon = {
                            IconButton(onClick = { cameraPermissionLauncher.launch(Manifest.permission.CAMERA) }) { Icon(Icons.Default.QrCodeScanner, null, tint = BrandBlue) }
                        })
                    }
                }
            }

            Text("Stock & Cost", style = MaterialTheme.typography.titleSmall, color = TextSecondary)
            Card(colors = CardDefaults.cardColors(containerColor = White), elevation = CardDefaults.cardElevation(defaultElevation = 1.dp), shape = RoundedCornerShape(12.dp)) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        OutlinedTextField(value = stock, onValueChange = { if(it.all { c -> c.isDigit() }) stock = it }, label = { Text("Stock") }, modifier = Modifier.weight(1f), shape = RoundedCornerShape(12.dp), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
                        RupiahTextField(value = buyPrice, onValueChange = { buyPrice = it }, label = "Buy Price (Modal)", modifier = Modifier.weight(1f), shape = RoundedCornerShape(12.dp))
                    }
                }
            }

            Text("Wholesale / Grosir", style = MaterialTheme.typography.titleSmall, color = TextSecondary)
            Card(colors = CardDefaults.cardColors(containerColor = White), elevation = CardDefaults.cardElevation(defaultElevation = 1.dp), shape = RoundedCornerShape(12.dp)) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        RupiahTextField(value = wholesalePrice, onValueChange = { wholesalePrice = it }, label = "Wholesale Price", modifier = Modifier.weight(1f), shape = RoundedCornerShape(12.dp))
                        OutlinedTextField(value = wholesaleThreshold, onValueChange = { if(it.all { c -> c.isDigit() }) wholesaleThreshold = it }, label = { Text("Min Qty") }, modifier = Modifier.weight(0.6f), shape = RoundedCornerShape(12.dp), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
                    }
                }
            }

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text("Selling Units & Prices", style = MaterialTheme.typography.titleSmall, color = TextSecondary)
                TextButton(onClick = { unitPriceList = unitPriceList + ("" to "0") }) {
                    Icon(Icons.Default.Add, null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Add Unit")
                }
            }

            Card(colors = CardDefaults.cardColors(containerColor = White), elevation = CardDefaults.cardElevation(defaultElevation = 1.dp), shape = RoundedCornerShape(12.dp)) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    unitPriceList.forEachIndexed { index, (unitName, priceStr) ->
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(value = unitName, onValueChange = { new -> val n = unitPriceList.toMutableList(); n[index] = new to priceStr; unitPriceList = n }, label = { Text("Unit") }, modifier = Modifier.weight(0.8f), shape = RoundedCornerShape(12.dp))
                            RupiahTextField(value = priceStr, onValueChange = { new -> val n = unitPriceList.toMutableList(); n[index] = unitName to new; unitPriceList = n }, label = "Sell Price", modifier = Modifier.weight(1.2f), shape = RoundedCornerShape(12.dp))
                            if (unitPriceList.size > 1) {
                                IconButton(onClick = { val n = unitPriceList.toMutableList(); n.removeAt(index); unitPriceList = n }) { Icon(Icons.Default.Delete, null, tint = SystemRed) }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showScanner) {
        Dialog(onDismissRequest = { showScanner = false }, properties = DialogProperties(usePlatformDefaultWidth = false)) {
            Surface(modifier = Modifier.fillMaxSize()) {
                BarcodeScanner(onCodeScanned = { code -> barcode = code; showScanner = false }, onClose = { showScanner = false })
            }
        }
    }
}