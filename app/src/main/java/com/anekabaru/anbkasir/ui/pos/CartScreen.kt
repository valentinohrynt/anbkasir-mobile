package com.anekabaru.anbkasir.ui.pos

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Money
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
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

    // Payment State from ViewModel
    val paymentMethod = viewModel.paymentMethod
    val amountPaid = viewModel.amountPaidInput
    val change = viewModel.changeAmount

    // Check if payment is valid (Paid >= Total)
    val isPaymentValid = (amountPaid.toDoubleOrNull() ?: 0.0) >= total

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Checkout", style = MaterialTheme.typography.titleLarge) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = TextPrimary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = White)
            )
        },
        bottomBar = {
            // PAYMENT & CHECKOUT SECTION
            Surface(
                shadowElevation = 20.dp,
                color = White,
                shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(24.dp)) {

                    // 1. Payment Method Selector
                    Text("Payment Method", style = MaterialTheme.typography.labelMedium, color = TextSecondary)
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        PaymentOptionButton(
                            label = "Cash",
                            icon = Icons.Default.Money,
                            isSelected = paymentMethod == "CASH",
                            onClick = { viewModel.setPaymentType("CASH") },
                            modifier = Modifier.weight(1f)
                        )
                        PaymentOptionButton(
                            label = "QRIS",
                            icon = Icons.Default.QrCode,
                            isSelected = paymentMethod == "QRIS",
                            onClick = { viewModel.setPaymentType("QRIS") },
                            modifier = Modifier.weight(1f)
                        )
                        PaymentOptionButton(
                            label = "Transfer",
                            icon = Icons.Default.AccountBalance,
                            isSelected = paymentMethod == "TRANSFER",
                            onClick = { viewModel.setPaymentType("TRANSFER") },
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // 2. Amount Input & Calculation
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Input Field
                        OutlinedTextField(
                            value = amountPaid,
                            onValueChange = { viewModel.amountPaidInput = it },
                            label = { Text("Cash Received") },
                            prefix = { Text("Rp ") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = BrandGreen,
                                focusedLabelColor = BrandGreen
                            )
                        )

                        // Change Display
                        Column(
                            modifier = Modifier
                                .weight(0.8f)
                                .height(56.dp) // Match TextField height
                                .background(BackgroundApp, RoundedCornerShape(12.dp))
                                .padding(horizontal = 12.dp),
                            verticalArrangement = Arrangement.Center,
                            horizontalAlignment = Alignment.End
                        ) {
                            Text("Change / Kembali", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                            Text(
                                "Rp${"%.0f".format(change)}",
                                style = MaterialTheme.typography.titleMedium,
                                color = if(change >= 0) BrandGreen else SystemRed,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Divider(modifier = Modifier.padding(vertical = 16.dp), color = BorderColor)

                    // 3. Totals & Button
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Grand Total", style = MaterialTheme.typography.titleMedium, color = TextPrimary)
                        Text(
                            "Rp${"%.2f".format(total)}",
                            style = MaterialTheme.typography.headlineSmall,
                            color = BrandGreen,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = { viewModel.checkout() },
                        modifier = Modifier.fillMaxWidth().height(56.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = BrandGreen,
                            disabledContainerColor = TextTertiary
                        ),
                        enabled = cart.isNotEmpty() && isPaymentValid // Only enable if paid enough
                    ) {
                        Text(
                            if (isPaymentValid) "CONFIRM PAYMENT" else "INSUFFICIENT AMOUNT",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                    }
                }
            }
        },
        containerColor = BackgroundApp
    ) { padding ->
        // Cart Items List
        if (cart.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Text("Cart is empty", style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
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

    // Receipt Dialog with Print Capability
    if (receipt != null) {
        val context = LocalContext.current

        AlertDialog(
            onDismissRequest = { viewModel.closeReceipt() },
            containerColor = White,
            title = { Text("Transaction Success", style = MaterialTheme.typography.titleLarge, color = BrandGreen) },
            text = {
                Surface(
                    color = BackgroundApp,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth().heightIn(max = 400.dp) // Limit height for scrolling
                ) {
                    Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                        Text(
                            receipt!!,
                            fontFamily = FontFamily.Monospace,
                            modifier = Modifier.padding(16.dp),
                            color = TextPrimary,
                            style = MaterialTheme.typography.bodySmall,
                            fontSize = 12.sp
                        )
                    }
                }
            },
            confirmButton = {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    // PRINT BUTTON
                    Button(
                        onClick = {
                            val intent = android.content.Intent(android.content.Intent.ACTION_SEND).apply {
                                type = "text/plain"
                                putExtra(android.content.Intent.EXTRA_TEXT, receipt)
                            }
                            // Allows user to select RawBT, Bluetooth Print, or WhatsApp
                            context.startActivity(android.content.Intent.createChooser(intent, "Print Receipt"))
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = BrandBlue)
                    ) {
                        Icon(Icons.Default.Print, null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Print")
                    }

                    // CLOSE BUTTON
                    Button(onClick = {
                        viewModel.closeReceipt()
                        onBack()
                    }, colors = ButtonDefaults.buttonColors(containerColor = BrandGreen)) {
                        Text("Close")
                    }
                }
            }
        )
    }
}

@Composable
fun PaymentOptionButton(
    label: String,
    icon: ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onClick,
        modifier = modifier.height(70.dp),
        shape = RoundedCornerShape(12.dp),
        color = if (isSelected) BrandGreen.copy(alpha = 0.1f) else White,
        border = androidx.compose.foundation.BorderStroke(
            width = if (isSelected) 2.dp else 1.dp,
            color = if (isSelected) BrandGreen else BorderColor
        )
    ) {
        Column(
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (isSelected) BrandGreen else TextSecondary
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = if (isSelected) BrandGreen else TextSecondary,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
            )
        }
    }
}

@Composable
fun CartItemCard(item: CartItem, onPlus: () -> Unit, onMinus: () -> Unit) {
    Card(
        colors = CardDefaults.cardColors(containerColor = White),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.5.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Qty Controls
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.background(BackgroundApp, RoundedCornerShape(8.dp))
            ) {
                IconButton(onClick = onMinus, modifier = Modifier.size(32.dp)) {
                    Icon(Icons.Default.Remove, null, tint = TextSecondary, modifier = Modifier.size(16.dp))
                }
                Text("${item.quantity}", style = MaterialTheme.typography.titleSmall, modifier = Modifier.padding(horizontal = 8.dp))
                IconButton(onClick = onPlus, modifier = Modifier.size(32.dp)) {
                    Icon(Icons.Default.Add, null, tint = BrandGreen, modifier = Modifier.size(16.dp))
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Details
            Column(modifier = Modifier.weight(1f)) {
                Text(item.product.name, style = MaterialTheme.typography.titleSmall, color = TextPrimary)
                Text("@ Rp${item.activePrice}", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
            }

            Text(
                "Rp${item.total}",
                style = MaterialTheme.typography.titleSmall,
                color = BrandGreen,
                fontWeight = FontWeight.Bold
            )
        }
    }
}