package com.anekabaru.anbkasir.ui.admin

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.anekabaru.anbkasir.data.ProductEntity
import com.anekabaru.anbkasir.ui.PosViewModel
import com.anekabaru.anbkasir.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InventoryScreen(
    viewModel: PosViewModel,
    onBack: () -> Unit,
    onNavigateToDetail: () -> Unit,
    onNavigateToForm: () -> Unit
) {
    val products by viewModel.products.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Inventory", style = MaterialTheme.typography.headlineLarge.copy(fontSize = 24.sp), color = TextPrimary) },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = TextPrimary) } },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = BackgroundGray)
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    viewModel.selectProduct(null) // Clear selection for "New Product"
                    onNavigateToForm()
                },
                containerColor = BrandBlue,
                contentColor = White
            ) {
                Icon(Icons.Default.Add, "Add Product")
            }
        },
        containerColor = BackgroundGray
    ) { padding ->
        if (products.isEmpty()) {
            Box(modifier = Modifier.padding(padding).fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("No products. Tap + to add.", style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
            }
        } else {
            LazyColumn(
                modifier = Modifier.padding(padding).padding(horizontal = 20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item { Spacer(modifier = Modifier.height(8.dp)) }
                items(products) { product ->
                    ProductItemCard(
                        product = product,
                        onClick = {
                            viewModel.selectProduct(product) // Set selection
                            onNavigateToDetail()
                        }
                    )
                }
                item { Spacer(modifier = Modifier.height(80.dp)) }
            }
        }
    }
}

@Composable
fun ProductItemCard(product: ProductEntity, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = White),
        elevation = CardDefaults.cardElevation(0.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier.padding(20.dp).fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(product.name, style = MaterialTheme.typography.titleMedium, color = TextPrimary, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(color = BrandBlue.copy(alpha = 0.1f), shape = RoundedCornerShape(6.dp)) {
                        Text(product.category, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp), style = MaterialTheme.typography.labelSmall, color = BrandBlue)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Rp${product.sellPrice}", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = BrandGreen)
                }
            }
            Column(horizontalAlignment = Alignment.End) {
                Text("Stock", style = MaterialTheme.typography.labelSmall, color = TextTertiary)
                Text("${product.stock}", style = MaterialTheme.typography.titleMedium, color = if(product.stock < 10) SystemRed else TextPrimary)
            }
        }
    }
}