package com.anekabaru.anbkasir.ui.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.anekabaru.anbkasir.data.ProductEntity
import com.anekabaru.anbkasir.ui.PosViewModel
import com.anekabaru.anbkasir.ui.theme.* // Import your Theme files

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InventoryScreen(viewModel: PosViewModel, onBack: () -> Unit) {
    val products by viewModel.products.collectAsState()
    var showDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Inventory",
                        style = MaterialTheme.typography.headlineLarge.copy(fontSize = 24.sp), // Slightly smaller than dashboard
                        color = MaterialTheme.colorScheme.onBackground
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = MaterialTheme.colorScheme.onBackground)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background // Seamless blend
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showDialog = true },
                containerColor = BrandBlue, // Use Brand Color
                contentColor = White
            ) {
                Icon(Icons.Default.Add, "Add Product")
            }
        },
        containerColor = MaterialTheme.colorScheme.background // Match Dashboard Gray
    ) { padding ->
        if (products.isEmpty()) {
            Box(
                modifier = Modifier
                    .padding(padding)
                    .fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "No products found.\nTap + to add one.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .padding(padding)
                    .padding(horizontal = 20.dp), // Match Dashboard padding
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item { Spacer(modifier = Modifier.height(8.dp)) } // Top spacing
                items(products) { product ->
                    ProductItemCard(product)
                }
                item { Spacer(modifier = Modifier.height(80.dp)) } // Bottom spacing for FAB
            }
        }
    }

    if (showDialog) {
        AddProductDialog(
            onDismiss = { showDialog = false },
            onSave = { name, buy, sell, wholesale, threshold, stock, cat, bar ->
                viewModel.addProduct(name, buy, sell, wholesale, threshold, stock, cat, bar)
                showDialog = false
            }
        )
    }
}

@Composable
fun ProductItemCard(product: ProductEntity) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = White),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            // Header: Name & Category
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        product.name,
                        style = MaterialTheme.typography.titleMedium,
                        color = TextPrimary
                    )
                    Spacer(modifier = Modifier.height(4.dp))

                    // Category Badge
                    Surface(
                        color = BrandBlue.copy(alpha = 0.1f),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            text = product.category,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            style = MaterialTheme.typography.labelSmall,
                            color = BrandBlue
                        )
                    }
                }

                // Stock Badge
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        "Stock",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextTertiary
                    )
                    Text(
                        "${product.stock}",
                        style = MaterialTheme.typography.titleMedium,
                        color = if(product.stock < 10) SystemRed else TextPrimary
                    )
                }
            }

            HorizontalDivider(
                modifier = Modifier.padding(vertical = 12.dp),
                color = BackgroundGray
            )

            // Details Grid
            Row(modifier = Modifier.fillMaxWidth()) {
                // Retail Price
                Column(modifier = Modifier.weight(1f)) {
                    Text("Retail Price", style = MaterialTheme.typography.labelSmall, color = TextTertiary)
                    Text("Rp${product.sellPrice}", style = MaterialTheme.typography.labelLarge, color = BrandGreen)
                }

                // Wholesale
                Column(modifier = Modifier.weight(1f)) {
                    Text("Wholesale", style = MaterialTheme.typography.labelSmall, color = TextTertiary)
                    Text("Rp${product.wholesalePrice}", style = MaterialTheme.typography.labelLarge, color = TextSecondary)
                    Text("min ${product.wholesaleThreshold} pcs", style = MaterialTheme.typography.labelSmall, fontSize = 10.sp, color = TextTertiary)
                }

                // Buy Price (Cost)
                Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.End) {
                    Text("Cost", style = MaterialTheme.typography.labelSmall, color = TextTertiary)
                    Text("Rp${product.buyPrice}", style = MaterialTheme.typography.labelLarge, color = TextSecondary)
                }
            }
        }
    }
}

@Composable
fun AddProductDialog(
    onDismiss: () -> Unit,
    onSave: (String, Double, Double, Double, Int, Int, String, String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var buyPrice by remember { mutableStateOf("") }
    var sellPrice by remember { mutableStateOf("") }
    var wholesale by remember { mutableStateOf("") }
    var threshold by remember { mutableStateOf("10") }
    var stock by remember { mutableStateOf("0") }
    var category by remember { mutableStateOf("General") }
    var barcode by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = White,
        titleContentColor = TextPrimary,
        textContentColor = TextSecondary,
        title = { Text("New Product", style = MaterialTheme.typography.titleLarge) },
        text = {
            Column(
                modifier = Modifier
                    .verticalScroll(rememberScrollState())
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                CleanTextField(value = name, onValueChange = { name = it }, label = "Product Name")

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    CleanTextField(value = category, onValueChange = { category = it }, label = "Category", modifier = Modifier.weight(1f))
                    CleanTextField(value = barcode, onValueChange = { barcode = it }, label = "Barcode", modifier = Modifier.weight(1f), isNumber = true)
                }

                HorizontalDivider(color = BackgroundGray)
                Text("Pricing", style = MaterialTheme.typography.labelLarge, color = BrandBlue)

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    CleanTextField(value = buyPrice, onValueChange = { buyPrice = it }, label = "Cost (Buy)", modifier = Modifier.weight(1f), isNumber = true)
                    CleanTextField(value = sellPrice, onValueChange = { sellPrice = it }, label = "Retail (Sell)", modifier = Modifier.weight(1f), isNumber = true)
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    CleanTextField(value = wholesale, onValueChange = { wholesale = it }, label = "Wholesale", modifier = Modifier.weight(1f), isNumber = true)
                    CleanTextField(value = threshold, onValueChange = { threshold = it }, label = "Min Qty", modifier = Modifier.weight(0.6f), isNumber = true)
                }

                CleanTextField(value = stock, onValueChange = { stock = it }, label = "Initial Stock", isNumber = true)
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isNotEmpty() && sellPrice.isNotEmpty()) {
                        onSave(
                            name,
                            buyPrice.toDoubleOrNull() ?: 0.0,
                            sellPrice.toDoubleOrNull() ?: 0.0,
                            wholesale.toDoubleOrNull() ?: (sellPrice.toDoubleOrNull() ?: 0.0),
                            threshold.toIntOrNull() ?: 10,
                            stock.toIntOrNull() ?: 0,
                            category,
                            barcode
                        )
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = BrandBlue)
            ) { Text("Save Product") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel", color = TextSecondary) }
        }
    )
}

// Helper for cleaner text fields
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
            unfocusedBorderColor = DividerColor,
            focusedBorderColor = BrandBlue,
            focusedLabelColor = BrandBlue,
            unfocusedLabelColor = TextTertiary
        ),
        keyboardOptions = if (isNumber) KeyboardOptions(keyboardType = KeyboardType.Number) else KeyboardOptions.Default,
        singleLine = true
    )
}