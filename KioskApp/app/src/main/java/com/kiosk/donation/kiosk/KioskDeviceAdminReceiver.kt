package com.kiosk.donation.kiosk

import android.app.admin.DeviceAdminReceiver
import android.content.Context
import android.content.Intent

/**
 * Device Admin Receiver required for LockTaskMode kiosk operation.
 *
 * To activate Device Owner mode (one-time ADB setup on a fresh/no-account device):
 *   adb shell dpm set-device-owner com.kiosk.donation/.kiosk.KioskDeviceAdminReceiver
 *
 * To verify it worked:
 *   adb shell dpm list-owners
 *
 * To remove Device Owner (if needed):
 *   adb shell dpm remove-active-admin com.kiosk.donation/.kiosk.KioskDeviceAdminReceiver
 *   OR from within the app via the admin screen.
 */
class KioskDeviceAdminReceiver : DeviceAdminReceiver() {

    override fun onEnabled(context: Context, intent: Intent) {
        super.onEnabled(context, intent)
        // Device admin enabled — LockTaskMode can now be used
    }

    override fun onDisabled(context: Context, intent: Intent) {
        super.onDisabled(context, intent)
    }
}
