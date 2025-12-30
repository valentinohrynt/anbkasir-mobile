package com.anekabaru.anbkasir.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarData
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.anekabaru.anbkasir.ui.theme.BrandGreen
import com.anekabaru.anbkasir.ui.theme.SystemRed
import com.anekabaru.anbkasir.ui.theme.TextPrimary
import com.anekabaru.anbkasir.ui.theme.White

enum class SnackbarType {
    SUCCESS, ERROR, INFO
}

@Composable
fun AppSnackbar(
    snackbarData: SnackbarData,
    type: SnackbarType
) {
    val backgroundColor = when (type) {
        SnackbarType.SUCCESS -> BrandGreen
        SnackbarType.ERROR -> SystemRed
        SnackbarType.INFO -> TextPrimary
    }

    val icon: ImageVector = when (type) {
        SnackbarType.SUCCESS -> Icons.Default.CheckCircle
        SnackbarType.ERROR -> Icons.Default.Error
        SnackbarType.INFO -> Icons.Default.Info
    }

    Surface(
        modifier = Modifier
            .padding(16.dp)
            .fillMaxWidth(),
        color = backgroundColor,
        shadowElevation = 6.dp,
        shape = RoundedCornerShape(12.dp) // Sudut membulat modern
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = White,
                modifier = Modifier.size(24.dp)
            )

            Text(
                text = snackbarData.visuals.message,
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                color = White
            )
        }
    }
}