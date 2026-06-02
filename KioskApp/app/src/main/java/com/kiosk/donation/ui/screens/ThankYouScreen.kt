package com.kiosk.donation.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.kiosk.donation.ui.theme.*
import kotlinx.coroutines.delay
import java.math.BigDecimal

@Composable
fun ThankYouScreen(
    amountGBP: BigDecimal?,
    isDonation: Boolean,
    orgName: String,
    onDone: () -> Unit         // Called automatically after 5 seconds
) {
    // Auto-return to home after 6 seconds
    LaunchedEffect(Unit) {
        delay(6_000)
        onDone()
    }

    val infiniteTransition = rememberInfiniteTransition(label = "check")
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.08f,
        animationSpec = infiniteRepeatable(
            tween(900, easing = EaseInOut),
            RepeatMode.Reverse
        ),
        label = "pulse"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(listOf(KarimaDark, KarimaGreen))
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(48.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .scale(scale)
                    .background(TransparentWhite, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.CheckCircle,
                    contentDescription = null,
                    tint = SurfaceWhite,
                    modifier = Modifier.size(80.dp)
                )
            }

            Spacer(Modifier.height(32.dp))

            Text(
                text = if (isDonation) "Thank You!" else "Payment Complete!",
                style = MaterialTheme.typography.displayMedium,
                color = SurfaceWhite,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )

            Spacer(Modifier.height(16.dp))

            if (amountGBP != null) {
                Text(
                    text = "£${"%.2f".format(amountGBP)}",
                    style = MaterialTheme.typography.displayLarge,
                    color = Amber,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.height(8.dp))
            }

            Text(
                text = if (isDonation)
                    "Your generosity helps $orgName make a real difference."
                else
                    "Thank you for your purchase and support.",
                style = MaterialTheme.typography.titleLarge,
                color = SoftWhite,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 32.dp)
            )

            Spacer(Modifier.height(48.dp))

            // Countdown indicator
            CountdownDots(totalSeconds = 6)

            Spacer(Modifier.height(16.dp))

            Text(
                text = "Returning to start…",
                style = MaterialTheme.typography.bodyMedium,
                color = FaintWhite
            )
        }
    }
}

@Composable
private fun CountdownDots(totalSeconds: Int) {
    var remaining by remember { mutableIntStateOf(totalSeconds) }
    LaunchedEffect(Unit) {
        while (remaining > 0) {
            delay(1_000)
            remaining--
        }
    }
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        repeat(totalSeconds) { idx ->
            val filled = idx < remaining
            Box(
                modifier = Modifier
                    .size(12.dp)
                    .background(
                        if (filled) SurfaceWhite else DimWhite,
                        CircleShape
                    )
            )
        }
    }
}
