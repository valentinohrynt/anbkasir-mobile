package com.anekabaru.anbkasir.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material.icons.filled.PointOfSale
import androidx.compose.material.icons.outlined.TrendingUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.anekabaru.anbkasir.ui.components.PullToRefreshLayout
import com.anekabaru.anbkasir.ui.theme.BackgroundApp
import com.anekabaru.anbkasir.ui.theme.BrandBlue
import com.anekabaru.anbkasir.ui.theme.BrandGreen
import com.anekabaru.anbkasir.ui.theme.BrandOrange
import com.anekabaru.anbkasir.ui.theme.SurfaceBlue
import com.anekabaru.anbkasir.ui.theme.SystemRed
import com.anekabaru.anbkasir.ui.theme.TextPrimary
import com.anekabaru.anbkasir.ui.theme.TextSecondary
import com.anekabaru.anbkasir.ui.theme.White

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    viewModel: PosViewModel,
    onNavigateToPos: () -> Unit,
    onNavigateToInventory: () -> Unit,
    onNavigateToHistory: () -> Unit,
    onNavigateToReport: () -> Unit
) {
    val isSyncing by viewModel.isSyncing.collectAsState()

    Scaffold(
        containerColor = BackgroundApp // Theme Color
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
                        Column {
                            Text(
                                "Aneka Baru",
                                style = MaterialTheme.typography.titleLarge,
                                color = TextPrimary
                            )
                            Text(
                                "Kasir Management",
                                style = MaterialTheme.typography.bodySmall, // Use standard bodySmall
                                color = TextSecondary
                            )
                        }

                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(SurfaceBlue), // Theme Surface Color
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Outlined.TrendingUp,
                                contentDescription = null,
                                tint = BrandBlue, // Theme Brand Color
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Main Menu Grid
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        CompactMenuCard(
                            title = "Point of Sale",
                            subtitle = "New Transaction",
                            icon = Icons.Default.PointOfSale,
                            color = BrandGreen, // Theme Color
                            onClick = onNavigateToPos,
                            modifier = Modifier.weight(1f)
                        )
                        CompactMenuCard(
                            title = "Inventory",
                            subtitle = "Stock Management",
                            icon = Icons.Default.Inventory,
                            color = BrandBlue, // Theme Color
                            onClick = onNavigateToInventory,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        CompactMenuCard(
                            title = "History",
                            subtitle = "Past Transactions",
                            icon = Icons.Default.History,
                            color = BrandOrange, // Theme Color
                            onClick = onNavigateToHistory,
                            modifier = Modifier.weight(1f)
                        )
                        CompactMenuCard(
                            title = "Reports",
                            subtitle = "Analytics",
                            icon = Icons.Default.Assessment,
                            color = SystemRed, // Theme Color
                            onClick = onNavigateToReport,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@Composable
fun CompactMenuCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    color: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .aspectRatio(1f)
            .clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(color.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = color,
                    modifier = Modifier.size(24.dp)
                )
            }

            Column {
                Text(
                    title,
                    style = MaterialTheme.typography.titleSmall, // 15.sp defined in Type.kt
                    color = TextPrimary
                )
                Text(
                    subtitle,
                    style = MaterialTheme.typography.labelSmall, // 11.sp defined in Type.kt
                    color = TextSecondary
                )
            }
        }
    }
}