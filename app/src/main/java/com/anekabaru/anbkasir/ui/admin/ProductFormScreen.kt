package com.anekabaru.anbkasir.ui.admin

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.anekabaru.anbkasir.ui.PosViewModel
import com.anekabaru.anbkasir.ui.theme.*
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.filled.Check
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductFormScreen(
    viewModel: PosViewModel,
    onBack: () -> Unit
) {
    val productToEdit = viewModel.selectedProduct
    val isEditMode = productToEdit != null

    // State
    var name by remember { mutableStateOf(productToEdit?.name ?: "") }
    var buyPrice by remember { mutableStateOf(productToEdit?.buyPrice?.toString() ?: "") }
    var sellPrice by remember { mutableStateOf(productToEdit?.sellPrice?.toString() ?: "") }
    var wholesale by remember { mutableStateOf(productToEdit?.wholesalePrice?.toString() ?: "") }
    var threshold by remember { mutableStateOf(productToEdit?.wholesaleThreshold?.toString() ?: "10") }
    var stock by remember { mutableStateOf(productToEdit?.stock?.toString() ?: "0") }
    var category by remember { mutableStateOf(productToEdit?.category ?: "General") }
    var barcode by remember { mutableStateOf(productToEdit?.barcode ?: "") }

    @Composable
    fun CleanTextField(
        value: String,
        onValueChange: (String) -> Unit,
        label: String,
        modifier: Modifier = Modifier,
        isNumber: Boolean = false
    ) {
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            label = { Text(label, style = MaterialTheme.typography.bodyMedium) },
            modifier = modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                // logic: use your BrandBlue if available, otherwise fallback to Primary
                focusedBorderColor = BrandBlue,
                focusedLabelColor = BrandBlue,
                // logic: use Color.LightGray if DividerColor causes issues
                unfocusedBorderColor = Color.LightGray,
                cursorColor = BrandBlue
            ),
            keyboardOptions = if (isNumber) KeyboardOptions(keyboardType = KeyboardType.Number) else KeyboardOptions.Default,
            singleLine = true
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (isEditMode) "Edit Product" else "New Product") },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back") } },
                actions = {
                    IconButton(onClick = {
                        if (name.isNotEmpty() && sellPrice.isNotEmpty()) {
                            if (isEditMode) {
                                viewModel.updateProduct(
                                    productToEdit!!.id, name, buyPrice.toDoubleOrNull() ?: 0.0,
                                    sellPrice.toDoubleOrNull() ?: 0.0, wholesale.toDoubleOrNull() ?: 0.0,
                                    threshold.toIntOrNull() ?: 10, stock.toIntOrNull() ?: 0, category, barcode
                                )
                            } else {
                                viewModel.addProduct(
                                    name, buyPrice.toDoubleOrNull() ?: 0.0,
                                    sellPrice.toDoubleOrNull() ?: 0.0, wholesale.toDoubleOrNull() ?: 0.0,
                                    threshold.toIntOrNull() ?: 10, stock.toIntOrNull() ?: 0, category, barcode
                                )
                            }
                            onBack()
                        }
                    }) {
                        Icon(Icons.Default.Check, "Save", tint = BrandBlue)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = White)
            )
        },
        containerColor = BackgroundGray
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Card(colors = CardDefaults.cardColors(containerColor = White)) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    CleanTextField(name, { name = it }, "Product Name")
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        CleanTextField(category, { category = it }, "Category", Modifier.weight(1f))
                        CleanTextField(barcode, { barcode = it }, "Barcode", Modifier.weight(1f), true)
                    }
                }
            }

            Text("Pricing & Stock", style = MaterialTheme.typography.titleMedium, color = TextSecondary)

            Card(colors = CardDefaults.cardColors(containerColor = White)) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        CleanTextField(buyPrice, { buyPrice = it }, "Cost Price", Modifier.weight(1f), true)
                        CleanTextField(sellPrice, { sellPrice = it }, "Sell Price", Modifier.weight(1f), true)
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        CleanTextField(wholesale, { wholesale = it }, "Wholesale", Modifier.weight(1f), true)
                        CleanTextField(threshold, { threshold = it }, "Min Qty", Modifier.weight(0.6f), true)
                    }
                    CleanTextField(stock, { stock = it }, "Initial Stock", isNumber = true)
                }
            }
        }
    }
}