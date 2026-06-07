package com.kiosk.donation

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.lifecycle.lifecycleScope
import com.kiosk.donation.data.SumUpManager
import com.kiosk.donation.data.SumUpResult
import com.kiosk.donation.kiosk.KioskManager
import com.kiosk.donation.ui.KioskApp
import com.kiosk.donation.ui.KioskViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private val viewModel: KioskViewModel by viewModels()

    // Callback set by KioskApp so we can drive navigation after payment completes
    var onPaymentSuccess: (() -> Unit)? = null
    var onPaymentCancelled: (() -> Unit)? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Observe pending payments — launch SumUp checkout when one is set
        lifecycleScope.launch {
            viewModel.pendingPayment.collectLatest { payment ->
                if (payment != null) {
                    val affiliateKey = viewModel.sumupAffiliateKey.value
                    if (affiliateKey.isBlank() || affiliateKey == "YOUR_SUMUP_AFFILIATE_KEY") {
                        // Not configured yet — clear payment and let user know via admin screen
                        viewModel.clearPayment()
                        return@collectLatest
                    }

                    val title = when (payment) {
                        is com.kiosk.donation.data.PaymentMode.Donation ->
                            "Donation to ${viewModel.orgName.value}"
                        is com.kiosk.donation.data.PaymentMode.ProductPurchase ->
                            "Purchase (${payment.items.size} item${if (payment.items.size == 1) "" else "s"})"
                    }
                    val amount = when (payment) {
                        is com.kiosk.donation.data.PaymentMode.Donation        -> payment.amountGBP
                        is com.kiosk.donation.data.PaymentMode.ProductPurchase -> viewModel.cartTotal.value
                    }

                    SumUpManager.charge(
                        activity    = this@MainActivity,
                        amount      = amount,
                        title       = title,
                        foreignTxId = "kiosk-${System.currentTimeMillis()}"
                    )
                }
            }
        }

        // Observe SumUp login requests from admin screen
        lifecycleScope.launch {
            viewModel.loginRequested.collectLatest { requested ->
                if (requested) {
                    SumUpManager.login(this@MainActivity, viewModel.sumupAffiliateKey.value)
                    viewModel.onLoginHandled()
                }
            }
        }

        setContent {
            KioskApp(
                viewModel        = viewModel,
                isDeviceOwner    = KioskManager.isDeviceOwner(this),
                onSumupLogin     = {
                    SumUpManager.login(this, viewModel.sumupAffiliateKey.value)
                },
                onExitKiosk      = { KioskManager.stopKioskMode(this) },
                onPaymentSuccess = { callback -> onPaymentSuccess = callback },
                onPaymentCancelled = { callback -> onPaymentCancelled = callback }
            )
        }
    }

    @Deprecated("Required for SumUp SDK which uses classic startActivityForResult")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        when (requestCode) {
            SumUpManager.REQUEST_CODE_PAYMENT -> {
                val result = SumUpManager.parseActivityResult(requestCode, data)
                when (result) {
                    is SumUpResult.Success     -> {
                        viewModel.onPaymentSuccess()
                        onPaymentSuccess?.invoke()
                    }
                    is SumUpResult.NotLoggedIn -> {
                        // Prompt login then user retries
                        SumUpManager.login(this, viewModel.sumupAffiliateKey.value)
                    }
                    else -> {
                        viewModel.onPaymentCancelled()
                        onPaymentCancelled?.invoke()
                    }
                }
            }
            SumUpManager.REQUEST_CODE_LOGIN -> {
                // Login complete — user can now retry their payment
                viewModel.onLoginHandled()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        // Only engage lock task on the dedicated kiosk tablet once Device Owner is configured.
        // On personal/test devices, startLockTask() causes Huawei/MIUI/etc. aggressive memory
        // managers to kill and restart the process, which breaks in-app navigation entirely.
        if (KioskManager.isDeviceOwner(this)) {
            KioskManager.startKioskMode(this)
        }
    }
}
