package com.kiosk.donation

import android.app.Application
import com.sumup.reader.sdk.api.SumUpState

/**
 * Application class.
 */
class KioskApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        // Initialize SumUp SDK for v6.0.0+
        SumUpState.init(this)
    }
}
