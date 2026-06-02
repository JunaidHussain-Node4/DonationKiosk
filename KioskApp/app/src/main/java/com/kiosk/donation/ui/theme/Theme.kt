package com.kiosk.donation.ui.theme

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// ── Palette ───────────────────────────────────────────────────────────────────
// Warm, welcoming, high-contrast — suitable for public-facing kiosk displays

val KarimaLight       = Color(76, 204, 170)
val KarimaGreen      = Color(26, 154, 120)
val KarimaDark      = Color(26, 154, 120)
val Amber            = Color(0xFFFFC107)
val AmberDark        = Color(0xFFFF8F00)
val OffWhite         = Color(0xFFF5F5F5)
val SurfaceWhite     = Color(0xFFFFFFFF)
val TextDark         = Color(0xFF1A1A2E)
val TextMedium       = Color(0xFF424242)
val ErrorRed         = Color(0xFFD32F2F)
val SuccessGreen     = Color(0xFF388E3C)

// Semantic colors for global use
val DividerColor     = Color(0xFFEEEEEE)
val BorderColor      = Color.LightGray
val TransparentWhite = Color.White.copy(alpha = 0.2f)
val SoftWhite        = Color.White.copy(alpha = 0.85f)
val FaintWhite       = Color.White.copy(alpha = 0.60f)
val DimWhite         = Color.White.copy(alpha = 0.30f)
val White15          = Color.White.copy(alpha = 0.15f)
val OverlayScrim     = Color.Black.copy(alpha = 0.85f)

// Gradient / Background semantic colors
val HomeGradientTop    = KarimaDark.copy(alpha = 0.72f)
val HomeGradientMid    = KarimaGreen.copy(alpha = 0.65f)
val HomeGradientBottom = KarimaLight.copy(alpha = 0.55f)
val HomeGradientSolid  = KarimaLight.copy(alpha = 0.6f)

private val KioskColorScheme = lightColorScheme(
    primary            = KarimaGreen,
    onPrimary          = Color.White,
    primaryContainer   = KarimaLight,
    onPrimaryContainer = TextDark,
    secondary          = Amber,
    onSecondary        = TextDark,
    secondaryContainer = AmberDark,
    onSecondaryContainer = Color.White,
    background         = OffWhite,
    onBackground       = TextDark,
    surface            = SurfaceWhite,
    onSurface          = TextDark,
    error              = ErrorRed,
    onError            = Color.White,
)

@Composable
fun KioskTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = KioskColorScheme,
        typography  = KioskTypography,
        shapes      = KioskShapes,
        content     = content
    )
}
