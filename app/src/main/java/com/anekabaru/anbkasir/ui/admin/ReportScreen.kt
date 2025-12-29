package com.anekabaru.anbkasir.ui.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Money
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.anekabaru.anbkasir.ui.PosViewModel
import com.anekabaru.anbkasir.ui.components.PullToRefreshLayout
import com.anekabaru.anbkasir.ui.theme.*
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportScreen(viewModel: PosViewModel, onBack: () -> Unit) {
    val calendar = Calendar.getInstance().apply {
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
    }
    val startOfDay = calendar.timeInMillis
    val endOfDay = System.currentTimeMillis()

    val dailySales by viewModel.getSalesTotal(startOfDay, endOfDay).collectAsState(initial = 0.0)
    val txCount by viewModel.getTxCount(startOfDay, endOfDay).collectAsState(initial = 0)
    val isSyncing by viewModel.isSyncing.collectAsState()

    Scaffold(
        containerColor = BackgroundApp
    ) { padding ->
        PullToRefreshLayout(
            isRefreshing = isSyncing,
            onRefresh = { viewModel.sync() },
            modifier = Modifier.padding(padding)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
            ) {
                // --- CUSTOM HEADER (Matches Inventory/History Style) ---
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
                        // Left: Back Button & Title
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            IconButton(onClick = onBack) {
                                Icon(
                                    Icons.AutoMirrored.Filled.ArrowBack,
                                    "Back",
                                    tint = TextPrimary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }

                            Column {
                                Text(
                                    "Financial Report",
                                    style = MaterialTheme.typography.titleLarge,
                                    color = TextPrimary
                                )
                                Text(
                                    "Daily Analytics",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = TextSecondary
                                )
                            }
                        }

                        // Right: Decorative Icon (Red for Reports)
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(SurfaceRed), // Red Background
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.Assessment,
                                contentDescription = null,
                                tint = SystemRed, // Red Icon
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }

                // --- CONTENT ---
                Column(modifier = Modifier.padding(20.dp)) {

                    // Section Title
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.DateRange, null, tint = TextSecondary, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            "TODAY'S SUMMARY",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextSecondary,
                            letterSpacing = 1.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Hero Card (Revenue)
                    StatCard(
                        title = "Total Revenue",
                        value = "Rp${dailySales ?: 0.0}",
                        icon = Icons.Default.Money,
                        color = BrandGreen,
                        isHero = true
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Secondary Cards
                    StatCard(
                        title = "Transactions",
                        value = "$txCount Orders",
                        icon = Icons.Default.Receipt,
                        color = BrandBlue
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    val estimatedProfit = (dailySales ?: 0.0) * 0.2
                    StatCard(
                        title = "Est. Gross Profit",
                        value = "Rp${"%.2f".format(estimatedProfit)}",
                        icon = Icons.AutoMirrored.Filled.TrendingUp,
                        color = BrandOrange
                    )
                }
            }
        }
    }
}

@Composable
fun StatCard(
    title: String,
    value: String,
    icon: ImageVector,
    color: Color,
    isHero: Boolean = false
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp), // Consistent 16dp
        colors = CardDefaults.cardColors(containerColor = White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier.padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = color.copy(alpha = 0.1f), // Soft background matching the icon color
                modifier = Modifier.size(if (isHero) 56.dp else 48.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = color,
                        modifier = Modifier.size(if (isHero) 28.dp else 24.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelSmall,
                    color = TextSecondary
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = value,
                    style = if (isHero) MaterialTheme.typography.headlineSmall else MaterialTheme.typography.titleLarge,
                    color = TextPrimary
                )
            }
        }
    }
}