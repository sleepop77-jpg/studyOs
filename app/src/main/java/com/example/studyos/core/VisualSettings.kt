package com.example.studyos.core

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow

object VisualSettings {
    private const val PREFS = "studyos_visual_settings"

    val motionMode = MutableStateFlow(1)
    val timerRefreshSeconds = MutableStateFlow(5)
    val effectBrightness = MutableStateFlow(0.65f)

    private var preferences: SharedPreferences? = null

    fun init(context: Context) {
        if (preferences != null) return

        synchronized(this) {
            if (preferences != null) return

            val prefs = context.applicationContext.getSharedPreferences(
                PREFS,
                Context.MODE_PRIVATE
            )

            preferences = prefs
            motionMode.value = prefs.getInt("motion_mode", 1).coerceIn(0, 2)
            timerRefreshSeconds.value = prefs.getInt("timer_refresh", 5).let {
                if (it == 1) 1 else 5
            }
            effectBrightness.value = prefs.getFloat("effect_brightness", 0.65f)
                .coerceIn(0.25f, 1f)
        }
    }

    fun setMotionMode(context: Context, mode: Int) {
        init(context)
        val value = mode.coerceIn(0, 2)
        motionMode.value = value
        preferences?.edit()?.putInt("motion_mode", value)?.apply()
    }

    fun setTimerRefreshSeconds(context: Context, seconds: Int) {
        init(context)
        val value = if (seconds == 1) 1 else 5
        timerRefreshSeconds.value = value
        preferences?.edit()?.putInt("timer_refresh", value)?.apply()
    }

    fun updateEffectBrightness(value: Float) {
        effectBrightness.value = value.coerceIn(0.25f, 1f)
    }

    fun saveEffectBrightness(context: Context) {
        init(context)
        preferences?.edit()
            ?.putFloat("effect_brightness", effectBrightness.value)
            ?.apply()
    }
}
