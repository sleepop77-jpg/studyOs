package com.example.studyos.core

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.app.usage.UsageEvents
import android.app.usage.UsageStatsManager
import android.content.Intent
import android.graphics.Color
import android.graphics.PixelFormat
import android.graphics.Typeface
import android.os.Build
import android.os.IBinder
import android.provider.Settings
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
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
    private var escapes = 0
    private var polling = false
    private var overlayView: View? = null

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
            scope.launch { poll() }
        }
        return START_STICKY
    }

    private suspend fun poll() {
        while (true) {
            delay(1000L)
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
            if (pkg == "com.android.systemui" || pkg == "com.android.launcher") continue
            if (LockdownManager.blockedPackages(this).contains(pkg)) {
                onBusted(pkg)
            }
        }
    }

    private fun onBusted(pkg: String) {
        escapes++
        val penalty = 3 + escapes * 2
        Economy.addShame(penalty)
        Economy.addFame(-10)
        val name = try {
            packageManager.getApplicationLabel(packageManager.getApplicationInfo(pkg, 0)).toString()
        } catch (_: Exception) { pkg }
        if (Settings.canDrawOverlays(this)) {
            showOverlay(name, penalty)
        } else {
            val intent = Intent(this, BustedActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_CLEAR_TASK
                putExtra("busted_app_name", name)
            }
            try { startActivity(intent) } catch (_: Exception) { }
        }
    }

    @Suppress("DEPRECATION")
    private fun showOverlay(appName: String, penalty: Int) {
        if (overlayView != null) return
        val wm = getSystemService(WINDOW_SERVICE) as WindowManager
        val type = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        } else {
            WindowManager.LayoutParams.TYPE_PHONE
        }
        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.MATCH_PARENT,
            type,
            WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
            PixelFormat.TRANSLUCENT
        )
        params.gravity = Gravity.CENTER
        overlayView = buildOverlay(appName, penalty)
        try { wm.addView(overlayView, params) } catch (_: Exception) { overlayView = null }
    }

    private fun hideOverlay() {
        overlayView?.let { v ->
            try { (getSystemService(WINDOW_SERVICE) as WindowManager).removeView(v) } catch (_: Exception) { }
        }
        overlayView = null
    }

    private fun buildOverlay(appName: String, penalty: Int): View {
        val ctx = this
        val dp = resources.displayMetrics.density
        fun p(v: Int) = (v * dp).toInt()
        val root = LinearLayout(ctx).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setBackgroundColor(Color.parseColor("#FF1A0505"))
            setPadding(p(28), p(28), p(28), p(28))
            isClickable = true
            isFocusable = true
        }
        val title = TextView(ctx).apply {
            text = "BUSTED."
            setTextColor(Color.parseColor("#FFFFD700"))
            textSize = 42f
            typeface = Typeface.DEFAULT_BOLD
            gravity = Gravity.CENTER
        }
        val msg = TextView(ctx).apply {
            text = "You opened $appName during Lockdown.\n+$penalty Shame and -10 Fame. The mascot is disappointed."
            setTextColor(Color.WHITE)
            textSize = 15f
            gravity = Gravity.CENTER
            setPadding(0, p(16), 0, p(32))
        }
        val btn = Button(ctx).apply {
            text = "RETURN TO STUDYOS"
            setTextColor(Color.WHITE)
            setBackgroundColor(Color.parseColor("#FFD9534F"))
            setPadding(p(16), p(14), p(16), p(14))
        }
        btn.setOnClickListener {
            hideOverlay()
            val i = Intent(ctx, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            }
            try { ctx.startActivity(i) } catch (_: Exception) { }
        }
        root.addView(title, LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT))
        root.addView(msg, LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT))
        root.addView(btn, LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT))
        return root
    }

    private fun createChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val ch = NotificationChannel("lockdown_channel", "Lockdown Active", NotificationManager.IMPORTANCE_LOW)
            (getSystemService(NOTIFICATION_SERVICE) as NotificationManager).createNotificationChannel(ch)
        }
    }

    private fun buildNotification(): Notification {
        val pi = PendingIntent.getActivity(
            this, 0, Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        return NotificationCompat.Builder(this, "lockdown_channel")
            .setSmallIcon(android.R.drawable.ic_lock_lock)
            .setContentTitle("LOCKDOWN ACTIVE")
            .setContentText("Distracting apps are sealed until your session ends.")
            .setOngoing(true)
            .setContentIntent(pi)
            .build()
    }

    override fun onDestroy() {
        hideOverlay()
        scope.cancel()
        super.onDestroy()
    }
}
