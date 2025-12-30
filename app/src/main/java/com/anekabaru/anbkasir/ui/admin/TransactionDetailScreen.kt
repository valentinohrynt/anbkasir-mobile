package com.anekabaru.anbkasir.ui.admin

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.anekabaru.anbkasir.data.TransactionItemEntity
import com.anekabaru.anbkasir.ui.PosViewModel
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
fun TransactionDetailScreen(
    viewModel: PosViewModel,
    onBack: () -> Unit
) {
    val transaction = viewModel.selectedTransaction
    val txItems = viewModel.selectedTransactionItems

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
            // --- HEADER TRANSAKSI ---
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

            // --- LIST ITEM BARANG ---
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(txItems) { item ->
                    TransactionItemRow(item)
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // --- [UPDATE] SUMMARY & PAYMENT DETAILS ---
            // Mengganti Surface biasa dengan Card yang lebih informatif
            Card(
                colors = CardDefaults.cardColors(containerColor = White),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    // Info Diskon (Jika ada)
                    if (transaction.discount > 0) {
                        Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                            Text("Discount", style = MaterialTheme.typography.bodyMedium, color = BrandOrange)
                            Text("-Rp${"%.0f".format(transaction.discount)}", style = MaterialTheme.typography.bodyMedium, color = BrandOrange)
                        }
                    }

                    // Grand Total
                    Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                        Text("Grand Total", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        // Menggunakan %.0f agar konsisten (tanpa koma desimal)
                        Text("Rp${"%.0f".format(transaction.totalAmount)}", style = MaterialTheme.typography.headlineSmall, color = BrandGreen, fontWeight = FontWeight.Bold)
                    }

                    Divider(modifier = Modifier.padding(vertical = 8.dp), color = BorderColor)

                    // Info Pembayaran & Kembalian
                    Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                        Text("Paid via ${transaction.paymentMethod}", style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
                        Text("Rp${"%.0f".format(transaction.amountPaid)}", style = MaterialTheme.typography.bodyMedium, color = TextPrimary)
                    }

                    Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                        Text("Change", style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
                        Text("Rp${"%.0f".format(transaction.changeAmount)}", style = MaterialTheme.typography.bodyMedium, color = TextPrimary)
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
                // Tampilkan Nama Barang + Satuan
                Text("${item.productName} (${item.unit})", style = MaterialTheme.typography.titleSmall)
                Text("@ Rp${"%.0f".format(item.priceSnapshot)}", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
            }
            Text("Rp${"%.0f".format(item.subtotal)}", style = MaterialTheme.typography.titleSmall, color = TextPrimary)
        }
    }
}