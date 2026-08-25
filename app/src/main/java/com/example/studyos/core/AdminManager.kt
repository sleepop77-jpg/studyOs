package com.example.studyos.core

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow

object Admin {
    const val SECRET_CODE = "4242"
    val enabled = MutableStateFlow(false)
    private lateinit var prefs: SharedPreferences

    fun init(context: Context) {
        if (::prefs.isInitialized) return
        prefs = context.getSharedPreferences("studyos_admin", Context.MODE_PRIVATE)
        enabled.value = prefs.getBoolean("on", false)
    }

    fun set(context: Context, on: Boolean) {
        if (!::prefs.isInitialized) init(context)
        prefs.edit().putBoolean("on", on).apply()
        enabled.value = on
    }
}
