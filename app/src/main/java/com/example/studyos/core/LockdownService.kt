package com.example.studyos.core

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.app.usage.UsageEvents
import android.app.usage.UsageStatsManager
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.example.studyos.MainActivity
import com.example.studyos.ui.lockdown.BustedActivity
import com.example.studyos.ui.lockdown.BustedOverlayContent
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
    private var escapes = 0
    private var polling = false

    private var fgPkg: String? = null
    private var fgSince = 0L
    private val lastLimitBust = mutableMapOf<String, Long>()

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        usage = getSystemService(USAGE_STATS_SERVICE) as UsageStatsManager
        createChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        startForeground(1101, buildNotification())
        if (!polling) {
            polling = true
            lastCheck = System.currentTimeMillis()
            fgSince = lastCheck
            scope.launch { poll() }
        }
        return START_STICKY
    }

    private suspend fun poll() {
        while (true) {
            delay(1000L)

            val lockdownOn = LockdownManager.isEnabled(this)
            if (!lockdownOn) {
                BustedOverlay.hide()
                fgPkg = null
                continue
            }

            LockdownManager.rollOverDay(this)
            val sealed = Timer.running.value

            val now = System.currentTimeMillis()
            val events = usage?.queryEvents(lastCheck, now)
            lastCheck = now
            if (events == null) continue

            val event = UsageEvents.Event()
            while (events.hasNextEvent()) {
                events.getNextEvent(event)
                when (event.eventType) {
                    UsageEvents.Event.MOVE_TO_FOREGROUND -> {
                        fgPkg = event.packageName
                        fgSince = event.timeStamp
                    }
                    UsageEvents.Event.MOVE_TO_BACKGROUND -> {
                        if (event.packageName == fgPkg) {
                            val usedSec = (event.timeStamp - fgSince) / 1000L
                            if (usedSec > 0) LockdownManager.addUsage(this, event.packageName, usedSec)
                            fgPkg = null
                        }
                    }
                }
            }

            val currentFg = fgPkg
            if (currentFg != null) {
                val usedSec = (now - fgSince) / 1000L
                if (usedSec > 0) LockdownManager.addUsage(this, currentFg, usedSec)
                fgSince = now
            }

            if (currentFg == null || currentFg == packageName) continue
            if (currentFg == "com.android.systemui" || currentFg == "com.android.launcher") continue

            if (LockdownManager.isGraceActive(currentFg)) continue

            val limitMin = LockdownManager.appLimits(this)[currentFg] ?: 0
            if (limitMin > 0) {
                val usedSec = LockdownManager.usageToday(this)[currentFg] ?: 0L
                if (usedSec >= limitMin * 60L && shouldLimitBust(currentFg)) {
                    onLimitBusted(currentFg, limitMin, usedSec)
                }
            }

            if (sealed && LockdownManager.blockedPackages(this).contains(currentFg)) {
                onBusted(currentFg)
            }
        }
    }

    private fun shouldLimitBust(pkg: String): Boolean {
        if (!LockdownManager.isLimitBusted(this, pkg)) return true
        val last = lastLimitBust[pkg] ?: 0L
        return System.currentTimeMillis() - last >= 30_000L
    }

    private fun labelFor(pkg: String): String = try {
        packageManager.getApplicationLabel(packageManager.getApplicationInfo(pkg, 0)).toString()
    } catch (_: Exception) {
        pkg
    }

    private fun onBusted(pkg: String) {
        doBust(labelFor(pkg), pkg, 0, 0L, 0L)
    }

    private fun onLimitBusted(pkg: String, limitMin: Int, usedSec: Long) {
        LockdownManager.markLimitBusted(this, pkg)
        lastLimitBust[pkg] = System.currentTimeMillis()
        doBust(labelFor(pkg), pkg, limitMin, usedSec, 0L)
    }

    private fun doBust(displayName: String, pkg: String, limitMin: Int, spentSec: Long, leftSec: Long) {
        escapes++
        val penalty = 3 + escapes * 2

        Economy.addShame(penalty)
        Economy.addFame(-10)
        StudyMarket.onUserBusted(displayName)

        val spentMin = spentSec / 60
        val leftMin = leftSec / 60

        var shown = BustedOverlay.show(this) {
            BustedOverlayContent(
                appName = displayName,
                penalty = penalty,
                appPkg = pkg,
                limitMinutes = limitMin,
                spentMinutes = spentMin,
                leftMinutes = leftMin,
                onUnblock = {
                    BustedOverlay.hide()
                    LockdownManager.unblockApp(this@LockdownService, pkg)
                },
                onGrantTime = { mins ->
                    LockdownManager.grantGrace(pkg, mins)
                    BustedOverlay.hide()
                },
                onReturn = {
                    BustedOverlay.hide()
                    try {
                        val i = Intent(this@LockdownService, MainActivity::class.java).apply {
                            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                        }
                        startActivity(i)
                    } catch (_: Exception) {
                    }
                }
            )
        }

        if (!shown) {
            try {
                val intent = Intent(this, BustedActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    putExtra("busted_app_name", displayName)
                    putExtra("busted_penalty", penalty)
                    putExtra("busted_pkg", pkg)
                    putExtra("busted_limit", limitMin)
                    putExtra("busted_spent", spentMin)
                    putExtra("busted_left", leftMin)
                }
                startActivity(intent)
                shown = true
            } catch (_: Exception) {
            }
        }

        if (!shown) {
            val n = NotificationCompat.Builder(this, "lockdown_channel")
                .setSmallIcon(android.R.drawable.ic_lock_lock)
                .setContentTitle("BUSTED: $displayName")
                .setContentText("+$penalty Shame and -10 Fame. Return to your session.")
                .setOngoing(false)
                .build()
            (getSystemService(NOTIFICATION_SERVICE) as NotificationManager).notify(1102, n)
        }
    }

    private fun createChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val ch = NotificationChannel(
                "lockdown_channel",
                "Lockdown Active",
                NotificationManager.IMPORTANCE_LOW
            )
            (getSystemService(NOTIFICATION_SERVICE) as NotificationManager).createNotificationChannel(ch)
        }
    }

    private fun buildNotification(): Notification {
        val pi = PendingIntent.getActivity(
            this,
            0,
            Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, "lockdown_channel")
            .setSmallIcon(android.R.drawable.ic_lock_lock)
            .setContentTitle("LOCKDOWN ARMED")
            .setContentText("Seals activate the moment your timer runs.")
            .setOngoing(true)
            .setContentIntent(pi)
            .build()
    }

    override fun onDestroy() {
        BustedOverlay.hide()
        scope.cancel()
        super.onDestroy()
    }
}