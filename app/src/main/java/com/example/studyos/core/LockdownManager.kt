package com.example.studyos.core

import android.app.AppOpsManager
import android.content.Context
import android.content.SharedPreferences
import android.os.Build
import android.os.Process
import android.provider.Settings
import kotlinx.coroutines.flow.MutableStateFlow
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object LockdownManager {
    private const val PREFS = "studyos_lockdown"
    private const val KEY_ENABLED = "enabled"
    private const val KEY_BLOCKED = "blocked_packages"
    private const val KEY_LIMITS = "app_limits"
    private const val KEY_USAGE = "app_usage_today"
    private const val KEY_USAGE_DATE = "usage_date"
    private const val KEY_LIMIT_BUSTED = "limit_busted_today"

    val limitsFlow = MutableStateFlow<Map<String, Int>>(emptyMap())
    val usageFlow = MutableStateFlow<Map<String, Long>>(emptyMap())

    private var loadedFor: Context? = null

    private fun prefs(context: Context): SharedPreferences =
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)

    private fun ensureLoaded(context: Context) {
        if (loadedFor != null) return
        loadedFor = context.applicationContext
        rollOverDay(context)
        limitsFlow.value = readLimits(context)
        usageFlow.value = readUsage(context)
    }

    fun isEnabled(context: Context): Boolean = prefs(context).getBoolean(KEY_ENABLED, false)

    fun setEnabled(context: Context, enabled: Boolean) {
        prefs(context).edit().putBoolean(KEY_ENABLED, enabled).apply()
    }

    fun blockedPackages(context: Context): Set<String> =
        prefs(context).getStringSet(KEY_BLOCKED, null) ?: defaultBlocked()

    fun setBlockedPackages(context: Context, packages: Set<String>) {
        prefs(context).edit().putStringSet(KEY_BLOCKED, packages).apply()
    }

    fun appLimits(context: Context): Map<String, Int> {
        ensureLoaded(context)
        return limitsFlow.value
    }

    fun setAppLimit(context: Context, pkg: String, minutes: Int) {
        val map = appLimits(context).toMutableMap()
        if (minutes <= 0) map.remove(pkg) else map[pkg] = minutes
        prefs(context).edit()
            .putStringSet(KEY_LIMITS, map.map { "${it.key}|${it.value}" }.toSet())
            .apply()
        limitsFlow.value = map
        if (minutes <= 0) clearLimitBust(context, pkg)
    }

    fun usageToday(context: Context): Map<String, Long> {
        ensureLoaded(context)
        return usageFlow.value
    }

    fun addUsage(context: Context, pkg: String, seconds: Long) {
        if (seconds <= 0) return
        ensureLoaded(context)
        rollOverDay(context)
        val map = usageFlow.value.toMutableMap()
        map[pkg] = (map[pkg] ?: 0L) + seconds
        prefs(context).edit()
            .putStringSet(KEY_USAGE, map.map { "${it.key}|${it.value}" }.toSet())
            .apply()
        usageFlow.value = map
    }

    fun isLimitBusted(context: Context, pkg: String): Boolean =
        prefs(context).getStringSet(KEY_LIMIT_BUSTED, null)?.contains(pkg) == true

    fun markLimitBusted(context: Context, pkg: String) {
        val set = (prefs(context).getStringSet(KEY_LIMIT_BUSTED, null) ?: emptySet()).toMutableSet()
        set.add(pkg)
        prefs(context).edit().putStringSet(KEY_LIMIT_BUSTED, set).apply()
    }

    fun clearLimitBust(context: Context, pkg: String) {
        val set = (prefs(context).getStringSet(KEY_LIMIT_BUSTED, null) ?: emptySet()).toMutableSet()
        set.remove(pkg)
        prefs(context).edit().putStringSet(KEY_LIMIT_BUSTED, set).apply()
    }

    fun rollOverDay(context: Context) {
        val today = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())
        val p = prefs(context)
        if (p.getString(KEY_USAGE_DATE, null) != today) {
            p.edit()
                .putString(KEY_USAGE_DATE, today)
                .remove(KEY_USAGE)
                .remove(KEY_LIMIT_BUSTED)
                .apply()
            usageFlow.value = emptyMap()
        }
    }

    private fun readLimits(context: Context): Map<String, Int> =
        prefs(context).getStringSet(KEY_LIMITS, null)?.mapNotNull {
            val parts = it.split("|")
            val m = parts.getOrNull(1)?.toIntOrNull() ?: return@mapNotNull null
            parts.getOrNull(0) to m
        }?.toMap() ?: emptyMap()

    private fun readUsage(context: Context): Map<String, Long> =
        prefs(context).getStringSet(KEY_USAGE, null)?.mapNotNull {
            val parts = it.split("|")
            val s = parts.getOrNull(1)?.toLongOrNull() ?: return@mapNotNull null
            parts.getOrNull(0) to s
        }?.toMap() ?: emptyMap()

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

    fun hasOverlayPermission(context: Context): Boolean =
        Build.VERSION.SDK_INT < Build.VERSION_CODES.M || Settings.canDrawOverlays(context)

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