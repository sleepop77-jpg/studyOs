package com.example.studyos.ui.lockdown

import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.ui.graphics.Color
import com.example.studyos.MainActivity
import com.example.studyos.core.LockdownManager

class BustedActivity : ComponentActivity() {
    @Suppress("DEPRECATION")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val appName = intent.getStringExtra("busted_app_name") ?: "a sealed app"
        val penalty = intent.getIntExtra("busted_penalty", 3)
        val pkg = intent.getStringExtra("busted_pkg") ?: ""
        val limit = intent.getIntExtra("busted_limit", 0)
        val spent = intent.getLongExtra("busted_spent", 0L)
        val left = intent.getLongExtra("busted_left", 0L)

        val vib = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vib.vibrate(VibrationEffect.createWaveform(longArrayOf(0, 300, 150, 300, 150, 600), -1))
        } else {
            vib.vibrate(longArrayOf(0, 300, 150, 300, 150, 600), -1)
        }

        setContent {
            MaterialTheme(colorScheme = darkColorScheme(primary = Color(0xFFD9534F))) {
                BustedOverlayContent(
                    appName = appName,
                    penalty = penalty,
                    appPkg = pkg,
                    limitMinutes = limit,
                    spentMinutes = spent,
                    leftMinutes = left,
                    onOpenApp = { finish() },
                    onTurnOff = {
                        LockdownManager.setEnabled(this, false)
                        finish()
                    },
                    onReturn = {
                        finish()
                        try {
                            startActivity(
                                Intent(this, MainActivity::class.java).apply {
                                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                                }
                            )
                        } catch (_: Exception) {
                        }
                    }
                )
            }
        }
    }
}