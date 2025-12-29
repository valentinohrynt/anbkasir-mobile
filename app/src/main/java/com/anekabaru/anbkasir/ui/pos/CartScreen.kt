package com.anekabaru.anbkasir.ui.pos

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
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
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = TextPrimary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = White)
            )
        },
        bottomBar = {
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
                        Text("Total Amount", style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
                        Text(
                            "Rp${"%.2f".format(total)}",
                            style = MaterialTheme.typography.headlineSmall,
                            color = BrandGreen
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
                        Text("CONFIRM PAYMENT", style = MaterialTheme.typography.labelLarge)
                    }
                }
            }
        },
        containerColor = BackgroundApp
    ) { padding ->
        if (cart.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Text("Cart is empty", style = MaterialTheme.typography.bodyMedium)
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding).padding(horizontal = 16.dp),
                contentPadding = PaddingValues(top = 16.dp, bottom = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(cart) { item ->
                    CartItemCard(
                        item = item,
                        onPlus = { viewModel.updateCartQuantity(item.product.id, 1) },
                        onMinus = { viewModel.updateCartQuantity(item.product.id, -1) }
                    )
                }
            }
        }
    }

    if (receipt != null) {
        AlertDialog(
            onDismissRequest = { viewModel.closeReceipt() },
            containerColor = White,
            title = { Text("Transaction Success", style = MaterialTheme.typography.titleLarge, color = BrandGreen) },
            text = {
                Surface(color = BackgroundApp, shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth()) {
                    Text(
                        receipt!!,
                        fontFamily = FontFamily.Monospace,
                        modifier = Modifier.padding(16.dp),
                        color = TextPrimary,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            },
            confirmButton = {
                Button(onClick = {
                    viewModel.closeReceipt()
                    onBack()
                }, colors = ButtonDefaults.buttonColors(containerColor = BrandGreen)) {
                    Text("Close")
                }
            }
        )
    }
}

@Composable
fun CartItemCard(
    item: CartItem,
    onPlus: () -> Unit,
    onMinus: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = White),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.background(BackgroundApp, RoundedCornerShape(8.dp))) {
                IconButton(onClick = onMinus, modifier = Modifier.size(32.dp)) {
                    Icon(Icons.Default.Remove, null, tint = TextSecondary, modifier = Modifier.size(16.dp))
                }
                Text("${item.quantity}", style = MaterialTheme.typography.titleSmall, modifier = Modifier.padding(horizontal = 8.dp))
                IconButton(onClick = onPlus, modifier = Modifier.size(32.dp)) {
                    Icon(Icons.Default.Add, null, tint = BrandGreen, modifier = Modifier.size(16.dp))
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(item.product.name, style = MaterialTheme.typography.titleSmall, color = TextPrimary)
                Text("@ Rp${item.activePrice}", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
            }

            Text(
                "Rp${item.total}",
                style = MaterialTheme.typography.titleSmall,
                color = BrandGreen
            )
        }
    }
}