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
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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

    // [BARU] Input Diskon State
    var discountInput by remember { mutableStateOf(if(viewModel.discountAmount > 0) viewModel.discountAmount.toInt().toString() else "") }

    val paymentMethod = viewModel.paymentMethod
    val amountPaid = viewModel.amountPaidInput
    val change = viewModel.changeAmount
    val isPaymentValid = (amountPaid.toDoubleOrNull() ?: 0.0) >= total

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Checkout", style = MaterialTheme.typography.titleLarge) },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = TextPrimary) } },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = White)
            )
        },
        bottomBar = {
            Surface(
                shadowElevation = 20.dp,
                color = White,
                shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(24.dp)) {

                    Text("Payment Method", style = MaterialTheme.typography.labelMedium, color = TextSecondary)
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        PaymentOptionButton("Cash", Icons.Default.Money, paymentMethod == "CASH", { viewModel.setPaymentType("CASH") }, Modifier.weight(1f))
                        PaymentOptionButton("QRIS", Icons.Default.QrCode, paymentMethod == "QRIS", { viewModel.setPaymentType("QRIS") }, Modifier.weight(1f))
                        PaymentOptionButton("Transfer", Icons.Default.AccountBalance, paymentMethod == "TRANSFER", { viewModel.setPaymentType("TRANSFER") }, Modifier.weight(1f))
                    }
                    Spacer(modifier = Modifier.height(16.dp))

                    // [BARU] INPUT DISKON
                    OutlinedTextField(
                        value = discountInput,
                        onValueChange = {
                            discountInput = it
                            viewModel.setDiscount(it.toDoubleOrNull() ?: 0.0)
                        },
                        label = { Text("Diskon (Rp)") },
                        prefix = { Text("Rp ") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = BrandOrange,
                            focusedLabelColor = BrandOrange
                        )
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        OutlinedTextField(
                            value = amountPaid,
                            onValueChange = { viewModel.amountPaidInput = it },
                            label = { Text("Cash Received") },
                            prefix = { Text("Rp ") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = BrandGreen, focusedLabelColor = BrandGreen)
                        )
                        Column(
                            modifier = Modifier.weight(0.8f).height(56.dp).background(BackgroundApp, RoundedCornerShape(12.dp)).padding(horizontal = 12.dp),
                            verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.End
                        ) {
                            Text("Kembali", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                            Text("Rp${"%.0f".format(change)}", style = MaterialTheme.typography.titleMedium, color = if(change >= 0) BrandGreen else SystemRed, fontWeight = FontWeight.Bold)
                        }
                    }

                    Divider(modifier = Modifier.padding(vertical = 16.dp), color = BorderColor)

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Column {
                            Text("Grand Total", style = MaterialTheme.typography.titleMedium, color = TextPrimary)
                            // [BARU] Info Diskon
                            if (viewModel.discountAmount > 0) {
                                Text("(Hemat Rp${"%.0f".format(viewModel.discountAmount)})", style = MaterialTheme.typography.labelSmall, color = BrandOrange)
                            }
                        }
                        Text("Rp${"%.2f".format(total)}", style = MaterialTheme.typography.headlineSmall, color = BrandGreen, fontWeight = FontWeight.Bold)
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = { viewModel.checkout() },
                        modifier = Modifier.fillMaxWidth().height(56.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = BrandGreen, disabledContainerColor = TextTertiary),
                        enabled = cart.isNotEmpty() && isPaymentValid
                    ) {
                        Text(if (isPaymentValid) "CONFIRM PAYMENT" else "INSUFFICIENT AMOUNT", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                    }
                }
            }
        },
        containerColor = BackgroundApp
    ) { padding ->
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

    if (receipt != null) {
        val context = LocalContext.current
        AlertDialog(
            onDismissRequest = { viewModel.closeReceipt() },
            containerColor = White,
            title = { Text("Transaction Success", style = MaterialTheme.typography.titleLarge, color = BrandGreen) },
            text = {
                Surface(color = BackgroundApp, shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth().heightIn(max = 400.dp)) {
                    Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                        Text(receipt!!, fontFamily = FontFamily.Monospace, modifier = Modifier.padding(16.dp), color = TextPrimary, style = MaterialTheme.typography.bodySmall, fontSize = 12.sp)
                    }
                }
            },
            confirmButton = {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = {
                            val intent = android.content.Intent(android.content.Intent.ACTION_SEND).apply {
                                type = "text/plain"
                                putExtra(android.content.Intent.EXTRA_TEXT, receipt)
                            }
                            context.startActivity(android.content.Intent.createChooser(intent, "Print Receipt"))
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = BrandBlue)
                    ) {
                        Icon(Icons.Default.Print, null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Print")
                    }
                    Button(onClick = { viewModel.closeReceipt(); onBack() }, colors = ButtonDefaults.buttonColors(containerColor = BrandGreen)) { Text("Close") }
                }
            }
        )
    }
}

// ... (PaymentOptionButton & CartItemCard sama seperti sebelumnya) ...
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