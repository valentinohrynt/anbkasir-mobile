package com.anekabaru.anbkasir.ui.pos

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.anekabaru.anbkasir.ui.CartItem
import com.anekabaru.anbkasir.ui.PosViewModel
import com.anekabaru.anbkasir.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CartScreen(viewModel: PosViewModel, onBack: () -> Unit) {
    val cart by viewModel.cart.collectAsState()
    val total by viewModel.grandTotal.collectAsState()
    val receipt by viewModel.receiptText.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Review Order", style = MaterialTheme.typography.titleLarge) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = White)
            )
        },
        bottomBar = {
            // Checkout Section fixed at bottom
            Surface(
                shadowElevation = 16.dp,
                color = White,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Total Amount", style = MaterialTheme.typography.titleMedium, color = TextSecondary)
                        Text(
                            "Rp${"%.2f".format(total)}",
                            style = MaterialTheme.typography.headlineMedium,
                            color = BrandGreen,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = { viewModel.checkout() },
                        modifier = Modifier.fillMaxWidth().height(56.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = BrandGreen),
                        enabled = cart.isNotEmpty()
                    ) {
                        Text("CONFIRM PAYMENT", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    }
                }
            }
        },
        containerColor = BackgroundGray
    ) { padding ->
        if (cart.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Text("Cart is empty", color = TextSecondary)
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding).padding(horizontal = 16.dp),
                contentPadding = PaddingValues(top = 16.dp, bottom = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(cart) { item ->
                    CartItemCard(item)
                }
            }
        }
    }

    // Receipt Dialog (Same logic as before)
    if (receipt != null) {
        AlertDialog(
            onDismissRequest = { viewModel.closeReceipt() },
            containerColor = White,
            title = { Text("Transaction Success", color = BrandGreen) },
            text = {
                Surface(color = BackgroundGray, shape = RoundedCornerShape(8.dp), modifier = Modifier.fillMaxWidth()) {
                    Text(receipt!!, fontFamily = FontFamily.Monospace, modifier = Modifier.padding(16.dp), color = TextPrimary)
                }
            },
            confirmButton = {
                Button(onClick = {
                    viewModel.closeReceipt()
                    onBack() // Go back to products after success
                }, colors = ButtonDefaults.buttonColors(containerColor = BrandGreen)) {
                    Text("Close")
                }
            }
        )
    }
}

@Composable
fun CartItemCard(item: CartItem) {
    Card(
        colors = CardDefaults.cardColors(containerColor = White),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                color = BackgroundGray,
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.size(40.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text("${item.quantity}x", fontWeight = FontWeight.Bold, color = TextPrimary)
                }
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(item.product.name, style = MaterialTheme.typography.titleMedium, color = TextPrimary)
                Text("@ Rp${item.activePrice}", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
            }
            Text("Rp${item.total}", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = TextPrimary)
        }
    }
}