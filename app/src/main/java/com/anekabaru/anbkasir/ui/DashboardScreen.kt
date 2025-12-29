package com.anekabaru.anbkasir.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.anekabaru.anbkasir.ui.theme.*

@Composable
fun DashboardScreen(
    role: String,
    onNav: (String) -> Unit,
    onLogout: () -> Unit
) {
    // Apply background from Theme
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp)
        ) {
            Spacer(modifier = Modifier.height(20.dp))

            // Header Text using Theme Typography
            Text(
                text = "Dashboard",
                style = MaterialTheme.typography.headlineLarge,
                color = MaterialTheme.colorScheme.onBackground
            )

            // Role Indicator
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(top = 4.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(BrandGreen) // Semantic color
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = role,
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(40.dp))

            // Clean Action Cards using Theme Colors
            CleanActionCard(
                title = "Point of Sale",
                description = "Process transactions and payments",
                icon = Icons.Default.ShoppingCart,
                accentColor = BrandGreen, // Or MaterialTheme.colorScheme.primary
                onClick = { onNav(Routes.POS) }
            )

            if (role == "Owner") {
                Spacer(modifier = Modifier.height(16.dp))

                CleanActionCard(
                    title = "Inventory",
                    description = "Manage products and stock levels",
                    icon = Icons.Default.List,
                    accentColor = BrandBlue, // Or MaterialTheme.colorScheme.secondary
                    onClick = { onNav(Routes.INVENTORY) }
                )

                Spacer(modifier = Modifier.height(16.dp))

                CleanActionCard(
                    title = "Reports",
                    description = "Analytics and business insights",
                    icon = Icons.Default.Info,
                    accentColor = BrandOrange, // Or MaterialTheme.colorScheme.tertiary
                    onClick = { onNav(Routes.REPORTS) }
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            // Minimal Logout Button
            TextButton(
                onClick = onLogout,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                colors = ButtonDefaults.textButtonColors(
                    contentColor = MaterialTheme.colorScheme.error
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ExitToApp,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Sign Out",
                    style = MaterialTheme.typography.labelLarge
                )
            }
        }
    }
}

@Composable
fun CleanActionCard(
    title: String,
    description: String,
    icon: ImageVector,
    accentColor: Color,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(88.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 0.dp,
            pressedElevation = 2.dp
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icon Container
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(accentColor.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = accentColor,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Text Content
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                  Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextTertiary // Custom color from Color.kt for specific shade
                )
            }

            // Arrow
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = DividerColor, // Custom color from Color.kt
                modifier = Modifier.size(24.dp)
            )
        }
    }
}