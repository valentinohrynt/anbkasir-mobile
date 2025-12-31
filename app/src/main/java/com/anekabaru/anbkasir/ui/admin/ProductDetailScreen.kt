package com.anekabaru.anbkasir.ui.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.outlined.Inventory
import androidx.compose.material.icons.outlined.Payments
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.anekabaru.anbkasir.ui.PosViewModel
import com.anekabaru.anbkasir.ui.components.RupiahText
import com.anekabaru.anbkasir.ui.theme.*
import com.anekabaru.anbkasir.util.toRupiah
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductDetailScreen(
    viewModel: PosViewModel,
    onBack: () -> Unit,
    onEdit: () -> Unit
) {
    val product = viewModel.selectedProduct
    var showDeleteDialog by remember { mutableStateOf(false) }

    if (product == null) {
        LaunchedEffect(Unit) { onBack() }
        return
    }

    Scaffold(
        bottomBar = {
            Surface(shadowElevation = 8.dp, color = White, modifier = Modifier.fillMaxWidth()) {
                Box(modifier = Modifier.padding(16.dp)) {
                    Button(onClick = onEdit, colors = ButtonDefaults.buttonColors(containerColor = BrandBlue), modifier = Modifier.fillMaxWidth().height(52.dp), shape = RoundedCornerShape(12.dp)) {
                        Icon(Icons.Default.Edit, null, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Edit Product", style = MaterialTheme.typography.titleMedium)
                    }
                }
            }
        },
        containerColor = BackgroundApp
    ) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize()) {
            Column(modifier = Modifier.fillMaxWidth().background(White).padding(20.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        IconButton(onClick = onBack, modifier = Modifier.size(40.dp).clip(CircleShape).background(BackgroundApp)) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = TextPrimary)
                        }
                        Text("Product Details", style = MaterialTheme.typography.titleLarge, color = TextPrimary)
                    }
                    IconButton(onClick = { showDeleteDialog = true }, modifier = Modifier.size(40.dp).clip(CircleShape).background(SystemRed.copy(alpha = 0.1f))) {
                        Icon(Icons.Default.Delete, "Delete Product", tint = SystemRed, modifier = Modifier.size(20.dp))
                    }
                }
                Spacer(modifier = Modifier.height(20.dp))
                Text(product.name, style = MaterialTheme.typography.headlineSmall, color = TextPrimary)
                Spacer(modifier = Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Surface(color = SurfaceBlue, shape = RoundedCornerShape(6.dp)) {
                        Text(product.category, modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp), style = MaterialTheme.typography.labelSmall, color = BrandBlue)
                    }
                    if (product.barcode != null) {
                        Text("•", color = TextTertiary)
                        Text(product.barcode, style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                    }
                }
            }

            Column(modifier = Modifier.verticalScroll(rememberScrollState()).padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                InfoSection(title = "Pricing Information", icon = Icons.Outlined.Payments, iconColor = BrandGreen) {
                    CompactInfoCard(label = "Buy Price (Modal)", value = product.buyPrice.toRupiah(), modifier = Modifier.fillMaxWidth())
                    Spacer(modifier = Modifier.height(8.dp))
                    Divider(color = BorderColor)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Selling Units", style = MaterialTheme.typography.labelMedium, color = TextSecondary)
                    product.unitPrices.forEach { (unit, price) ->
                        Row(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(unit, style = MaterialTheme.typography.bodyMedium, color = TextPrimary)
                            RupiahText(amount = price, style = MaterialTheme.typography.titleSmall, color = BrandGreen, fontWeight = FontWeight.Bold)
                        }
                    }
                    if (product.wholesalePrice > 0) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Surface(color = BackgroundApp, shape = RoundedCornerShape(8.dp), modifier = Modifier.fillMaxWidth()) {
                            Row(modifier = Modifier.padding(12.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                                Column {
                                    Text("Grosir / Wholesale (Eceran)", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                                    Text("Min. Qty: ${product.wholesaleThreshold}", style = MaterialTheme.typography.bodySmall, color = TextTertiary)
                                }
                                RupiahText(amount = product.wholesalePrice, style = MaterialTheme.typography.titleSmall, color = BrandBlue, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

                InfoSection(title = "Inventory Status", icon = Icons.Outlined.Inventory, iconColor = BrandBlue) {
                    Row(modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).background(if (product.stock <= 5) SurfaceRed else SurfaceGreen).padding(16.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Column {
                            Text("Current Stock", style = MaterialTheme.typography.labelSmall, color = TextTertiary)
                            Text("${product.stock}", style = MaterialTheme.typography.headlineSmall, color = if (product.stock <= 5) SystemRed else BrandGreen, fontWeight = FontWeight.Bold)
                        }
                        if (product.stock <= 5) {
                            Surface(color = SystemRed, shape = RoundedCornerShape(8.dp)) {
                                Text("Low Stock", modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp), style = MaterialTheme.typography.labelSmall, color = White)
                            }
                        }
                    }
                    Text("Last Updated: ${SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault()).format(Date(product.updatedAt))}", style = MaterialTheme.typography.labelSmall, color = TextTertiary, modifier = Modifier.padding(top = 8.dp))
                }
            }
        }
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            icon = { Icon(Icons.Default.Delete, null, tint = SystemRed) },
            title = { Text("Hapus Produk?", fontWeight = FontWeight.Bold) },
            text = { Text("Apakah Anda yakin ingin menghapus '${product.name}'?", textAlign = androidx.compose.ui.text.style.TextAlign.Center) },
            confirmButton = {
                Button(onClick = { viewModel.deleteProduct(product.id); showDeleteDialog = false; onBack() }, colors = ButtonDefaults.buttonColors(containerColor = SystemRed)) { Text("Ya, Hapus") }
            },
            dismissButton = { TextButton(onClick = { showDeleteDialog = false }) { Text("Batal", color = TextSecondary) } },
            containerColor = White, shape = RoundedCornerShape(16.dp)
        )
    }
}

@Composable
fun InfoSection(title: String, icon: ImageVector, iconColor: Color, content: @Composable ColumnScope.() -> Unit) {
    Card(colors = CardDefaults.cardColors(containerColor = White), elevation = CardDefaults.cardElevation(defaultElevation = 1.dp), shape = RoundedCornerShape(16.dp)) {
        Column(modifier = Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Box(modifier = Modifier.size(32.dp).clip(RoundedCornerShape(8.dp)).background(iconColor.copy(alpha = 0.1f)), contentAlignment = Alignment.Center) {
                    Icon(icon, null, tint = iconColor, modifier = Modifier.size(18.dp))
                }
                Text(title, style = MaterialTheme.typography.titleMedium, color = TextPrimary)
            }
            content()
        }
    }
}

@Composable
fun CompactInfoCard(label: String, value: String, modifier: Modifier = Modifier) {
    Column(modifier = modifier.clip(RoundedCornerShape(10.dp)).background(BackgroundApp).padding(12.dp)) {
        Text(label, style = MaterialTheme.typography.labelSmall, color = TextTertiary)
        Spacer(modifier = Modifier.height(4.dp))
        Text(value, style = MaterialTheme.typography.titleSmall, color = TextPrimary, fontWeight = FontWeight.Bold)
    }
}