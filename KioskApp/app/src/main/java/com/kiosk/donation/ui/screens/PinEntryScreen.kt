package com.kiosk.donation.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kiosk.donation.ui.theme.*

/**
 * Full-screen PIN entry — lives inside the NavHost so navigation is reliable.
 * Replaces the AlertDialog approach which had back-press interference issues.
 */
@Composable
fun PinEntryScreen(
    correctPin: String,
    onSuccess: () -> Unit,
    onCancel: () -> Unit
) {
    // Block back press — handle it ourselves via Cancel button
    BackHandler { onCancel() }

    var entered by remember { mutableStateOf("") }
    var shakeError by remember { mutableStateOf(false) }

    val maxLen = correctPin.length.coerceAtLeast(4)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(OverlayScrim),
        contentAlignment = Alignment.Center
    ) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
            elevation = CardDefaults.cardElevation(16.dp),
            modifier = Modifier.width(360.dp)
        ) {
            Column(
                modifier = Modifier.padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Staff Access",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = TextDark
                )

                Spacer(Modifier.height(8.dp))

                Text(
                    text = "Enter admin PIN",
                    style = MaterialTheme.typography.bodyLarge,
                    color = TextMedium
                )

                Spacer(Modifier.height(24.dp))

                // PIN dots display
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.height(20.dp)
                ) {
                    repeat(maxLen) { idx ->
                        Box(
                            modifier = Modifier
                                .size(16.dp)
                                .background(
                                    color = if (idx < entered.length) KarimaDark else BorderColor,
                                    shape = androidx.compose.foundation.shape.CircleShape
                                )
                        )
                    }
                }

                if (shakeError) {
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = "Incorrect PIN — try again",
                        color = ErrorRed,
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center
                    )
                }

                Spacer(Modifier.height(24.dp))

                // Numeric keypad
                val keys = listOf("1","2","3","4","5","6","7","8","9","","0","⌫")
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    keys.chunked(3).forEach { row ->
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            row.forEach { key ->
                                if (key.isEmpty()) {
                                    Spacer(Modifier.weight(1f))
                                } else {
                                    OutlinedButton(
                                        onClick = {
                                            shakeError = false
                                            if (key == "⌫") {
                                                if (entered.isNotEmpty()) entered = entered.dropLast(1)
                                            } else if (entered.length < maxLen) {
                                                val newVal = entered + key
                                                entered = newVal
                                                // Auto-check when enough digits entered
                                                if (newVal.length == correctPin.length) {
                                                    if (newVal == correctPin) {
                                                        onSuccess()
                                                    } else {
                                                        shakeError = true
                                                        entered = ""
                                                    }
                                                }
                                            }
                                        },
                                        shape = RoundedCornerShape(12.dp),
                                        modifier = Modifier
                                            .weight(1f)
                                            .aspectRatio(1.6f)
                                    ) {
                                        Text(
                                            text = key,
                                            fontSize = 22.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(Modifier.height(16.dp))

                TextButton(
                    onClick = onCancel,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Cancel", style = MaterialTheme.typography.titleLarge, color = TextMedium)
                }
            }
        }
    }
}
