package com.kiosk.donation.data

import android.app.Activity
import android.content.Intent
import com.sumup.merchant.reader.api.SumUpAPI
import com.sumup.merchant.reader.api.SumUpLogin
import com.sumup.merchant.reader.api.SumUpPayment
import java.math.BigDecimal

/**
 * Thin wrapper around the SumUp Android SDK v6.
 *
 * SETUP:
 * 1. Create a SumUp merchant account at https://sumup.com
 * 2. Go to https://me.sumup.com/settings/affiliate-keys and generate an Affiliate Key
 *    for your app's package name (com.kiosk.donation).
 * 3. Enter that key in the Admin screen — it is used for the SumUp login call.
 *
 * The SumUp SDK uses classic startActivityForResult (not ActivityResultLauncher).
 * Results are received in MainActivity.onActivityResult().
 */
object SumUpManager {

    const val REQUEST_CODE_LOGIN   = 1001
    const val REQUEST_CODE_PAYMENT = 1002

    /**
     * Opens the SumUp login screen.
     * The merchant logs in once; the session persists.
     * Result returned to Activity.onActivityResult() with REQUEST_CODE_LOGIN.
     */
    fun login(activity: Activity, affiliateKey: String) {
        try {
            val sumUpLogin = SumUpLogin.builder(affiliateKey).build()
            SumUpAPI.openLoginActivity(activity, sumUpLogin, REQUEST_CODE_LOGIN)
        } catch (e: Exception) {
            android.util.Log.e("SumUpManager", "Login failed: ${e.message}", e)
        }
    }

    /** Returns true if a merchant account is currently logged in. */
    fun isLoggedIn(): Boolean = try {
        SumUpAPI.isLoggedIn()
    } catch (e: Exception) {
        false
    }

    /**
     * Initiates a card payment.
     * Result returned to Activity.onActivityResult() with REQUEST_CODE_PAYMENT.
     *
     * Note: affiliateKey is NOT passed to the payment builder — it is only used at login.
     *
     * @param activity    The calling Activity
     * @param amount      Amount in GBP (minimum 1.00)
     * @param title       Short description shown on the receipt
     * @param foreignTxId Optional unique transaction ID for your records (max 128 chars)
     */
    fun charge(
        activity: Activity,
        amount: BigDecimal,
        title: String,
        foreignTxId: String? = null,
        skipSuccessScreen: Boolean = false
    ) {
        val paymentBuilder = SumUpPayment.builder()
            .total(amount)
            .currency(SumUpPayment.Currency.GBP)
            .title(title)
        
        if (skipSuccessScreen) {
            paymentBuilder.skipSuccessScreen()
        }

        if (foreignTxId != null) {
            paymentBuilder.foreignTransactionId(foreignTxId)
        }

        try {
            SumUpAPI.checkout(activity, paymentBuilder.build(), REQUEST_CODE_PAYMENT)
        } catch (e: Exception) {
            // SDK not ready — should not happen after Application class is registered
        }
    }

    /**
     * Parse the result from onActivityResult for both login and payment.
     */
    fun parseActivityResult(requestCode: Int, data: Intent?): SumUpResult {
        if (data == null) return SumUpResult.Cancelled

        val extras = data.extras ?: return SumUpResult.Cancelled
        val sdkResultCode = extras.getInt(SumUpAPI.Response.RESULT_CODE, -1)
        
        // Try multiple keys to ensure we get the best ID for the Receipts API
        val txCode = extras.getString(SumUpAPI.Response.TX_CODE) 
            ?: extras.getString("transaction_code")
        
        val txId = extras.getString("transaction_id") 
            ?: extras.getString("id")

        android.util.Log.d("SumUpManager", "Parsed Result: $sdkResultCode, Code: $txCode, ID: $txId")

        return when (sdkResultCode) {
            SumUpAPI.Response.ResultCode.SUCCESSFUL                -> SumUpResult.Success(txCode, txId)
            SumUpAPI.Response.ResultCode.ERROR_NOT_LOGGED_IN       -> SumUpResult.NotLoggedIn
            SumUpAPI.Response.ResultCode.ERROR_TRANSACTION_FAILED  -> SumUpResult.Failed("Transaction failed")
            SumUpAPI.Response.ResultCode.ERROR_INVALID_AFFILIATE_KEY -> SumUpResult.Failed("Invalid affiliate key")
            SumUpAPI.Response.ResultCode.ERROR_NO_CONNECTIVITY     -> SumUpResult.Failed("No connectivity")
            else                                                   -> SumUpResult.Cancelled
        }
    }

    fun logout() {
        try { SumUpAPI.logout() } catch (e: Exception) { }
    }
}

sealed class SumUpResult {
    data class Success(val txCode: String?, val txId: String? = null) : SumUpResult()
    object Cancelled   : SumUpResult()
    object NotLoggedIn : SumUpResult()
    data class Failed(val reason: String) : SumUpResult()
}
