package com.example.budget.ui.utils

import android.content.Context
import android.content.pm.PackageManager

/**
 * Utility object for managing app version information.
 * Provides centralized version retrieval and management.
 */
object VersionUtils {
    
    /**
     * Gets the app version name from the package manager.
     * 
     * @param context The application context
     * @return The version name string, or "0" as fallback if unable to retrieve
     */
    fun getAppVersion(context: Context): String {
        return try {
            context.packageManager.getPackageInfo(context.packageName, 0).versionName
        } catch (e: PackageManager.NameNotFoundException) {
            "0" // Fallback version
        }
    }
    
    /**
     * Gets the app version code from the package manager.
     * 
     * @param context The application context
     * @return The version code as Long, or 0 as fallback if unable to retrieve
     */
    fun getAppVersionCode(context: Context): Long {
        return try {
            val packageInfo = context.packageManager.getPackageInfo(context.packageName, 0)
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.P) {
                packageInfo.longVersionCode
            } else {
                @Suppress("DEPRECATION")
                packageInfo.versionCode.toLong()
            }
        } catch (e: PackageManager.NameNotFoundException) {
            0L // Fallback version code
        }
    }
    
    /**
     * Gets a formatted version string with both name and code.
     * 
     * @param context The application context
     * @return Formatted string like "1.2 (3)" where 1.2 is version name and 3 is version code
     */
    fun getFormattedVersion(context: Context): String {
        val versionName = getAppVersion(context)
        val versionCode = getAppVersionCode(context)
        return "$versionName ($versionCode)"
    }
}
