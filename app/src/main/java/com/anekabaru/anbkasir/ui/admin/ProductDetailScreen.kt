package com.anekabaru.anbkasir.ui.admin

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.anekabaru.anbkasir.ui.PosViewModel
import com.anekabaru.anbkasir.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductDetailScreen(
    viewModel: PosViewModel,
    onBack: () -> Unit,
    onEdit: () -> Unit
) {
    val product = viewModel.selectedProduct

    if (product == null) {
        onBack() // Fallback if no product selected
        return
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Product Details") },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back") } },
                actions = { IconButton(onClick = onEdit) { Icon(Icons.Default.Edit, "Edit", tint = BrandBlue) } },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = White)
            )
        },
        bottomBar = {
            Surface(shadowElevation = 8.dp, color = White) {
                Button(
                    onClick = {
                        viewModel.deleteProduct(product.id)
                        onBack()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = SystemRed.copy(alpha = 0.1f), contentColor = SystemRed),
                    modifier = Modifier.fillMaxWidth().padding(16.dp).height(50.dp)
                ) {
                    Icon(Icons.Default.Delete, null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Delete Product")
                }
            }
        },
        containerColor = BackgroundGray
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // General Info Card
            Card(colors = CardDefaults.cardColors(containerColor = White), shape = RoundedCornerShape(12.dp)) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    DetailRow("Name", product.name, true)
                    Row(modifier = Modifier.fillMaxWidth()) {
                        Box(modifier = Modifier.weight(1f)) { DetailRow("Category", product.category) }
                        Box(modifier = Modifier.weight(1f)) { DetailRow("Barcode", product.barcode ?: "-") }
                    }
                }
            }

            // Pricing Card
            Card(colors = CardDefaults.cardColors(containerColor = White), shape = RoundedCornerShape(12.dp)) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Text("Pricing", style = MaterialTheme.typography.titleMedium, color = BrandBlue)
                    HorizontalDivider(color = BackgroundGray)
                    Row(modifier = Modifier.fillMaxWidth()) {
                        Box(modifier = Modifier.weight(1f)) { DetailRow("Cost (Buy)", "Rp${product.buyPrice}") }
                        Box(modifier = Modifier.weight(1f)) { DetailRow("Retail (Sell)", "Rp${product.sellPrice}", true, isHighlight = true, highlightColor = BrandGreen) }
                    }
                    Row(modifier = Modifier.fillMaxWidth()) {
                        Box(modifier = Modifier.weight(1f)) { DetailRow("Wholesale", "Rp${product.wholesalePrice}") }
                        Box(modifier = Modifier.weight(1f)) { DetailRow("Min. Qty", "${product.wholesaleThreshold}") }
                    }
                }
            }

            // Stock Card
            Card(colors = CardDefaults.cardColors(containerColor = White), shape = RoundedCornerShape(12.dp)) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Text("Inventory", style = MaterialTheme.typography.titleMedium, color = BrandBlue)
                    HorizontalDivider(color = BackgroundGray)
                    DetailRow("Current Stock", "${product.stock}", true, isHighlight = product.stock < 10, highlightColor = SystemRed)
                }
            }
        }
    }
}

@Composable
fun DetailRow(label: String, value: String, isBold: Boolean = false, isHighlight: Boolean = false, highlightColor: Color = TextPrimary) {
    Column {
        Text(label, style = MaterialTheme.typography.labelSmall, color = TextTertiary)
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            value,
            style = if (isBold) MaterialTheme.typography.titleMedium else MaterialTheme.typography.bodyLarge,
            color = if (isHighlight) highlightColor else TextPrimary,
            fontWeight = if (isBold) FontWeight.Bold else FontWeight.Normal
        )
    }
}