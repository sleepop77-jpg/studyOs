package com.example.studyos.core

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow

object OvertimeSettings {
    private const val PREFS_NAME = "studyos_overtime_settings"
    private const val KEY_MINUTES = "overtime_minutes"

    val minutes = MutableStateFlow(25)

    private var preferences: SharedPreferences? = null

    fun init(context: Context) {
        if (preferences != null) return

        synchronized(this) {
            if (preferences != null) return

            val prefs = context.applicationContext.getSharedPreferences(
                PREFS_NAME,
                Context.MODE_PRIVATE
            )

            preferences = prefs
            minutes.value = prefs.getInt(KEY_MINUTES, 25).coerceIn(1, 180)
        }
    }

    fun setMinutes(context: Context, value: Int) {
        init(context)

        val clamped = value.coerceIn(1, 180)
        minutes.value = clamped

        preferences?.edit()
            ?.putInt(KEY_MINUTES, clamped)
            ?.apply()
    }
}
