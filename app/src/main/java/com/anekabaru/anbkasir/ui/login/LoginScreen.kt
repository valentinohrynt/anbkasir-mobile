package com.anekabaru.anbkasir.ui.login

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Store
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.anekabaru.anbkasir.ui.PosViewModel
import com.anekabaru.anbkasir.ui.theme.* // Import Theme

@Composable
fun LoginScreen(
    onLoginSuccess: () -> Unit,
    viewModel: PosViewModel
) {
    var pin by remember { mutableStateOf("") }
    var error by remember { mutableStateOf("") }

    // Full screen container with theme background
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.Center
    ) {
        // Card-like container for the form
        Card(
            modifier = Modifier
                .fillMaxWidth(0.85f) // Occupy 85% of screen width
                .wrapContentHeight(),
            colors = CardDefaults.cardColors(containerColor = White),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp), // Flat modern look
            shape = RoundedCornerShape(24.dp)
        ) {
            Column(
                modifier = Modifier.padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // 1. Icon / Logo Placeholder
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = BrandGreen.copy(alpha = 0.1f),
                    modifier = Modifier.size(80.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.Store,
                            contentDescription = null,
                            tint = BrandGreen,
                            modifier = Modifier.size(40.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // 2. Title & Subtitle
                Text(
                    text = "Toko Aneka Baru",
                    style = MaterialTheme.typography.headlineLarge.copy(fontSize = 24.sp),
                    color = TextPrimary,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Please enter your staff PIN\nto access the POS terminal.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(32.dp))

                // 3. PIN Input
                OutlinedTextField(
                    value = pin,
                    onValueChange = {
                        if (it.length <= 4) pin = it
                        error = ""
                    },
                    label = { Text("Access PIN") },
                    leadingIcon = {
                        Icon(Icons.Default.Lock, null, tint = TextTertiary)
                    },
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedBorderColor = DividerColor,
                        focusedBorderColor = BrandGreen,
                        focusedLabelColor = BrandGreen,
                        cursorColor = BrandGreen
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                // Error Message
                if (error.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = error,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.error
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                // 4. Login Button
                Button(
                    onClick = {
                        if (viewModel.login(pin)) {
                            onLoginSuccess()
                        } else {
                            error = "Invalid PIN. Please try again."
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = BrandGreen
                    )
                ) {
                    Text(
                        text = "Access Dashboard",
                        style = MaterialTheme.typography.labelLarge.copy(fontSize = 16.sp)
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                // 5. Hint (Optional, kept discrete)
                Surface(
                    color = BackgroundGray,
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = "Demo: 1234 (Cashier) • 9999 (Owner)",
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        style = MaterialTheme.typography.labelSmall,
                        color = TextTertiary
                    )
                }
            }
        }
    }
}