package com.anekabaru.anbkasir.ui.admin

import androidx.compose.foundation.BorderStroke
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
        onBack()
        return
    }

    Scaffold(
        bottomBar = {
            Surface(
                shadowElevation = 2.dp,
                color = White,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = {
                            viewModel.deleteProduct(product.id)
                            onBack()
                        },
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = SystemRed
                        ),
                        border = BorderStroke(1.dp, SystemRed),
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.Delete, null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Delete", style = MaterialTheme.typography.labelLarge)
                    }

                    Button(
                        onClick = onEdit,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = BrandBlue
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.Edit, null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Edit Product", style = MaterialTheme.typography.labelLarge)
                    }
                }
            }
        },
        containerColor = BackgroundApp
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
        ) {
            // Header Section
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(White)
                    .padding(20.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        IconButton(
                            onClick = onBack,
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(BackgroundApp)
                        ) {
                            Icon(
                                Icons.AutoMirrored.Filled.ArrowBack,
                                "Back",
                                tint = TextPrimary,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        Text(
                            "Product Details",
                            style = MaterialTheme.typography.titleLarge,
                            color = TextPrimary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Product Name Highlight
                Text(
                    product.name,
                    style = MaterialTheme.typography.headlineSmall,
                    color = TextPrimary
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Surface(
                        color = SurfaceBlue,
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            product.category,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                            style = MaterialTheme.typography.labelSmall,
                            color = BrandBlue
                        )
                    }

                    if (product.barcode != null) {
                        Text("•", color = TextTertiary, style = MaterialTheme.typography.labelSmall)
                        Text(product.barcode, style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                    }
                }
            }

            // Content
            Column(
                modifier = Modifier
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // [MODIFIKASI] Pricing Section (Multi Unit)
                InfoSection(
                    title = "Pricing (Modal & Jual)",
                    icon = Icons.Outlined.Payments,
                    iconColor = BrandGreen
                ) {
                    // 1. Buy Price (Modal)
                    CompactInfoCard(
                        label = "Buy Price (Modal)",
                        value = "Rp${"%.0f".format(product.buyPrice)}",
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(8.dp))
                    Divider(color = BorderColor)
                    Spacer(modifier = Modifier.height(8.dp))

                    Text("Selling Units", style = MaterialTheme.typography.labelMedium, color = TextSecondary)

                    // 2. Loop Daftar Satuan
                    if (product.unitPrices.isNotEmpty()) {
                        product.unitPrices.forEach { (unit, price) ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(unit, style = MaterialTheme.typography.bodyMedium, color = TextPrimary)
                                Text(
                                    "Rp${"%.0f".format(price)}",
                                    style = MaterialTheme.typography.titleSmall,
                                    color = BrandGreen,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    } else {
                        // Fallback jika belum ada unitPrices
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Pcs (Default)", style = MaterialTheme.typography.bodyMedium)
                            Text("Rp${"%.0f".format(product.sellPrice)}", style = MaterialTheme.typography.titleSmall, color = BrandGreen, fontWeight = FontWeight.Bold)
                        }
                    }

                    // 3. Info Grosir (Jika ada)
                    if (product.wholesalePrice > 0) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Surface(
                            color = BackgroundApp,
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column {
                                    Text("Wholesale (Grosir)", style = MaterialTheme.typography.labelSmall, color = TextTertiary)
                                    Text("Min. Buy ${product.wholesaleThreshold}", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                                }
                                Text("Rp${"%.0f".format(product.wholesalePrice)}", style = MaterialTheme.typography.titleSmall, color = BrandBlue, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

                // Stock Section
                InfoSection(
                    title = "Inventory",
                    icon = Icons.Outlined.Inventory,
                    iconColor = BrandBlue
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(
                                if (product.stock <= product.wholesaleThreshold) SurfaceRed
                                else SurfaceGreen
                            )
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                "Current Stock",
                                style = MaterialTheme.typography.labelSmall,
                                color = TextTertiary
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                "${product.stock}",
                                style = MaterialTheme.typography.headlineSmall,
                                color = if (product.stock <= product.wholesaleThreshold) SystemRed else BrandGreen
                            )
                        }

                        if (product.stock <= product.wholesaleThreshold) {
                            Surface(
                                color = SystemRed,
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(
                                    "Low Stock",
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = White
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}

@Composable
fun InfoSection(
    title: String,
    icon: ImageVector,
    iconColor: Color,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(iconColor.copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = iconColor,
                        modifier = Modifier.size(18.dp)
                    )
                }
                Text(
                    title,
                    style = MaterialTheme.typography.titleMedium,
                    color = TextPrimary
                )
            }

            content()
        }
    }
}

@Composable
fun CompactInfoCard(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    valueColor: Color = TextPrimary,
    isHighlight: Boolean = false
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(BackgroundApp)
            .padding(12.dp)
    ) {
        Text(
            label,
            style = MaterialTheme.typography.labelSmall,
            color = TextTertiary
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            value,
            style = MaterialTheme.typography.titleSmall,
            color = valueColor,
            fontWeight = if (isHighlight) FontWeight.Bold else FontWeight.SemiBold
        )
    }
}