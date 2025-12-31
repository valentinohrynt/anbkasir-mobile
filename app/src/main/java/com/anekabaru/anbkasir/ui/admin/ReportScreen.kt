package com.anekabaru.anbkasir.ui.admin

import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Inventory
import androidx.compose.material.icons.outlined.MonetizationOn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.content.FileProvider
import com.anekabaru.anbkasir.ui.PosViewModel
import com.anekabaru.anbkasir.ui.components.RupiahText
import com.anekabaru.anbkasir.ui.theme.*
import com.anekabaru.anbkasir.util.toRupiah
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportScreen(
    viewModel: PosViewModel,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val products by viewModel.products.collectAsState()

    val totalAssetValue = remember(products) {
        products.sumOf { it.stock * it.buyPrice }
    }

    val totalPotentialRevenue = remember(products) {
        products.sumOf { it.stock * it.sellPrice }
    }

    val outOfStockProducts = remember(products) {
        products.filter { it.stock <= 0 }
    }

    val lowStockProducts = remember(products) {
        products.filter { it.stock > 0 && it.stock <= it.wholesaleThreshold }
    }

    var showOutOfStockDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Reports & Analytics", style = MaterialTheme.typography.titleLarge) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = TextPrimary)
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

            Text("Asset Valuation", style = MaterialTheme.typography.titleMedium, color = TextSecondary)

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                SummaryCard(
                    title = "Total Stock Cost",
                    amount = totalAssetValue,
                    icon = Icons.Outlined.Inventory,
                    color = BrandBlue,
                    modifier = Modifier.weight(1f)
                )
                SummaryCard(
                    title = "Potential Revenue",
                    amount = totalPotentialRevenue,
                    icon = Icons.Outlined.MonetizationOn,
                    color = BrandGreen,
                    modifier = Modifier.weight(1f)
                )
            }

            Text("Inventory Status", style = MaterialTheme.typography.titleMedium, color = TextSecondary)

            Card(
                colors = CardDefaults.cardColors(containerColor = White),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { showOutOfStockDialog = true }
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(
                                if (outOfStockProducts.isNotEmpty())
                                    SystemRed.copy(alpha = 0.1f)
                                else
                                    BrandGreen.copy(alpha = 0.1f)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            if (outOfStockProducts.isNotEmpty())
                                Icons.Default.Warning
                            else
                                Icons.Default.TrendingUp,
                            null,
                            tint = if (outOfStockProducts.isNotEmpty()) SystemRed else BrandGreen
                        )
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text("Out of Stock Items", style = MaterialTheme.typography.titleSmall)
                        Text(
                            if (outOfStockProducts.isNotEmpty())
                                "${outOfStockProducts.size} items need restocking"
                            else
                                "All items are available",
                            style = MaterialTheme.typography.bodySmall,
                            color = if (outOfStockProducts.isNotEmpty()) SystemRed else TextSecondary
                        )
                    }

                    Icon(
                        Icons.AutoMirrored.Filled.ArrowBack,
                        null,
                        modifier = Modifier.size(16.dp).graphicsLayer { rotationZ = 180f },
                        tint = TextTertiary
                    )
                }
            }

            if (lowStockProducts.isNotEmpty()) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = White),
                    shape = RoundedCornerShape(16.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(BrandOrange.copy(alpha = 0.1f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Warning, null, tint = BrandOrange)
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Column {
                            Text("Low Stock Warning", style = MaterialTheme.typography.titleSmall)
                            Text("${lowStockProducts.size} items running low", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                        }
                    }
                }
            }
        }
    }

    if (showOutOfStockDialog) {
        Dialog(
            onDismissRequest = { showOutOfStockDialog = false },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Surface(
                modifier = Modifier.fillMaxSize().padding(16.dp),
                shape = RoundedCornerShape(16.dp),
                color = BackgroundApp
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Out of Stock Report", style = MaterialTheme.typography.titleLarge)
                            Text("${outOfStockProducts.size} items require purchase", color = SystemRed)
                        }
                        IconButton(onClick = { showOutOfStockDialog = false }) {
                            Icon(Icons.Default.Close, null)
                        }
                    }

                    Divider(modifier = Modifier.padding(vertical = 12.dp))

                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(outOfStockProducts) { product ->
                            Card(
                                colors = CardDefaults.cardColors(containerColor = White),
                                border = androidx.compose.foundation.BorderStroke(1.dp, BorderColor)
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp).fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(product.name, fontWeight = FontWeight.Bold)
                                        Text(product.category, style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                                    }
                                    Surface(
                                        color = SystemRed.copy(alpha = 0.1f),
                                        shape = RoundedCornerShape(4.dp)
                                    ) {
                                        Text(
                                            "Stock: ${product.stock}",
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                            color = SystemRed,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = {
                            val csv = StringBuilder()
                            csv.append("Product Name,Category,Barcode,Stock,Buy Price\n")
                            outOfStockProducts.forEach {
                                csv.append("\"${it.name}\",\"${it.category}\",\"${it.barcode ?: "-"}\",${it.stock},${it.buyPrice.toRupiah()}\n")
                            }

                            val file = File(
                                context.cacheDir,
                                "out_of_stock_${System.currentTimeMillis()}.csv"
                            )
                            file.writeText(csv.toString())

                            val uri = FileProvider.getUriForFile(
                                context,
                                "${context.packageName}.provider",
                                file
                            )

                            val intent = Intent(Intent.ACTION_SEND).apply {
                                type = "text/csv"
                                putExtra(Intent.EXTRA_STREAM, uri)
                                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                            }

                            context.startActivity(
                                Intent.createChooser(intent, "Export CSV Report")
                            )
                        },
                        modifier = Modifier.fillMaxWidth().height(50.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = BrandBlue)
                    ) {
                        Icon(Icons.Default.Share, null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Export CSV")
                    }
                }
            }
        }
    }
}

@Composable
fun SummaryCard(
    title: String,
    amount: Double,
    icon: ImageVector,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = White),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(color.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, null, tint = color)
            }

            Column {
                Text(title, style = MaterialTheme.typography.labelMedium, color = TextSecondary)
                RupiahText(amount = amount, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            }
        }
    }
}