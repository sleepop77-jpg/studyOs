package com.example.studyos

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.example.studyos.core.Admin
import com.example.studyos.core.Economy
import com.example.studyos.core.Store
import com.example.studyos.core.Timer
import com.example.studyos.ui.launcher.LauncherScreen
import com.example.studyos.ui.pomodoro.PomodoroScreen
import com.example.studyos.ui.settings.SettingsScreen
import com.example.studyos.ui.store.StoreScreen
import com.example.studyos.ui.lockdown.LockdownScreen
import com.example.studyos.core.LockdownManager
import com.example.studyos.core.LockdownService
import android.content.Intent
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import com.example.studyos.ui.theme.PrimaryCoral

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Economy.init(applicationContext)
        Timer.init()
        Store.init(applicationContext)
        Admin.init(applicationContext)
        setContent {
            MaterialTheme(colorScheme = darkColorScheme(primary = PrimaryCoral)) {
                var route by remember { mutableStateOf("launcher") }
                BackHandler(enabled = route != "launcher") { route = "launcher" }
                when (route) {
                    "launcher" -> LauncherScreen(nav = { route = it })
                    "pomodoro" -> PomodoroScreen(back = { route = "launcher" })
                    "store" -> StoreScreen(back = { route = "launcher" })
                    "settings" -> SettingsScreen(back = { route = "launcher" })
                }
            }
        }
    }
}
