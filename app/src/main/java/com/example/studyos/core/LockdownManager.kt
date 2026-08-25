package com.example.studyos.core

import android.app.AppOpsManager
import android.content.Context
import android.content.SharedPreferences
import android.os.Build
import android.os.Process

object LockdownManager {
    private const val PREFS = "studyos_lockdown"
    private const val KEY_ENABLED = "enabled"
    private const val KEY_BLOCKED = "blocked_packages"

    private fun prefs(context: Context): SharedPreferences =
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)

    fun isEnabled(context: Context): Boolean = prefs(context).getBoolean(KEY_ENABLED, false)

    fun setEnabled(context: Context, enabled: Boolean) {
        prefs(context).edit().putBoolean(KEY_ENABLED, enabled).apply()
    }

    fun blockedPackages(context: Context): Set<String> =
        prefs(context).getStringSet(KEY_BLOCKED, null) ?: defaultBlocked()

    fun setBlockedPackages(context: Context, packages: Set<String>) {
        prefs(context).edit().putStringSet(KEY_BLOCKED, packages).apply()
    }

    @Suppress("DEPRECATION")
    fun hasUsageAccess(context: Context): Boolean {
        val appOps = context.getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
        val mode = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            appOps.unsafeCheckOpNoThrow(AppOpsManager.OPSTR_GET_USAGE_STATS, Process.myUid(), context.packageName)
        } else {
            appOps.checkOpNoThrow(AppOpsManager.OPSTR_GET_USAGE_STATS, Process.myUid(), context.packageName)
        }
        return mode == AppOpsManager.MODE_ALLOWED
    }

    private fun defaultBlocked(): Set<String> = setOf(
        "com.instagram.android",
        "com.zhiliaoapp.musically",
        "com.google.android.youtube",
        "com.whatsapp",
        "com.snapchat.android",
        "com.twitter.android",
        "com.reddit.frontpage"
    )
}
