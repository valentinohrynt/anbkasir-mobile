package com.anekabaru.anbkasir.ui.admin

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import com.anekabaru.anbkasir.data.TransactionItemEntity
import com.anekabaru.anbkasir.ui.PosViewModel
import com.anekabaru.anbkasir.ui.theme.*
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionDetailScreen(
    viewModel: PosViewModel,
    onBack: () -> Unit
) {
    val transaction = viewModel.selectedTransaction
    val items = viewModel.selectedTransactionItems

    if (transaction == null) {
        onBack()
        return
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Transaction Details", style = MaterialTheme.typography.titleLarge) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = TextPrimary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = White)
            )
        },
        containerColor = BackgroundApp
    ) { padding ->
        Column(modifier = Modifier.padding(padding).padding(20.dp)) {
            Card(
                colors = CardDefaults.cardColors(containerColor = White),
                shape = RoundedCornerShape(12.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Receipt, null, tint = BrandBlue, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("#${transaction.id.take(8).uppercase()}", style = MaterialTheme.typography.titleMedium, fontFamily = FontFamily.Monospace)
                    }
                    Divider(color = BorderColor)
                    Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                        Text("Date", style = MaterialTheme.typography.bodyMedium)
                        Text(
                            SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault()).format(Date(transaction.date)),
                            style = MaterialTheme.typography.titleSmall
                        )
                    }
                    Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                        Text("Cashier", style = MaterialTheme.typography.bodyMedium)
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Person, null, tint = TextTertiary, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(transaction.cashierName, style = MaterialTheme.typography.titleSmall)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Text("Items Purchased", style = MaterialTheme.typography.titleMedium, color = TextPrimary)
            Spacer(modifier = Modifier.height(8.dp))

            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(items) { item ->
                    TransactionItemRow(item)
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Surface(
                color = White,
                shape = RoundedCornerShape(16.dp),
                shadowElevation = 4.dp
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(20.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Total Amount", color = TextSecondary, style = MaterialTheme.typography.labelSmall)
                        Text(
                            "Rp${"%.2f".format(transaction.totalAmount)}",
                            color = BrandGreen,
                            style = MaterialTheme.typography.headlineSmall
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun TransactionItemRow(item: TransactionItemEntity) {
    Card(
        colors = CardDefaults.cardColors(containerColor = White),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                color = BackgroundApp,
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.size(40.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text("${item.quantity}x", style = MaterialTheme.typography.labelLarge, color = TextPrimary)
                }
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(item.productName, style = MaterialTheme.typography.titleSmall)
                Text("@ Rp${item.priceSnapshot}", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
            }
            Text("Rp${item.subtotal}", style = MaterialTheme.typography.titleSmall, color = TextPrimary)
        }
    }
}