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
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.History
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.anekabaru.anbkasir.ui.PosViewModel
import com.anekabaru.anbkasir.ui.components.PullToRefreshLayout
import com.anekabaru.anbkasir.ui.components.RupiahText
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
) {
    val history by viewModel.salesHistory.collectAsState()
    val isSyncing by viewModel.isSyncing.collectAsState()

    var showDatePicker by remember { mutableStateOf(false) }
    var selectedDateRange by remember { mutableStateOf<Pair<Long?, Long?>>(null to null) }

    val filteredHistory = remember(history, selectedDateRange) {
        val (start, end) = selectedDateRange
        if (start == null || end == null) history
        else history.filter { it.date in start..(end + 86400000 - 1) }
    }

    Scaffold(containerColor = BackgroundApp) { padding ->
        PullToRefreshLayout(isRefreshing = isSyncing, onRefresh = { viewModel.sync() }, modifier = Modifier.padding(padding)) {
            Column(modifier = Modifier.fillMaxSize()) {
                Column(modifier = Modifier.fillMaxWidth().background(White).padding(20.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, null, modifier = Modifier.size(20.dp)) }
                            Column {
                                Text("Sales History", style = MaterialTheme.typography.titleLarge)
                                Text("${filteredHistory.size} Transactions", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                            }
                        }
                        Box(modifier = Modifier.size(36.dp).clip(CircleShape).background(Color(0xFFFFF7ED)), contentAlignment = Alignment.Center) {
                            Icon(Icons.Default.History, null, tint = BrandOrange, modifier = Modifier.size(20.dp))
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                        OutlinedButton(onClick = { showDatePicker = true }, modifier = Modifier.weight(1f), shape = RoundedCornerShape(12.dp)) {
                            Icon(Icons.Default.CalendarToday, null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            val (s, e) = selectedDateRange
                            Text(if (s != null && e != null) "${SimpleDateFormat("dd MMM", Locale.getDefault()).format(Date(s))} - ${SimpleDateFormat("dd MMM", Locale.getDefault()).format(Date(e))}" else "Filter by Date Range")
                        }
                        if (selectedDateRange.first != null) {
                            Spacer(modifier = Modifier.width(8.dp))
                            IconButton(onClick = { selectedDateRange = null to null }, modifier = Modifier.background(BackgroundApp, CircleShape)) { Icon(Icons.Default.Close, null) }
                        }
                    }
                }

                LazyColumn(modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp), verticalArrangement = Arrangement.spacedBy(12.dp), contentPadding = PaddingValues(top = 16.dp, bottom = 16.dp)) {
                    if (filteredHistory.isEmpty()) {
                        item { Box(modifier = Modifier.fillMaxWidth().height(300.dp), contentAlignment = Alignment.Center) { Text("No transactions found", color = TextSecondary) } }
                    } else {
                        items(filteredHistory) { tx ->
                            Card(modifier = Modifier.clickable { viewModel.openTransactionDetail(tx); onNavigateToDetail() }, colors = CardDefaults.cardColors(containerColor = White), elevation = CardDefaults.cardElevation(defaultElevation = 1.dp), shape = RoundedCornerShape(12.dp)) {
                                ListItem(
                                    headlineContent = { Text("Order #${tx.id.take(8).uppercase()}", style = MaterialTheme.typography.titleSmall) },
                                    supportingContent = { Text(SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault()).format(Date(tx.date)), style = MaterialTheme.typography.labelSmall) },
                                    trailingContent = {
                                        Column(horizontalAlignment = Alignment.End) {
                                            RupiahText(amount = tx.totalAmount, style = MaterialTheme.typography.titleSmall, color = BrandGreen)
                                            Text(tx.paymentMethod, style = MaterialTheme.typography.labelSmall, color = TextTertiary)
                                        }
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

    if (showDatePicker) {
        val state = rememberDateRangePickerState()
        DatePickerDialog(onDismissRequest = { showDatePicker = false }, confirmButton = {
            TextButton(onClick = { if (state.selectedStartDateMillis != null && state.selectedEndDateMillis != null) selectedDateRange = state.selectedStartDateMillis to state.selectedEndDateMillis; showDatePicker = false }) { Text("Apply") }
        }) { DateRangePicker(state = state) }
    }
}