package com.kiosk.donation.kiosk

import android.app.Activity
import android.app.ActivityManager
import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Context
import android.os.Build
import android.util.Log
import android.view.WindowManager
import android.widget.Toast

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

    fun configureKioskPolicies(context: Context) {
        val dpm = context.getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
        val adminComponent = ComponentName(context, KioskDeviceAdminReceiver::class.java)

        if (dpm.isDeviceOwnerApp(context.packageName)) {
            try {
                // Set the packages allowed to enter LockTask mode
                dpm.setLockTaskPackages(adminComponent, arrayOf(context.packageName))
                
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    // To truly LOCK the app, we must NOT include HOME or NOTIFICATIONS.
                    // If we include HOME, the user can press home and exit.
                    // If we include NOTIFICATIONS, they can pull down the shade and reach settings.
                    val flags = DevicePolicyManager.LOCK_TASK_FEATURE_NONE or
                               DevicePolicyManager.LOCK_TASK_FEATURE_SYSTEM_INFO
                    
                    Log.d("KioskManager", "Setting lock task features (restrictive)")
                    dpm.setLockTaskFeatures(adminComponent, flags)
                }
            } catch (e: Exception) {
                Log.e("KioskManager", "Policy setup failed", e)
            }
        }
    }

    fun startKioskMode(activity: Activity) {
        Log.d("KioskManager", "startKioskMode called")
        activity.window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        if (isDeviceOwner(activity)) {
            try {
                Log.d("KioskManager", "Executing startLockTask()")
                // Keeping a debug toast to be 100% sure the code path is hit
                Toast.makeText(activity, "Engaging Kiosk Mode...", Toast.LENGTH_SHORT).show()
                activity.startLockTask()
            } catch (e: Exception) {
                Log.e("KioskManager", "startLockTask() failed", e)
                Toast.makeText(activity, "Kiosk Error: ${e.message}", Toast.LENGTH_LONG).show()
            }
        } else {
            Log.w("KioskManager", "Not Device Owner")
        }
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
