package com.anekabaru.anbkasir.ui.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.anekabaru.anbkasir.ui.PosViewModel
import com.anekabaru.anbkasir.ui.components.PullToRefreshLayout
import com.anekabaru.anbkasir.ui.theme.*
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SalesHistoryScreen(
    viewModel: PosViewModel,
    onBack: () -> Unit,
    onNavigateToDetail: () -> Unit
){
    val history by viewModel.salesHistory.collectAsState()
    val isSyncing by viewModel.isSyncing.collectAsState()

    // Add semantic color for History (Orange) matching Dashboard
    val SurfaceOrange = Color(0xFFFFF7ED)

    Scaffold(
        containerColor = BackgroundApp
    ) { padding ->
        PullToRefreshLayout(
            isRefreshing = isSyncing,
            onRefresh = { viewModel.sync() },
            modifier = Modifier.padding(padding)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {

                // --- CUSTOM HEADER (Matches InventoryScreen Style) ---
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
                                    "Sales History",
                                    style = MaterialTheme.typography.titleLarge,
                                    color = TextPrimary
                                )
                                Text(
                                    "${history.size} Transactions",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = TextSecondary
                                )
                            }
                        }

                        // Right: Decorative Icon (The feature you requested)
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(SurfaceOrange), // Orange Background
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.History,
                                contentDescription = null,
                                tint = BrandOrange, // Orange Icon
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }

                    // Optional: Search/Filter for History could go here
                    // (omitted for now to keep it clean, but space is ready)
                }

                Spacer(modifier = Modifier.height(8.dp))

                // --- CONTENT ---
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(top = 8.dp, bottom = 16.dp)
                ) {
                    if (history.isEmpty()) {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(300.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Icon(Icons.Default.History, null, tint = TextTertiary, modifier = Modifier.size(48.dp))
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text("No transactions found", style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
                                }
                            }
                        }
                    } else {
                        items(history) { tx ->
                            Card(
                                modifier = Modifier.clickable {
                                    viewModel.openTransactionDetail(tx)
                                    onNavigateToDetail()
                                },
                                colors = CardDefaults.cardColors(containerColor = White),
                                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                ListItem(
                                    headlineContent = {
                                        Text(
                                            "Order #${tx.id.take(8).uppercase()}",
                                            style = MaterialTheme.typography.titleSmall
                                        )
                                    },
                                    supportingContent = {
                                        Text(
                                            SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault()).format(Date(tx.date)),
                                            style = MaterialTheme.typography.labelSmall,
                                            color = TextSecondary
                                        )
                                    },
                                    trailingContent = {
                                        Text(
                                            "Rp${tx.totalAmount}",
                                            style = MaterialTheme.typography.titleSmall,
                                            color = BrandGreen
                                        )
                                    },
                                    colors = ListItemDefaults.colors(containerColor = White)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}