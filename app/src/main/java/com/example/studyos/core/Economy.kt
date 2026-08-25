package com.example.studyos.core

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import java.util.Calendar

object Economy {
    val fame = MutableStateFlow(100)
    val shame = MutableStateFlow(0)
    val streak = MutableStateFlow(4)
    val studying = MutableStateFlow(false)
    val continuousSecs = MutableStateFlow(0)
    private lateinit var prefs: SharedPreferences
    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())

    fun init(context: Context) {
        if (::prefs.isInitialized) return
        prefs = context.getSharedPreferences("studyos_economy", Context.MODE_PRIVATE)
        fame.value = prefs.getInt("fame", 100)
        shame.value = prefs.getInt("shame", 0)
        streak.value = prefs.getInt("streak", 4)
        scope.launch {
            while (true) {
                delay(60_000L)
                val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
                if (studying.value) {
                    continuousSecs.value += 60
                } else {
                    continuousSecs.value = 0
                    if (hour in 5 until 22) {
                        addShame(if (hour in 16 until 18) 3 else 1)
                    }
                }
            }
        }
    }

    fun addFame(n: Int) {
        fame.value += n
        save()
    }

    fun addShame(n: Int) {
        shame.value += n
        save()
    }

    fun spend(n: Int): Boolean {
        if (fame.value < n) return false
        fame.value -= n
        save()
        return true
    }

    private fun save() {
        if (!::prefs.isInitialized) return
        prefs.edit()
            .putInt("fame", fame.value)
            .putInt("shame", shame.value)
            .putInt("streak", streak.value)
            .apply()
    }
}
