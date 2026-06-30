package com.kiosk.donation.ui.screens

import android.graphics.Bitmap
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.google.zxing.BarcodeFormat
import com.google.zxing.qrcode.QRCodeWriter
import com.kiosk.donation.ui.theme.*
import kotlinx.coroutines.delay
import java.math.BigDecimal

@Composable
fun ThankYouScreen(
    amountGBP: BigDecimal?,
    isDonation: Boolean,
    orgName: String,
    timeoutSeconds: Int,
    txCode: String?,
    txId: String?,
    merchantCode: String?,
    onDone: () -> Unit
) {
    // Auto-return to home after the configured timeout
    LaunchedEffect(Unit) {
        delay(timeoutSeconds * 1000L)
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

    // Generate QR code for the receipt URL using the verified format
    val qrBitmap = remember(txCode, merchantCode) {
        if (txCode != null && !merchantCode.isNullOrBlank()) {
            val url = "https://hi.sumup.com/merchants/$merchantCode/sales/transaction:$txCode?utm_source=email_generic"
            generateQrCode(url, 400)
        } else null
    }

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
                    .size(100.dp)
                    .scale(scale)
                    .background(TransparentWhite, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.CheckCircle,
                    contentDescription = null,
                    tint = SurfaceWhite,
                    modifier = Modifier.size(64.dp)
                )
            }

            Spacer(Modifier.height(16.dp))

            Text(
                text = if (isDonation) "Thank You!" else "Payment Complete!",
                style = MaterialTheme.typography.displayMedium,
                color = SurfaceWhite,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )

            if (amountGBP != null) {
                Spacer(Modifier.height(8.dp))
                Text(
                    text = "£${"%.2f".format(amountGBP)}",
                    style = MaterialTheme.typography.displayLarge,
                    color = Amber,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(Modifier.height(24.dp))

            if (qrBitmap != null) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "Scan for your receipt",
                        style = MaterialTheme.typography.titleLarge,
                        color = SurfaceWhite,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(Modifier.height(16.dp))
                    Box(
                        modifier = Modifier
                            .size(200.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color.White)
                            .padding(12.dp)
                    ) {
                        Image(
                            bitmap = qrBitmap.asImageBitmap(),
                            contentDescription = "Receipt QR Code",
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }
            } else {
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
            }

            Spacer(Modifier.height(32.dp))

            // Countdown indicator
            CountdownDots(totalSeconds = timeoutSeconds)

            Spacer(Modifier.height(16.dp))

            Text(
                text = "Returning to start…",
                style = MaterialTheme.typography.bodyMedium,
                color = FaintWhite
            )

            Spacer(Modifier.height(24.dp))

            Button(
                onClick = onDone,
                colors = ButtonDefaults.buttonColors(containerColor = SurfaceWhite.copy(alpha = 0.2f), contentColor = SurfaceWhite),
                shape = MaterialTheme.shapes.medium
            ) {
                Text("Finish", fontWeight = FontWeight.Bold)
            }
        }
    }
}

private fun generateQrCode(text: String, size: Int): Bitmap? {
    return try {
        val writer = QRCodeWriter()
        val bitMatrix = writer.encode(text, BarcodeFormat.QR_CODE, size, size)
        val width = bitMatrix.width
        val height = bitMatrix.height
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565)
        for (x in 0 until width) {
            for (y in 0 until height) {
                bitmap.setPixel(x, y, if (bitMatrix.get(x, y)) android.graphics.Color.BLACK else android.graphics.Color.WHITE)
            }
        }
        bitmap
    } catch (e: Exception) {
        null
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
