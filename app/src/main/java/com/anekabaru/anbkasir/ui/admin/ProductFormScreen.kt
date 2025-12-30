package com.anekabaru.anbkasir.ui.admin

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
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
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
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
import com.anekabaru.anbkasir.ui.theme.BackgroundApp
import com.anekabaru.anbkasir.ui.theme.BorderColor
import com.anekabaru.anbkasir.ui.theme.BrandBlue
import com.anekabaru.anbkasir.ui.theme.SystemRed
import com.anekabaru.anbkasir.ui.theme.TextPrimary
import com.anekabaru.anbkasir.ui.theme.TextSecondary
import com.anekabaru.anbkasir.ui.theme.TextTertiary
import com.anekabaru.anbkasir.ui.theme.White
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductFormScreen(
    viewModel: PosViewModel,
    onBack: () -> Unit
) {
    LocalContext.current
    val productToEdit = viewModel.selectedProduct
    val isEditMode = productToEdit != null

    // State Dasar
    var name by remember { mutableStateOf(productToEdit?.name ?: "") }
    var category by remember { mutableStateOf(productToEdit?.category ?: "General") }
    var barcode by remember { mutableStateOf(productToEdit?.barcode ?: "") }
    var stock by remember { mutableStateOf(productToEdit?.stock?.toString() ?: "0") }
    var buyPrice by remember { mutableStateOf(productToEdit?.buyPrice?.toString() ?: "0") }

    // State Wholesale (Grosir)
    var wholesalePrice by remember { mutableStateOf(productToEdit?.wholesalePrice?.toInt()?.toString() ?: "0") }
    var wholesaleThreshold by remember { mutableStateOf(productToEdit?.wholesaleThreshold?.toString() ?: "0") }

    // State untuk Multi-Satuan
    var unitPriceList by remember {
        mutableStateOf(
            if (productToEdit != null && productToEdit.unitPrices.isNotEmpty()) {
                productToEdit.unitPrices.map { it.key to it.value.toInt().toString() }
            } else {
                listOf("Pcs" to "0")
            }
        )
    }

    var showScanner by remember { mutableStateOf(false) }

    // --- SNACKBAR SETUP ---
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

    @Composable
    fun CleanTextField(
        value: String,
        onValueChange: (String) -> Unit,
        label: String,
        modifier: Modifier = Modifier,
        isNumber: Boolean = false,
        trailingIcon: @Composable (() -> Unit)? = null
    ) {
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            label = { Text(label, style = MaterialTheme.typography.bodyMedium, color = TextTertiary) },
            modifier = modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = BrandBlue,
                focusedLabelColor = BrandBlue,
                unfocusedBorderColor = BorderColor,
                cursorColor = BrandBlue,
                focusedContainerColor = White,
                unfocusedContainerColor = White
            ),
            keyboardOptions = if (isNumber) KeyboardOptions(keyboardType = KeyboardType.Number) else KeyboardOptions.Default,
            singleLine = true,
            trailingIcon = trailingIcon
        )
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
                        if (name.isEmpty()) {
                            showFeedback("Nama produk wajib diisi", SnackbarType.ERROR)
                            return@IconButton
                        }
                        if (unitPriceList.any { it.first.isEmpty() }) {
                            showFeedback("Semua nama satuan wajib diisi", SnackbarType.ERROR)
                            return@IconButton
                        }

                        val unitPricesMap = unitPriceList.associate { it.first to (it.second.toDoubleOrNull() ?: 0.0) }
                        val defaultSellPrice = unitPricesMap.values.firstOrNull() ?: 0.0

                        if (isEditMode && productToEdit != null) {
                            val updatedProduct = productToEdit.copy(
                                name = name,
                                category = category,
                                barcode = barcode,
                                stock = stock.toIntOrNull() ?: 0,
                                buyPrice = buyPrice.toDoubleOrNull() ?: 0.0,
                                sellPrice = defaultSellPrice,
                                wholesalePrice = wholesalePrice.toDoubleOrNull() ?: 0.0,
                                wholesaleThreshold = wholesaleThreshold.toIntOrNull() ?: 0,
                                unitPrices = unitPricesMap,
                                updatedAt = System.currentTimeMillis()
                            )
                            viewModel.updateProduct(updatedProduct)
                        } else {
                            viewModel.addProduct(
                                name = name,
                                buy = buyPrice.toDoubleOrNull() ?: 0.0,
                                stock = stock.toIntOrNull() ?: 0,
                                cat = category,
                                bar = barcode,
                                unitPrices = unitPricesMap,
                                wholesalePrice = wholesalePrice.toDoubleOrNull() ?: 0.0,
                                wholesaleThreshold = wholesaleThreshold.toIntOrNull() ?: 0
                            )
                        }
                        onBack()
                    }) {
                        Icon(Icons.Default.Check, "Save", tint = BrandBlue)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = White)
            )
        },
        containerColor = BackgroundApp
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // --- CARD 1: INFO DASAR ---
            Card(
                colors = CardDefaults.cardColors(containerColor = White),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    CleanTextField(name, { name = it }, "Product Name")

                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        CleanTextField(category, { category = it }, "Category", Modifier.weight(1f))
                        CleanTextField(
                            value = barcode,
                            onValueChange = { barcode = it },
                            label = "Barcode",
                            modifier = Modifier.weight(1f),
                            trailingIcon = {
                                IconButton(onClick = { cameraPermissionLauncher.launch(Manifest.permission.CAMERA) }) {
                                    Icon(Icons.Default.QrCodeScanner, "Scan", tint = BrandBlue)
                                }
                            }
                        )
                    }
                }
            }

            Text("Stock & Cost", style = MaterialTheme.typography.titleSmall, color = TextSecondary)

            // --- CARD 2: STOK & MODAL ---
            Card(
                colors = CardDefaults.cardColors(containerColor = White),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        CleanTextField(stock, { if(it.all { c -> c.isDigit() }) stock = it }, "Initial Stock", Modifier.weight(1f), true)
                        CleanTextField(buyPrice, { if(it.all { c -> c.isDigit() }) buyPrice = it }, "Buy Price (Modal)", Modifier.weight(1f), true)
                    }
                }
            }

            // --- CARD 3: GROSIR (WHOLESALE) ---
            Card(
                colors = CardDefaults.cardColors(containerColor = White),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Text("Wholesale / Grosir (Optional)", style = MaterialTheme.typography.labelMedium, color = BrandBlue)
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        CleanTextField(wholesalePrice, { if(it.all { c -> c.isDigit() }) wholesalePrice = it }, "Wholesale Price", Modifier.weight(1f), true)
                        CleanTextField(wholesaleThreshold, { if(it.all { c -> c.isDigit() }) wholesaleThreshold = it }, "Min Qty", Modifier.weight(0.6f), true)
                    }
                }
            }

            // --- HEADER MULTI SATUAN ---
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Selling Units & Prices", style = MaterialTheme.typography.titleSmall, color = TextSecondary)
                TextButton(onClick = { unitPriceList = unitPriceList + ("" to "0") }) {
                    Icon(Icons.Default.Add, null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Add Unit")
                }
            }

            // --- CARD 4: LIST HARGA SATUAN ---
            Card(
                colors = CardDefaults.cardColors(containerColor = White),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (unitPriceList.isEmpty()) {
                        Text("Add at least one unit (e.g. Pcs)", color = TextTertiary, style = MaterialTheme.typography.bodySmall)
                    }

                    unitPriceList.forEachIndexed { index, (unitName, priceStr) ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            CleanTextField(
                                value = unitName,
                                onValueChange = { newName ->
                                    val newList = unitPriceList.toMutableList()
                                    newList[index] = newName to priceStr
                                    unitPriceList = newList
                                },
                                label = "Unit (ex: Pcs)",
                                modifier = Modifier.weight(1f)
                            )

                            CleanTextField(
                                value = priceStr,
                                onValueChange = { newPrice ->
                                    if(newPrice.all { c -> c.isDigit() }) {
                                        val newList = unitPriceList.toMutableList()
                                        newList[index] = unitName to newPrice
                                        unitPriceList = newList
                                    }
                                },
                                label = "Sell Price",
                                modifier = Modifier.weight(1f),
                                isNumber = true
                            )

                            if (unitPriceList.size > 1) {
                                IconButton(
                                    onClick = {
                                        val newList = unitPriceList.toMutableList()
                                        newList.removeAt(index)
                                        unitPriceList = newList
                                    }
                                ) {
                                    Icon(Icons.Default.Delete, "Remove", tint = SystemRed)
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(30.dp))
        }
    }

    if (showScanner) {
        Dialog(
            onDismissRequest = { showScanner = false },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Surface(modifier = Modifier.fillMaxSize()) {
                BarcodeScanner(
                    onCodeScanned = { code ->
                        barcode = code
                        showScanner = false
                    },
                    onClose = { showScanner = false }
                )
            }
        }
    }
}