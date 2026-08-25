package com.example.studyos

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
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
        installSplashScreen()
        Economy.init(applicationContext)
        Timer.init()
        Store.init(applicationContext)
        Admin.init(applicationContext)
        setContent {
            MaterialTheme(colorScheme = darkColorScheme(primary = PrimaryCoral)) {
                var route by remember { mutableStateOf("launcher") }
                val notifLauncher = androidx.activity.compose.rememberLauncherForActivityResult(
                    contract = androidx.activity.result.contract.ActivityResultContracts.RequestPermission()
                ) { }
                LaunchedEffect(Unit) {
                    if (android.os.Build.VERSION.SDK_INT >= 33 &&
                        androidx.core.content.ContextCompat.checkSelfPermission(
                            this@MainActivity,
                            android.Manifest.permission.POST_NOTIFICATIONS
                        ) != android.content.pm.PackageManager.PERMISSION_GRANTED
                    ) {
                        notifLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
                    }
                }
                LaunchedEffect(Unit) {
                    val serviceIntent = Intent(this@MainActivity, LockdownService::class.java)
                    if (LockdownManager.hasUsageAccess(this@MainActivity)) {
                        androidx.core.content.ContextCompat.startForegroundService(this@MainActivity, serviceIntent)
                    } else {
                        stopService(serviceIntent)
                    }
                }

                BackHandler(enabled = route != "launcher") { route = "launcher" }
                when (route) {
                    "launcher" -> LauncherScreen(nav = { route = it })
                    "pomodoro" -> PomodoroScreen(back = { route = "launcher" })
                    "store" -> StoreScreen(back = { route = "launcher" })
                    "settings" -> SettingsScreen(back = { route = "launcher" })
                    "lockdown" -> LockdownScreen(back = { route = "launcher" })
                }
            }
        }
    }
}
