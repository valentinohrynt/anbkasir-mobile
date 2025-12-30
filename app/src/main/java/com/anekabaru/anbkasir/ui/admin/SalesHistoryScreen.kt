package com.anekabaru.anbkasir.ui.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.History
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DateRangePicker
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDateRangePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.anekabaru.anbkasir.ui.PosViewModel
import com.anekabaru.anbkasir.ui.components.PullToRefreshLayout
import com.anekabaru.anbkasir.ui.theme.BackgroundApp
import com.anekabaru.anbkasir.ui.theme.BorderColor
import com.anekabaru.anbkasir.ui.theme.BrandBlue
import com.anekabaru.anbkasir.ui.theme.BrandGreen
import com.anekabaru.anbkasir.ui.theme.BrandOrange
import com.anekabaru.anbkasir.ui.theme.TextPrimary
import com.anekabaru.anbkasir.ui.theme.TextSecondary
import com.anekabaru.anbkasir.ui.theme.TextTertiary
import com.anekabaru.anbkasir.ui.theme.White
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

    // Date Filter State
    var showDatePicker by remember { mutableStateOf(false) }
    var selectedDateRange by remember { mutableStateOf<Pair<Long?, Long?>>(null to null) } // Start to End

    // Filter Logic
    val filteredHistory = remember(history, selectedDateRange) {
        val (start, end) = selectedDateRange
        if (start == null || end == null) {
            history
        } else {
            // Normalize to start of day and end of day
            val startDate = Date(start)
            val endDate = Date(end + 86400000 - 1) // Add 24h - 1ms to include the full end day

            history.filter { tx ->
                val txDate = Date(tx.date)
                txDate.after(startDate) && txDate.before(endDate)
            }
        }
    }

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

                // --- CUSTOM HEADER ---
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
                                    "${filteredHistory.size} Transactions",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = TextSecondary
                                )
                            }
                        }

                        // Right: Decorative Icon
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(SurfaceOrange),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.History,
                                contentDescription = null,
                                tint = BrandOrange,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // --- DATE FILTER BUTTON ---
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        OutlinedButton(
                            onClick = { showDatePicker = true },
                            shape = RoundedCornerShape(12.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, BorderColor),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = TextSecondary),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.CalendarToday, null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            val (start, end) = selectedDateRange
                            Text(
                                if (start != null && end != null) {
                                    val f = SimpleDateFormat("dd MMM", Locale.getDefault())
                                    "${f.format(Date(start))} - ${f.format(Date(end))}"
                                } else "Filter by Date Range",
                                style = MaterialTheme.typography.labelMedium
                            )
                        }

                        // Clear Filter Button (Visible if filtered)
                        if (selectedDateRange.first != null) {
                            Spacer(modifier = Modifier.width(8.dp))
                            IconButton(
                                onClick = { selectedDateRange = null to null },
                                modifier = Modifier.background(BackgroundApp, CircleShape)
                            ) {
                                Icon(Icons.Default.Close, "Clear", tint = TextSecondary)
                            }
                        }
                    }
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
                    if (filteredHistory.isEmpty()) {
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
                        items(filteredHistory) { tx ->
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
                                        Column(horizontalAlignment = Alignment.End) {
                                            // [UPDATE] Format uang biar rapi (%.0f)
                                            Text(
                                                "Rp${"%.0f".format(tx.totalAmount)}",
                                                style = MaterialTheme.typography.titleSmall,
                                                color = BrandGreen
                                            )
                                            // Optional: Show Payment Method
                                            Text(
                                                tx.paymentMethod,
                                                style = MaterialTheme.typography.labelSmall,
                                                color = TextTertiary,
                                                fontSize = 10.sp
                                            )
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
        val datePickerState = rememberDateRangePickerState()
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        val start = datePickerState.selectedStartDateMillis
                        val end = datePickerState.selectedEndDateMillis
                        if (start != null && end != null) {
                            selectedDateRange = start to end
                        }
                        showDatePicker = false
                    }
                ) { Text("Apply", color = BrandBlue) }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) { Text("Cancel", color = TextSecondary) }
            }
        ) {
            DateRangePicker(state = datePickerState, title = {
                Text(
                    "Select dates",
                    modifier = Modifier.padding(16.dp),
                    style = MaterialTheme.typography.titleMedium
                )
            })
        }
    }
}