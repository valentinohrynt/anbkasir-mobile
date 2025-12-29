package com.anekabaru.anbkasir.ui.admin

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.anekabaru.anbkasir.ui.PosViewModel
import com.anekabaru.anbkasir.ui.components.PullToRefreshLayout
import com.anekabaru.anbkasir.ui.theme.*
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SalesHistoryScreen(viewModel: PosViewModel, onBack: () -> Unit, onNavigateToDetail: () -> Unit){
    val history by viewModel.salesHistory.collectAsState()
    val isSyncing by viewModel.isSyncing.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Sales History", style = MaterialTheme.typography.titleLarge, color = TextPrimary) },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = TextPrimary) } },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = BackgroundGray)
            )
        },
        containerColor = BackgroundGray
    ) { padding ->
        PullToRefreshLayout(
            isRefreshing = isSyncing,
            onRefresh = { viewModel.sync() },
            modifier = Modifier.padding(padding)
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item { Spacer(modifier = Modifier.height(16.dp)) }

                if (history.isEmpty()) {
                    item {
                        Box(modifier = Modifier.fillParentMaxSize(), contentAlignment = androidx.compose.ui.Alignment.Center) {
                            Text("No transactions found", color = TextSecondary)
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
                            elevation = CardDefaults.cardElevation(0.dp)
                        ) {
                            ListItem(
                                headlineContent = { Text("Order #${tx.id.take(8)}", fontWeight = FontWeight.Bold) },
                                supportingContent = {
                                    Text(SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault()).format(Date(tx.date)))
                                },
                                trailingContent = {
                                    Text("Rp${tx.totalAmount}", style = MaterialTheme.typography.titleMedium, color = BrandGreen)
                                },
                                colors = ListItemDefaults.colors(containerColor = White)
                            )
                        }
                    }
                }
                item { Spacer(modifier = Modifier.height(32.dp)) }
            }
        }
    }
}