package com.example.studyos.core

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.app.usage.UsageEvents
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.example.studyos.MainActivity
import com.example.studyos.ui.lockdown.BustedActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class LockdownService : Service() {
    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())
    private var usage: UsageStatsManager? = null
    private var lastCheck = 0L
    private var polling = false

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        usage = getSystemService(USAGE_STATS_SERVICE) as UsageStatsManager
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        startForeground(1101, buildNotification())
        if (!polling) {
            polling = true
            lastCheck = System.currentTimeMillis()
            scope.launch { poll() }
        }
        return START_STICKY
    }

    private suspend fun poll() {
        while (true) {
            delay(1500L)
            if (!LockdownManager.isEnabled(this)) continue
            
            val now = System.currentTimeMillis()
            val events = usage?.queryEvents(lastCheck, now) ?: continue
            lastCheck = now
            
            var foreground: String? = null
            val event = UsageEvents.Event()
            while (events.hasNextEvent()) {
                events.getNextEvent(event)
                if (event.eventType == UsageEvents.Event.MOVE_TO_FOREGROUND) {
                    foreground = event.packageName
                }
            }
            
            val pkg = foreground ?: continue
            if (pkg == packageName) continue
            if (pkg == "com.android.launcher" || pkg == "com.android.systemui") continue
            
            if (LockdownManager.blockedPackages(this).contains(pkg)) {
                onBusted(pkg)
            }
        }
    }

    private fun onBusted(pkg: String) {
        val name = try {
            packageManager.getApplicationLabel(packageManager.getApplicationInfo(pkg, 0)).toString()
        } catch (_: Exception) { pkg }
        
        Economy.addShame(5)
        Economy.addFame(-10)

        val intent = Intent(this, BustedActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("busted_app_name", name)
        }
        startActivity(intent)
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "lockdown_channel",
                "Lockdown Active",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Shows when App Blocker is actively guarding your focus."
            }
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }

    private fun buildNotification(): android.app.Notification {
        val pi = PendingIntent.getActivity(
            this, 0, Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        return NotificationCompat.Builder(this, "lockdown_channel")
            .setSmallIcon(android.R.drawable.ic_lock_lock)
            .setContentTitle("LOCKDOWN ACTIVE")
            .setContentText("Distracting apps are sealed. Stay focused.")
            .setOngoing(true)
            .setContentIntent(pi)
            .build()
    }

    override fun onDestroy() {
        scope.cancel()
        super.onDestroy()
    }
}
