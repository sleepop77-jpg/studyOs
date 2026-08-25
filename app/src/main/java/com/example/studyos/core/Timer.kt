package com.example.studyos.core

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

object Timer {
    val seconds = MutableStateFlow(25 * 60)
    val total = MutableStateFlow(25 * 60)
    val running = MutableStateFlow(false)
    val subject = MutableStateFlow("Mathematics")
    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())

    fun init() {
        scope.launch {
            while (true) {
                delay(1000L)
                if (running.value) {
                    if (seconds.value > 1) {
                        seconds.value -= 1
                    } else {
                        running.value = false
                        Economy.studying.value = false
                        val mins = total.value / 60
                        Economy.addFame(mins * 2)
                        seconds.value = total.value
                    }
                }
            }
        }
    }

    fun setMinutes(m: Int) {
        val clamped = m.coerceIn(1, 480)
        total.value = clamped * 60
        seconds.value = clamped * 60
        running.value = false
        Economy.studying.value = false
    }

    fun toggle() {
        running.value = !running.value
        Economy.studying.value = running.value
        if (!running.value) Economy.continuousSecs.value = 0
    }

    fun reset() {
        running.value = false
        Economy.studying.value = false
        seconds.value = total.value
    }
}
