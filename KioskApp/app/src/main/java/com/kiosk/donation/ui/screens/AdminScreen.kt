package com.kiosk.donation.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.kiosk.donation.data.SumUpManager
import com.kiosk.donation.ui.SyncState
import com.kiosk.donation.ui.theme.*

@Composable
fun AdminPinDialog(
    onCorrectPin: () -> Unit,
    onDismiss: () -> Unit,
    correctPin: String
) {
    var input   by remember { mutableStateOf("") }
    var error   by remember { mutableStateOf(false) }
    var visible by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Staff Access", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold) },
        text = {
            Column {
                Text("Enter the admin PIN to access settings.", style = MaterialTheme.typography.bodyLarge)
                Spacer(Modifier.height(16.dp))
                OutlinedTextField(
                    value       = input,
                    onValueChange = { input = it; error = false },
                    label       = { Text("PIN") },
                    isError     = error,
                    supportingText = { if (error) Text("Incorrect PIN") },
                    visualTransformation = if (visible) VisualTransformation.None else PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                    trailingIcon = {
                        IconButton(onClick = { visible = !visible }) {
                            Icon(if (visible) Icons.Filled.VisibilityOff else Icons.Filled.Visibility, null)
                        }
                    },
                    singleLine = true
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { if (input == correctPin) onCorrectPin() else { error = true; input = "" } },
                colors  = ButtonDefaults.buttonColors(containerColor = KarimaGreen)
            ) { Text("Unlock") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}

@Composable
fun AdminScreen(
    currentOrgName: String,
    currentSumupKey: String,
    isDeviceOwner: Boolean,
    isLoggedInToSumup: Boolean,
    syncState: SyncState,
    productCount: Int,
    isDonationsEnabled: Boolean,
    isProductsEnabled: Boolean,
    basketTimeout: Int,
    onSaveOrgName: (String) -> Unit,
    onSaveSumupKey: (String) -> Unit,
    onSetDonationsEnabled: (Boolean) -> Unit,
    onSetProductsEnabled: (Boolean) -> Unit,
    onSetBasketTimeout: (Int) -> Unit,
    onChangePinClick: () -> Unit,
    onSumupLogin: () -> Unit,
    onSumupLogout: () -> Unit,
    onSyncProducts: () -> Unit,
    onExitKiosk: () -> Unit,
    onCloseApp: () -> Unit,
    onBack: () -> Unit
) {
    var orgName        by remember(currentOrgName)  { mutableStateOf(currentOrgName) }
    var sumupKey       by remember(currentSumupKey) { mutableStateOf(currentSumupKey) }
    var showSumupKey   by remember { mutableStateOf(false) }
    var showExitConfirm by remember { mutableStateOf(false) }
    var showCloseConfirm by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(32.dp)
    ) {
        // Header
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = KarimaDark, modifier = Modifier.size(28.dp))
            }
            Spacer(Modifier.width(8.dp))
            Text("Admin Settings", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        }

        Spacer(Modifier.height(24.dp))

        // ── Organisation ──────────────────────────────────────────────────────
        AdminSection("Organisation") {
            OutlinedTextField(
                value         = orgName,
                onValueChange = { orgName = it },
                label         = { Text("Organisation Name") },
                modifier      = Modifier.fillMaxWidth(),
                singleLine    = true
            )
            Spacer(Modifier.height(8.dp))
            Button(onClick = { onSaveOrgName(orgName) }, colors = ButtonDefaults.buttonColors(containerColor = KarimaGreen)) {
                Text("Save Name")
            }
        }

        Spacer(Modifier.height(24.dp))

        // ── Features ──────────────────────────────────────────────────────────
        AdminSection("Enable/Disable Features") {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Show Donation Button", style = MaterialTheme.typography.bodyLarge)
                Switch(
                    checked = isDonationsEnabled,
                    onCheckedChange = onSetDonationsEnabled,
                    colors = SwitchDefaults.colors(checkedThumbColor = KarimaGreen, checkedTrackColor = KarimaGreen.copy(alpha = 0.5f))
                )
            }
            Spacer(Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Show Product Shop Button", style = MaterialTheme.typography.bodyLarge)
                Switch(
                    checked = isProductsEnabled,
                    onCheckedChange = onSetProductsEnabled,
                    colors = SwitchDefaults.colors(checkedThumbColor = KarimaGreen, checkedTrackColor = KarimaGreen.copy(alpha = 0.5f))
                )
            }
            Spacer(Modifier.height(16.dp))
            OutlinedTextField(
                value = basketTimeout.toString(),
                onValueChange = { newValue ->
                    newValue.toIntOrNull()?.let { onSetBasketTimeout(it) }
                },
                label = { Text("Cart Timeout (Minutes)") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                supportingText = { Text("Clears the cart after this many minutes of inactivity") }
            )
        }

        Spacer(Modifier.height(24.dp))

        // ── Products ──────────────────────────────────────────────────────────
        AdminSection("Products") {
            Text(
                text  = if (productCount > 0) "✅ $productCount products loaded" else "⚠️ No products — sync from Firebase to load",
                style = MaterialTheme.typography.bodyMedium,
                color = if (productCount > 0) SuccessGreen else ErrorRed
            )
            Spacer(Modifier.height(8.dp))

            Button(
                onClick  = onSyncProducts,
                enabled  = syncState !is SyncState.Syncing,
                colors   = ButtonDefaults.buttonColors(containerColor = KarimaDark),
                modifier = Modifier.fillMaxWidth()
            ) {
                if (syncState is SyncState.Syncing) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        color    = SurfaceWhite,
                        strokeWidth = 2.dp
                    )
                    Spacer(Modifier.width(8.dp))
                    Text("Syncing…")
                } else {
                    Icon(Icons.Filled.Sync, null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Sync Products from Firebase")
                }
            }

            // Show sync result
            when (syncState) {
                is SyncState.Success -> {
                    Spacer(Modifier.height(6.dp))
                    Text(syncState.message, style = MaterialTheme.typography.bodyMedium, color = SuccessGreen)
                }
                is SyncState.Failed -> {
                    Spacer(Modifier.height(6.dp))
                    Text(syncState.message, style = MaterialTheme.typography.bodyMedium, color = ErrorRed)
                }
                else -> {}
            }

            Spacer(Modifier.height(4.dp))
            Text(
                "Manage products at: karimakioskdonation.web.app",
                style = MaterialTheme.typography.bodySmall,
                color = TextMedium
            )
        }

        Spacer(Modifier.height(24.dp))

        // ── SumUp ─────────────────────────────────────────────────────────────
        AdminSection("SumUp Payment") {
            OutlinedTextField(
                value         = sumupKey,
                onValueChange = { sumupKey = it },
                label         = { Text("Affiliate Key") },
                visualTransformation = if (showSumupKey) VisualTransformation.None else PasswordVisualTransformation(),
                trailingIcon  = {
                    IconButton(onClick = { showSumupKey = !showSumupKey }) {
                        Icon(if (showSumupKey) Icons.Filled.VisibilityOff else Icons.Filled.Visibility, null)
                    }
                },
                modifier   = Modifier.fillMaxWidth(),
                singleLine = true
            )
            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Button(onClick = { onSaveSumupKey(sumupKey) }, colors = ButtonDefaults.buttonColors(containerColor = KarimaGreen)) {
                    Text("Save Key")
                }
                if (isLoggedInToSumup) {
                    OutlinedButton(onClick = onSumupLogout) { Text("Log Out") }
                } else {
                    Button(onClick = onSumupLogin, colors = ButtonDefaults.buttonColors(containerColor = KarimaDark)) {
                        Text("Log In to SumUp")
                    }
                }
            }
            Text(
                text  = if (isLoggedInToSumup) "✅ Logged in to SumUp" else "⚠️ Not logged in",
                style = MaterialTheme.typography.bodyMedium,
                color = if (isLoggedInToSumup) SuccessGreen else ErrorRed
            )
        }

        Spacer(Modifier.height(24.dp))

        // ── Security ──────────────────────────────────────────────────────────
        AdminSection("Security") {
            Button(onClick = onChangePinClick, colors = ButtonDefaults.buttonColors(containerColor = KarimaGreen)) {
                Text("Change Admin PIN")
            }
            Spacer(Modifier.height(8.dp))
            Text(
                text  = if (isDeviceOwner) "✅ Device Owner mode active (full kiosk lock)" else "⚠️ Not Device Owner — kiosk lock is partial",
                style = MaterialTheme.typography.bodyMedium,
                color = if (isDeviceOwner) SuccessGreen else ErrorRed
            )
        }

        Spacer(Modifier.height(32.dp))

        // ── Exit kiosk ────────────────────────────────────────────────────────
        Button(
            onClick  = { showExitConfirm = true },
            colors   = ButtonDefaults.buttonColors(containerColor = ErrorRed),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Exit Kiosk Mode", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        }

        Spacer(Modifier.height(16.dp))

        // ── Close App ─────────────────────────────────────────────────────────
        OutlinedButton(
            onClick  = { showCloseConfirm = true },
            modifier = Modifier.fillMaxWidth(),
            colors   = ButtonDefaults.outlinedButtonColors(contentColor = ErrorRed),
            border   = androidx.compose.foundation.BorderStroke(1.dp, ErrorRed)
        ) {
            Text("Close App", style = MaterialTheme.typography.titleMedium)
        }
    }

    if (showExitConfirm) {
        AlertDialog(
            onDismissRequest = { showExitConfirm = false },
            title = { Text("Exit Kiosk?") },
            text  = { Text("This will stop kiosk lock mode and return to normal Android. Continue?") },
            confirmButton = {
                Button(
                    onClick = { showExitConfirm = false; onExitKiosk() },
                    colors  = ButtonDefaults.buttonColors(containerColor = ErrorRed)
                ) { Text("Exit") }
            },
            dismissButton = { TextButton(onClick = { showExitConfirm = false }) { Text("Cancel") } }
        )
    }

    if (showCloseConfirm) {
        AlertDialog(
            onDismissRequest = { showCloseConfirm = false },
            title = { Text("Close App?") },
            text  = { Text("This will shut down the Kiosk application completely. Continue?") },
            confirmButton = {
                Button(
                    onClick = { showCloseConfirm = false; onCloseApp() },
                    colors  = ButtonDefaults.buttonColors(containerColor = ErrorRed)
                ) { Text("Close App") }
            },
            dismissButton = { TextButton(onClick = { showCloseConfirm = false }) { Text("Cancel") } }
        )
    }
}

@Composable
private fun AdminSection(title: String, content: @Composable ColumnScope.() -> Unit) {
    Card(
        modifier  = Modifier.fillMaxWidth(),
        colors    = CardDefaults.cardColors(containerColor = SurfaceWhite),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = KarimaDark)
            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
            content()
        }
    }
}
