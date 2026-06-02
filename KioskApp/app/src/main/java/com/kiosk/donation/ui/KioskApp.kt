package com.kiosk.donation.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.kiosk.donation.data.PaymentMode
import com.kiosk.donation.data.SumUpManager
import com.kiosk.donation.ui.screens.*
import com.kiosk.donation.ui.theme.ErrorRed
import com.kiosk.donation.ui.theme.KioskTheme

object Routes {
    const val HOME      = "home"
    const val DONATE    = "donate"
    const val SHOP      = "shop"
    const val THANK_YOU = "thankyou"
    const val PIN_ENTRY = "pin_entry"
    const val ADMIN     = "admin"
}

@Composable
fun KioskApp(
    viewModel: KioskViewModel,
    isDeviceOwner: Boolean,
    onSumupLogin: () -> Unit,
    onExitKiosk: () -> Unit,
    onPaymentSuccess: (() -> Unit) -> Unit,
    onPaymentCancelled: (() -> Unit) -> Unit
) {
    val navController = rememberNavController()

    val orgName      by viewModel.orgName.collectAsState()
    val cart         by viewModel.cart.collectAsState()
    val cartTotal    by viewModel.cartTotal.collectAsState()
    val adminPin     by viewModel.adminPin.collectAsState()
    val sumupKey     by viewModel.sumupAffiliateKey.collectAsState()
    val products     by viewModel.products.collectAsState()
    val syncState    by viewModel.syncState.collectAsState()
    val isDonationsEnabled by viewModel.isDonationsEnabled.collectAsState()
    val isProductsEnabled  by viewModel.isProductsEnabled.collectAsState()
    val isLoggedInToSumup by viewModel.isLoggedIn.collectAsState()

    var lastPayment         by remember { mutableStateOf<PaymentMode?>(null) }
    var showChangePinDialog by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        onPaymentSuccess  { navController.navigate(Routes.THANK_YOU) }
        onPaymentCancelled { }
    }

    KioskTheme {
        NavHost(navController = navController, startDestination = Routes.HOME) {

            composable(Routes.HOME) {
                HomeScreen(
                    orgName            = orgName,
                    isDonationsEnabled = isDonationsEnabled,
                    isProductsEnabled  = isProductsEnabled,
                    onDonateClick      = { navController.navigate(Routes.DONATE) },
                    onShopClick        = { navController.navigate(Routes.SHOP) },
                    onAdminLongPress   = { navController.navigate(Routes.PIN_ENTRY) }
                )
            }

            composable(Routes.PIN_ENTRY) {
                PinEntryScreen(
                    correctPin = adminPin,
                    onSuccess  = {
                        navController.navigate(Routes.ADMIN) {
                            popUpTo(Routes.HOME)
                        }
                    },
                    onCancel = { navController.popBackStack() }
                )
            }

            composable(Routes.DONATE) {
                DonationScreen(
                    onBack = { navController.popBackStack() },
                    onProceedToPayment = { amount ->
                        lastPayment = PaymentMode.Donation(amount)
                        viewModel.initiateDonation(amount)
                    }
                )
            }

            composable(Routes.SHOP) {
                ProductScreen(
                    products         = products,
                    cart             = cart,
                    cartTotal        = cartTotal,
                    onBack           = { navController.popBackStack() },
                    onAddToCart      = viewModel::addToCart,
                    onRemoveFromCart = viewModel::removeFromCart,
                    onClearCart      = viewModel::clearCart,
                    onCheckout       = {
                        lastPayment = PaymentMode.ProductPurchase(cart)
                        viewModel.initiateProductPurchase()
                    }
                )
            }

            composable(Routes.THANK_YOU) {
                val payment = lastPayment
                val amount = when (payment) {
                    is PaymentMode.Donation        -> payment.amountGBP
                    is PaymentMode.ProductPurchase -> cartTotal
                    null                           -> null
                }
                ThankYouScreen(
                    amountGBP  = amount,
                    isDonation = payment is PaymentMode.Donation,
                    orgName    = orgName,
                    onDone     = {
                        viewModel.clearCart()
                        viewModel.clearPayment()
                        navController.navigate(Routes.HOME) { popUpTo(0) { inclusive = true } }
                    }
                )
            }

            composable(Routes.ADMIN) {
                AdminScreen(
                    currentOrgName    = orgName,
                    currentSumupKey   = sumupKey,
                    isDeviceOwner     = isDeviceOwner,
                    isLoggedInToSumup = isLoggedInToSumup,
                    syncState         = syncState,
                    productCount      = products.size,
                    isDonationsEnabled = isDonationsEnabled,
                    isProductsEnabled  = isProductsEnabled,
                    onSaveOrgName     = viewModel::saveOrgName,
                    onSaveSumupKey    = viewModel::saveSumupKey,
                    onSetDonationsEnabled = viewModel::setDonationsEnabled,
                    onSetProductsEnabled  = viewModel::setProductsEnabled,
                    onChangePinClick  = { showChangePinDialog = true },
                    onSumupLogin      = onSumupLogin,
                    onSumupLogout     = viewModel::sumupLogout,
                    onSyncProducts    = viewModel::syncProducts,
                    onExitKiosk       = onExitKiosk,
                    onBack            = {
                        viewModel.clearSyncState()
                        navController.navigate(Routes.HOME) { popUpTo(0) { inclusive = true } }
                    }
                )
            }
        }

        if (showChangePinDialog) {
            ChangePinDialog(
                onSave    = { newPin -> viewModel.saveAdminPin(newPin); showChangePinDialog = false },
                onDismiss = { showChangePinDialog = false }
            )
        }
    }
}

@Composable
private fun ChangePinDialog(onSave: (String) -> Unit, onDismiss: () -> Unit) {
    var pin1  by remember { mutableStateOf("") }
    var pin2  by remember { mutableStateOf("") }
    var error by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Change Admin PIN") },
        text  = {
            Column {
                OutlinedTextField(
                    value         = pin1,
                    onValueChange = { pin1 = it; error = "" },
                    label         = { Text("New PIN") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                    visualTransformation = PasswordVisualTransformation(),
                    singleLine    = true
                )
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value         = pin2,
                    onValueChange = { pin2 = it; error = "" },
                    label         = { Text("Confirm PIN") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                    visualTransformation = PasswordVisualTransformation(),
                    singleLine    = true
                )
                if (error.isNotEmpty()) {
                    Spacer(Modifier.height(4.dp))
                    Text(error, color = ErrorRed)
                }
            }
        },
        confirmButton = {
            Button(onClick = {
                when {
                    pin1.length < 4 -> error = "PIN must be at least 4 digits"
                    pin1 != pin2    -> error = "PINs don't match"
                    else            -> onSave(pin1)
                }
            }) { Text("Save") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}
