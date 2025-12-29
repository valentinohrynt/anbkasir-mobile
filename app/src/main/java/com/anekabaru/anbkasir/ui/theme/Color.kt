package com.anekabaru.anbkasir.ui.theme

import androidx.compose.ui.graphics.Color

// Neutral Colors
val BackgroundApp = Color(0xFFFAFAFA) // The signature light gray background
val White = Color(0xFFFFFFFF)
val TextPrimary = Color(0xFF1F1F1F) // Dark Gray for headings
val TextSecondary = Color(0xFF6B7280) // Medium Gray for body
val TextTertiary = Color(0xFF9CA3AF) // Light Gray for placeholders/labels
val BorderColor = Color(0xFFE5E7EB) // Subtle border for TextFields

// Brand Colors
val BrandGreen = Color(0xFF10B981) // Primary Action / POS
val BrandBlue = Color(0xFF3B82F6)  // Inventory / Info
val BrandOrange = Color(0xFFF59E0B) // Warnings / History
val SystemRed = Color(0xFFEF4444)   // Error / Delete

// Semantic Surfaces (Light backgrounds for badges)
val SurfaceGreen = Color(0xFFF0FDF4)
val SurfaceBlue = Color(0xFFEFF6FF)
val SurfaceRed = Color(0xFFFEF2F2)

// Material 3 Mapping
val Primary = BrandGreen
val Secondary = BrandBlue
val Tertiary = BrandOrange
val Error = SystemRed
val Surface = White
val OnSurface = TextPrimary