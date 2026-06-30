package com.kiosk.donation.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kiosk.donation.R
import com.kiosk.donation.ui.theme.*

@Composable
fun HomeScreen(
    orgName: String,
    currentDateTime: String,
    isDonationsEnabled: Boolean,
    isProductsEnabled: Boolean,
    appVersion: String,
    onDonateClick: () -> Unit,
    onShopClick: () -> Unit,
    onAdminLongPress: () -> Unit
) {
    // We reference R.drawable.background directly. 
    // It must exist in res/drawable for the app to compile with a background image.
    val imageResId = R.drawable.background

    Box(modifier = Modifier.fillMaxSize()) {

        // ── Layer 1: background (image or gradient) ───────────────────────────
        // If you want to use a solid gradient instead, you can set imageResId to null
        // or toggle this logic.
        if (imageResId != 0) {
            Image(
                painter            = painterResource(id = imageResId),
                contentDescription = null,
                contentScale       = ContentScale.Crop,
                modifier           = Modifier.fillMaxSize()
            )
            // Karima scrim over the image.
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                HomeGradientTop,
                                HomeGradientMid,
                                HomeGradientBottom
                            )
                        )
                    )
            )
        } else {
            // Fallback gradient — used when no background image is present
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(KarimaDark, KarimaGreen, HomeGradientSolid)
                        )
                    )
            )
        }

        // ── Layer 2: UI content ───────────────────────────────────────────────
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
        ) {
            Text(
                text = currentDateTime,
                color = SoftWhite,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.align(Alignment.TopEnd)
            )

            // Version number at bottom left
            Text(
                text = appVersion,
                color = SoftWhite,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.align(Alignment.BottomStart)
            )
        }

        Column(
            modifier            = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        ) {
            Spacer(modifier = Modifier.weight(4f)) // Pushed further down

            PulsingHeart()

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text       = orgName,
                style      = MaterialTheme.typography.displayLarge.copy(
                    fontSize = 72.sp,
                    lineHeight = 82.sp
                ),
                color      = SurfaceWhite,
                fontWeight = FontWeight.Bold,
                textAlign  = TextAlign.Center,
                modifier   = Modifier.padding(horizontal = 32.dp)
            )

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text      = "Jazakallah for your support",
                style     = MaterialTheme.typography.displaySmall,
                color     = SoftWhite,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(80.dp))

            Column(
                verticalArrangement = Arrangement.spacedBy(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (isDonationsEnabled) {
                    KioskActionButton(
                        label          = "Donate",
                        icon           = Icons.Filled.Favorite,
                        containerColor = Amber,
                        contentColor   = TextDark,
                        onClick        = onDonateClick
                    )
                }
                if (isProductsEnabled) {
                    KioskActionButton(
                        label          = "Shop",
                        icon           = Icons.Filled.ShoppingCart,
                        containerColor = SurfaceWhite,
                        contentColor   = KarimaDark,
                        onClick        = onShopClick
                    )
                }
            }

            Spacer(modifier = Modifier.weight(1.5f)) // Balance space below
        }

        // ── Layer 3: hidden admin trigger (bottom-right corner, tap 5×) ───────
        AdminTriggerButton(
            modifier   = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp),
            onLongPress = onAdminLongPress
        )
    }
}

@Composable
private fun KioskActionButton(
    label: String,
    icon: ImageVector,
    containerColor: Color,
    contentColor: Color,
    onClick: () -> Unit
) {
    Button(
        onClick        = onClick,
        colors         = ButtonDefaults.buttonColors(
            containerColor = containerColor,
            contentColor   = contentColor
        ),
        shape          = MaterialTheme.shapes.large,
        contentPadding = PaddingValues(horizontal = 56.dp, vertical = 40.dp),
        elevation      = ButtonDefaults.buttonElevation(defaultElevation = 8.dp),
        modifier       = Modifier.width(550.dp)
    ) {
        Icon(imageVector = icon, contentDescription = null, modifier = Modifier.size(56.dp))
        Spacer(modifier = Modifier.width(20.dp))
        Text(text = label, style = MaterialTheme.typography.displayLarge, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun PulsingHeart() {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue  = 1.15f,
        animationSpec = infiniteRepeatable(
            animation  = tween(800, easing = EaseInOut),
            repeatMode = RepeatMode.Reverse
        ),
        label = "heartbeat"
    )
    Box(
        modifier = Modifier
            .size(96.dp)
            .scale(scale)
            .background(White15, CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector        = Icons.Filled.Favorite,
            contentDescription = null,
            tint               = SurfaceWhite,
            modifier           = Modifier.size(56.dp)
        )
    }
}

@Composable
private fun AdminTriggerButton(modifier: Modifier, onLongPress: () -> Unit) {
    var pressCount by remember { mutableIntStateOf(0) }
    TextButton(
        onClick = {
            pressCount++
            if (pressCount >= 5) {
                pressCount = 0
                onLongPress()
            }
        },
        modifier = modifier.size(48.dp),
        colors   = ButtonDefaults.textButtonColors(contentColor = Color.Transparent)
    ) {}
}
