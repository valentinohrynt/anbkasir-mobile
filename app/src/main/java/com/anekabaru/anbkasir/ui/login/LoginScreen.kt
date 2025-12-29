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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.anekabaru.anbkasir.ui.PosViewModel
import com.anekabaru.anbkasir.ui.theme.*

@Composable
fun LoginScreen(
    onLoginSuccess: () -> Unit,
    viewModel: PosViewModel
) {
    var pin by remember { mutableStateOf("") }
    var error by remember { mutableStateOf("") }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundApp), // Updated to FAFAFA
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.85f)
                .wrapContentHeight(),
            colors = CardDefaults.cardColors(containerColor = White),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp), // Consistent elevation
            shape = RoundedCornerShape(24.dp)
        ) {
            Column(
                modifier = Modifier.padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = SurfaceGreen, // Updated
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

                Text(
                    text = "Toko Aneka Baru",
                    style = MaterialTheme.typography.headlineSmall,
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

                OutlinedTextField(
                    value = pin,
                    onValueChange = {
                        if (it.length <= 4) pin = it
                        error = ""
                    },
                    label = { Text("Access PIN") },
                    leadingIcon = { Icon(Icons.Default.Lock, null, tint = TextTertiary) },
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = BrandGreen,
                        unfocusedBorderColor = BorderColor, // Updated
                        focusedLabelColor = BrandGreen,
                        cursorColor = BrandGreen,
                        focusedContainerColor = White,
                        unfocusedContainerColor = White
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                if (error.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = error,
                        style = MaterialTheme.typography.labelSmall,
                        color = SystemRed
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

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
                    colors = ButtonDefaults.buttonColors(containerColor = BrandGreen)
                ) {
                    Text(
                        text = "Access Dashboard",
                        style = MaterialTheme.typography.labelLarge
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                Surface(
                    color = BackgroundApp,
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