package com.example.studyos.core

import android.content.Context
import android.content.Intent
import android.media.AudioManager
import android.media.ToneGenerator
import androidx.core.content.ContextCompat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

object Timer {
    val seconds = MutableStateFlow(25 * 60)
    val displaySeconds = MutableStateFlow(25 * 60)
    val total = MutableStateFlow(25 * 60)
    val originalTotal = MutableStateFlow(25 * 60)

    val running = MutableStateFlow(false)
    val strict = MutableStateFlow(false)
    val subject = MutableStateFlow("Mathematics")

    val isOvertime = MutableStateFlow(false)
    val originalCompleted = MutableStateFlow(false)
    val originalDurationMinutes = MutableStateFlow(25)
    val overtimeMinutes = MutableStateFlow(25)
    val overtimeIndex = MutableStateFlow(0)
    val overtimeCompletedCount = MutableStateFlow(0)
    val completionEvents = MutableStateFlow(0)

    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())
    private var started = false
    private var sessionStarted = false
    private var appContext: Context? = null

    fun init(context: Context) {
        if (started) return

        appContext = context.applicationContext
        OvertimeSettings.init(context.applicationContext)
        started = true

        scope.launch {
            while (true) {
                delay(1000L)

                if (running.value) {
                    if (seconds.value > 1) {
                        seconds.value -= 1

                        val interval = VisualSettings.timerRefreshSeconds.value
                            .coerceIn(1, 5)

                        if (seconds.value % interval == 0) {
                            displaySeconds.value = seconds.value
                        }
                    } else {
                        if (!isOvertime.value) {
                            completeOriginalSession()
                        } else {
                            completeOvertimeSession()
                        }
                    }
                }
            }
        }
    }

    private fun completeOriginalSession() {
        val completedMinutes = originalDurationMinutes.value
        val completedStrict = strict.value

        // Preserve the original session's existing rewards.
        Economy.addFame(completedMinutes * 2)
        StudyMarket.onUserSessionCompleted(completedMinutes, completedStrict)

        originalCompleted.value = true
        strict.value = false

        // Keep running and studying true. This prevents the lock screen
        // from closing between the original block and overtime.
        isOvertime.value = true
        overtimeIndex.value = 1

        val nextSeconds = overtimeMinutes.value.coerceIn(1, 180) * 60
        total.value = nextSeconds
        seconds.value = nextSeconds
        displaySeconds.value = nextSeconds

        completionEvents.value += 1
        playCompletionTing()
    }

    private fun completeOvertimeSession() {
        val completedMinutes = overtimeMinutes.value.coerceIn(1, 180)

        // Each completed overtime block earns the normal non-strict
        // focus rewards once. The original block is never rewarded again.
        Economy.addFame(completedMinutes * 2)
        StudyMarket.onUserSessionCompleted(completedMinutes, false)

        overtimeCompletedCount.value += 1
        overtimeIndex.value = overtimeCompletedCount.value + 1

        // Automatically begin the next overtime block.
        val nextSeconds = completedMinutes * 60
        total.value = nextSeconds
        seconds.value = nextSeconds
        displaySeconds.value = nextSeconds

        completionEvents.value += 1
        playCompletionTing()
    }

    private fun playCompletionTing() {
        try {
            val tone = ToneGenerator(AudioManager.STREAM_NOTIFICATION, 85)
            tone.startTone(ToneGenerator.TONE_PROP_ACK, 250)

            scope.launch {
                delay(350L)
                try {
                    tone.release()
                } catch (_: Exception) {
                }
            }
        } catch (_: Exception) {
        }
    }

    private fun startKeepAlive() {
        val context = appContext ?: return
        try {
            ContextCompat.startForegroundService(
                context,
                Intent(context, FocusTimerService::class.java)
            )
        } catch (_: Exception) {
        }
    }

    private fun stopKeepAlive() {
        val context = appContext ?: return
        try {
            context.stopService(Intent(context, FocusTimerService::class.java))
        } catch (_: Exception) {
        }
    }

    private fun startNewSession() {
        sessionStarted = true

        originalDurationMinutes.value = (originalTotal.value / 60)
            .coerceIn(25, 180)

        // The selected overtime interval is captured when a new session
        // starts. Settings changes apply to the next session.
        overtimeMinutes.value = OvertimeSettings.minutes.value.coerceIn(1, 180)

        originalCompleted.value = false
        isOvertime.value = false
        overtimeIndex.value = 0
        overtimeCompletedCount.value = 0
        completionEvents.value = 0

        total.value = originalTotal.value
        seconds.value = originalTotal.value
        displaySeconds.value = originalTotal.value
    }

    fun setStrict(on: Boolean): Boolean {
        // Strict Mode belongs only to the original focus block.
        if (isOvertime.value && on) return false
        if (running.value && strict.value && !on) return false

        strict.value = on
        return true
    }

    fun canControl(): Boolean = !(strict.value && running.value)

    fun setMinutes(m: Int) {
        if (!canControl() || running.value || isOvertime.value) return

        val clamped = m.coerceIn(1, 180)
        originalTotal.value = clamped * 60
        total.value = clamped * 60
        seconds.value = clamped * 60
        displaySeconds.value = seconds.value

        sessionStarted = false
        originalCompleted.value = false
        isOvertime.value = false
        overtimeIndex.value = 0
        overtimeCompletedCount.value = 0
        completionEvents.value = 0

        Economy.studying.value = false
        Economy.continuousSecs.value = 0
    }

    fun toggle() {
        if (!canControl()) return

        if (running.value) {
            running.value = false
            Economy.studying.value = false
            Economy.continuousSecs.value = 0
            displaySeconds.value = seconds.value
            stopKeepAlive()
        } else {
            if (!sessionStarted) {
                startNewSession()
            }

            running.value = true
            Economy.studying.value = true
            startKeepAlive()
        }
    }

    fun reset() {
        if (!canControl()) return

        running.value = false
        Economy.studying.value = false
        Economy.continuousSecs.value = 0

        sessionStarted = false
        originalCompleted.value = false
        isOvertime.value = false
        overtimeIndex.value = 0
        overtimeCompletedCount.value = 0
        completionEvents.value = 0

        total.value = originalTotal.value
        seconds.value = originalTotal.value
        displaySeconds.value = originalTotal.value

        stopKeepAlive()
    }

    fun refreshDisplay() {
        displaySeconds.value = seconds.value
    }

    fun durationLabel(minutes: Int): String {
        return if (minutes > 0 && minutes % 60 == 0) {
            "${minutes / 60}-hour"
        } else {
            "$minutes-minute"
        }
    }

    fun ordinalWord(value: Int): String {
        val suffix = if (value % 100 in 11..13) {
            "th"
        } else {
            when (value % 10) {
                1 -> "st"
                2 -> "nd"
                3 -> "rd"
                else -> "th"
            }
        }
        return "$value$suffix"
    }
}
