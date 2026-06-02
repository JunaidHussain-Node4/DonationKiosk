package com.kiosk.donation.kiosk

import android.app.Activity
import android.app.ActivityManager
import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Context
import android.os.Build
import android.view.WindowManager

/**
 * Manages kiosk (LockTask) mode.
 *
 * LockTaskMode prevents the user from:
 *  - Pressing Home / Recents
 *  - Pulling down the notification/status bar
 *  - Switching apps
 *  - Accessing Settings
 *
 * Requires this app to be the Device Owner (see KioskDeviceAdminReceiver).
 */
object KioskManager {

    fun startKioskMode(activity: Activity) {
        // Keep screen on permanently
        activity.window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        val dpm = activity.getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
        val adminComponent = ComponentName(activity, KioskDeviceAdminReceiver::class.java)

        if (dpm.isDeviceOwnerApp(activity.packageName)) {
            // Full kiosk lock — only available when Device Owner is set via ADB
            try {
                dpm.setLockTaskPackages(adminComponent, arrayOf(activity.packageName))
            } catch (e: Exception) {
                // Ignore — already set
            }
            activity.startLockTask()
        }
        // If not Device Owner, do nothing — startLockTask() without Device Owner
        // triggers screen-pinning prompts and causes process restarts on Huawei/MIUI devices
    }

    fun stopKioskMode(activity: Activity) {
        activity.stopLockTask()
        activity.window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
    }

    fun isInLockTaskMode(context: Context): Boolean {
        val am = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            am.lockTaskModeState != ActivityManager.LOCK_TASK_MODE_NONE
        } else {
            am.isInLockTaskMode
        }
    }

    fun isDeviceOwner(context: Context): Boolean {
        val dpm = context.getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
        return dpm.isDeviceOwnerApp(context.packageName)
    }
}
