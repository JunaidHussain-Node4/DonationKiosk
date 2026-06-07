package com.kiosk.donation.ui

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.HourglassEmpty
import androidx.compose.material.icons.filled.WifiOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.kiosk.donation.R
import com.kiosk.donation.data.PaymentMode
import com.kiosk.donation.data.SumUpManager
import com.kiosk.donation.ui.screens.*
import com.kiosk.donation.ui.theme.*
import kotlinx.coroutines.delay

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
    val isOnline          by viewModel.isOnline.collectAsState()
    val basketTimeout     by viewModel.basketTimeoutMinutes.collectAsState()

    var showSplash          by remember { mutableStateOf(true) }
    var lastPayment         by remember { mutableStateOf<PaymentMode?>(null) }
    var showChangePinDialog by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        onPaymentSuccess  { navController.navigate(Routes.THANK_YOU) }
        onPaymentCancelled { }
        
        delay(2000)
        showSplash = false
    }

    KioskTheme {
        Box(modifier = Modifier.fillMaxSize()) {
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
                        isOnline = isOnline,
                        onBack   = { navController.popBackStack() },
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
                        isOnline         = isOnline,
                        onBack           = { navController.popBackStack() },
                        onAddToCart      = viewModel::addToCart,
                        onRemoveFromCart = viewModel::removeFromCart,
                        onClearCart      = viewModel::clearCart,
                        onCheckout       = {
                            lastPayment = PaymentMode.ProductPurchase(cart)
                            viewModel.initiateProductPurchase()
                        },
                        onUserActivity   = viewModel::updateActivity
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
                        basketTimeout      = basketTimeout,
                        onSaveOrgName     = viewModel::saveOrgName,
                        onSaveSumupKey    = viewModel::saveSumupKey,
                        onSetDonationsEnabled = viewModel::setDonationsEnabled,
                        onSetProductsEnabled  = viewModel::setProductsEnabled,
                        onSetBasketTimeout    = viewModel::setBasketTimeout,
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

            if (!isOnline) {
                NoConnectionOverlay()
            }

            AnimatedVisibility(
                visible = showSplash,
                exit = fadeOut(tween(800))
            ) {
                SplashScreenOverlay()
            }
        }
    }
}

@Composable
fun SplashScreenOverlay() {
    val infiniteTransition = rememberInfiniteTransition(label = "hourglass")
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotation"
    )

    Box(modifier = Modifier.fillMaxSize()) {
        Image(
            painter = painterResource(id = R.drawable.background),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(HomeGradientTop, HomeGradientMid, HomeGradientBottom)
                    )
                )
        )

        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Default.HourglassEmpty,
                contentDescription = null,
                modifier = Modifier
                    .size(80.dp)
                    .rotate(rotation),
                tint = Color.White
            )
            Spacer(Modifier.height(24.dp))
            Text(
                "Loading...",
                color = Color.White,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun NoConnectionOverlay() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.7f))
            .clickable(enabled = false) {}, 
        contentAlignment = Alignment.Center
    ) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 8.dp,
            modifier = Modifier.padding(32.dp).widthIn(max = 500.dp)
        ) {
            Column(
                modifier = Modifier.padding(40.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = Icons.Default.WifiOff,
                    contentDescription = null,
                    modifier = Modifier.size(80.dp),
                    tint = ErrorRed
                )
                Spacer(Modifier.height(24.dp))
                Text(
                    text = "Connection Lost",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(Modifier.height(12.dp))
                Text(
                    text = "This kiosk requires an internet connection to process payments. " +
                           "Please wait while we try to reconnect.",
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
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
