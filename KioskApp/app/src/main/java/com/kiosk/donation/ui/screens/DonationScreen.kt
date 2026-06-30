package com.kiosk.donation.ui.screens

import android.annotation.SuppressLint
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kiosk.donation.data.DONATION_PRESETS
import com.kiosk.donation.ui.theme.*
import kotlinx.coroutines.delay
import java.math.BigDecimal

@SuppressLint("UnusedBoxWithConstraintsScope")
@Composable
fun DonationScreen(
    isOnline: Boolean,
    onBack: () -> Unit,
    onProceedToPayment: (BigDecimal) -> Unit,
    timeoutSeconds: Int = 30
) {
    var selectedAmount by remember { mutableStateOf<BigDecimal?>(null) }
    var customInput    by remember { mutableStateOf("") }
    var showCustom     by remember { mutableStateOf(false) }
    var showNoConnectionError by remember { mutableStateOf(false) }

    // Auto-return to home after inactivity
    LaunchedEffect(selectedAmount, customInput, showCustom) {
        delay(timeoutSeconds * 1000L)
        onBack()
    }

    val effectiveAmount: BigDecimal? = when {
        showCustom -> {
            val pence = customInput.filter { it.isDigit() }.toLongOrNull() ?: 0L
            if (pence > 0) (pence.toBigDecimal()).divide(BigDecimal("100")) else null
        }
        else -> selectedAmount
    }

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(OffWhite)
    ) {
        val screenHeight = maxHeight
        val screenWidth  = maxWidth
        val keypadVisible = showCustom

        // Proportional sizing based on screen dimensions
        val headerHeight:   Dp       = screenHeight * if (keypadVisible) 0.10f else 0.13f
        val chipHeight:     Dp       = screenHeight * if (keypadVisible) 0.09f else 0.12f
        val toggleHeight:   Dp       = screenHeight * if (keypadVisible) 0.08f else 0.10f
        val keyHeight:      Dp       = screenHeight * 0.09f
        val donateHeight:   Dp       = screenHeight * 0.11f // Fixed height to match both screens
        val sectionGap:     Dp       = screenHeight * if (keypadVisible) 0.01f else 0.025f
        val chipFontSize:   TextUnit = (screenWidth.value * if (keypadVisible) 0.035f else 0.055f).sp
        val keyFontSize:    TextUnit = (screenWidth.value * 0.045f).sp
        val amountFontSize: TextUnit = (screenWidth.value * 0.06f).sp
        val hPad:           Dp       = screenWidth  * 0.04f

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = hPad),
            verticalArrangement = Arrangement.Top // Changed to Top to pull everything up
        ) {
            // ── Header Section ────────────────────────────────────────────────
            Column(verticalArrangement = Arrangement.spacedBy(sectionGap)) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(headerHeight),
                    contentAlignment = Alignment.Center
                ) {
                    IconButton(
                        onClick = onBack, 
                        modifier = Modifier
                            .size(headerHeight * 0.7f)
                            .align(Alignment.CenterStart)
                    ) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = KarimaDark,
                            modifier = Modifier.fillMaxSize(0.7f)
                        )
                    }
                    
                    Text(
                        "Choose Your Donation",
                        fontSize   = (screenWidth.value * 0.038f).sp,
                        fontWeight = FontWeight.Bold,
                        color      = TextDark,
                        textAlign  = TextAlign.Center
                    )
                }

                if (!keypadVisible) {
                    Spacer(Modifier.height(24.dp)) // Reduced from 48dp to move subtitle up
                    Text(
                        "Every donation makes a difference — Jazakum Allahu Khayran",
                        fontSize = (screenWidth.value * 0.026f).sp,
                        color    = TextMedium,
                        lineHeight = (screenWidth.value * 0.032f).sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(Modifier.height(sectionGap)) // Reduced spacing
                }

                // "Back to preset amounts" button
                if (showCustom) {
                    OutlinedButton(
                        onClick = {
                            showCustom     = false
                            selectedAmount = null
                            customInput    = ""
                        },
                        shape  = MaterialTheme.shapes.medium,
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = KarimaDark,
                            contentColor   = SurfaceWhite
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(toggleHeight)
                    ) {
                        Text(
                            "Back to preset amounts",
                            fontSize   = (screenWidth.value * 0.035f).sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            // ── Main Content Section ──────────────────────────────────────────
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.Top
            ) {
                if (!showCustom) {
                    // Preset Amounts Mode
                    Column(
                        verticalArrangement = Arrangement.Top,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Spacer(Modifier.height(sectionGap))
                        DONATION_PRESETS.chunked(2).forEach { row ->
                            Row(
                                modifier              = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(sectionGap)
                            ) {
                                row.forEach { amount ->
                                    DonationChip(
                                        amount   = amount,
                                        selected = selectedAmount == amount,
                                        height   = chipHeight,
                                        fontSize = (screenWidth.value * if (keypadVisible) 0.045f else 0.065f).sp, // Increased font size
                                        modifier = Modifier.weight(1f),
                                        onClick  = {
                                            selectedAmount = amount
                                            showCustom     = false
                                            customInput    = ""
                                        }
                                    )
                                }
                                repeat(2 - row.size) { Spacer(Modifier.weight(1f)) }
                            }
                            Spacer(Modifier.height(sectionGap))
                        }

                        // "Enter a different amount" button
                        OutlinedButton(
                            onClick = {
                                showCustom     = true
                                selectedAmount = null
                                customInput    = ""
                            },
                            shape  = MaterialTheme.shapes.medium,
                            colors = ButtonDefaults.outlinedButtonColors(
                                containerColor = Color.Transparent,
                                contentColor   = KarimaDark
                            ),
                            modifier = Modifier.fillMaxWidth().height(toggleHeight)
                        ) {
                            Text(
                                "Enter a different amount",
                                fontSize   = (screenWidth.value * 0.035f).sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                } else {
                    // Keypad Section
                    Column(
                        verticalArrangement = Arrangement.Top,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Spacer(Modifier.height(24.dp)) // Reduced top gap to move amount UP

                        // Amount display
                        Text(
                            text       = "£${"%.2f".format(
                                (customInput.filter { it.isDigit() }.toLongOrNull() ?: 0L) / 100.0
                            )}",
                            fontSize   = amountFontSize,
                            fontWeight = FontWeight.Bold,
                            color      = TextDark,
                            modifier   = Modifier.align(Alignment.CenterHorizontally)
                        )

                        Spacer(Modifier.height(48.dp)) // Kept gap below amount text

                        // Numeric keypad
                        val keys = listOf("1","2","3","4","5","6","7","8","9","00","0","⌫")
                        Column(verticalArrangement = Arrangement.spacedBy(sectionGap)) {
                            keys.chunked(3).forEach { row ->
                                Row(
                                    modifier              = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(sectionGap)
                                ) {
                                    row.forEach { key ->
                                        if (key.isEmpty()) {
                                            Spacer(Modifier.weight(1f))
                                        } else {
                                            OutlinedButton(
                                                onClick = {
                                                    if (key == "⌫") {
                                                        if (customInput.isNotEmpty())
                                                            customInput = customInput.dropLast(1)
                                                    } else {
                                                        val newVal = customInput + key
                                                        if (newVal.length <= 6) customInput = newVal
                                                    }
                                                },
                                                shape    = RoundedCornerShape(12.dp),
                                                modifier = Modifier.weight(1f).height(keyHeight),
                                                colors = ButtonDefaults.outlinedButtonColors(
                                                    containerColor = SurfaceWhite,
                                                    contentColor   = TextDark
                                                )
                                            ) {
                                                Text(key, fontSize = if (key == "00") (keyFontSize.value * 0.8f).sp else keyFontSize, fontWeight = FontWeight.Bold)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // ── Donate button ─────────────────────────────────────────────────
            // Added a fixed Spacer and more bottom padding to lift the button and prevent touching
            Spacer(Modifier.height(48.dp)) 
            
            Button(
                onClick        = { 
                    if (isOnline) {
                        effectiveAmount?.let { onProceedToPayment(it) } 
                    } else {
                        showNoConnectionError = true
                    }
                },
                enabled        = effectiveAmount != null,
                colors         = ButtonDefaults.buttonColors(
                    containerColor = Amber,
                    contentColor   = TextDark
                ),
                shape          = MaterialTheme.shapes.large,
                contentPadding = PaddingValues(0.dp),
                modifier       = Modifier
                    .fillMaxWidth()
                    .height(donateHeight)
                    .padding(bottom = 32.dp) // Lifted the button higher
            ) {
                Icon(Icons.Filled.Favorite, null, modifier = Modifier.size(donateHeight * 0.4f))
                Spacer(Modifier.width(8.dp))
                Text(
                    text = if (effectiveAmount != null)
                        "Donate £${"%.2f".format(effectiveAmount)}"
                    else
                        "Select an amount",
                    fontSize   = (screenWidth.value * 0.030f).sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        if (showNoConnectionError) {
            AlertDialog(
                onDismissRequest = { showNoConnectionError = false },
                title = { Text("Connection Lost") },
                text  = { Text("A connection is required to process donations. Please check your Wi-Fi and try again.") },
                confirmButton = {
                    Button(onClick = { showNoConnectionError = false }) { Text("OK") }
                }
            )
        }
    }
}

@Composable
private fun DonationChip(
    amount: BigDecimal,
    selected: Boolean,
    height: Dp,
    fontSize: TextUnit,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .height(height)
            .clip(RoundedCornerShape(14.dp))
            .background(if (selected) KarimaGreen else SurfaceWhite)
            .border(2.dp, if (selected) KarimaDark else BorderColor, RoundedCornerShape(14.dp))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text       = "£${"%.0f".format(amount)}",
            fontSize   = fontSize,
            color      = if (selected) SurfaceWhite else TextDark,
            fontWeight = FontWeight.Bold
        )
    }
}
