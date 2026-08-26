package com.example.studyos

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
import androidx.compose.animation.core.tween
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.example.studyos.core.Admin
import com.example.studyos.core.Economy
import com.example.studyos.core.LockdownManager
import com.example.studyos.core.LockdownService
import com.example.studyos.core.Store
import com.example.studyos.core.StudyMarket
import com.example.studyos.core.Timer
import com.example.studyos.ui.launcher.LauncherScreen
import com.example.studyos.ui.lockdown.LockdownScreen
import com.example.studyos.ui.pomodoro.PomodoroScreen
import com.example.studyos.ui.settings.SettingsScreen
import com.example.studyos.ui.stock.StocksScreen
import com.example.studyos.ui.store.StoreScreen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        installSplashScreen()

        Economy.init(applicationContext)
        StudyMarket.init(applicationContext)
        Timer.init()
        Store.init(applicationContext)
        Admin.init(applicationContext)

        if (LockdownManager.isEnabled(this) && LockdownManager.hasUsageAccess(this)) {
            ContextCompat.startForegroundService(this, Intent(this, LockdownService::class.java))
        }

        setContent {
            MaterialTheme(colorScheme = darkColorScheme(primary = Color(0xFFD9534F))) {
                StudyOSNav()
            }
        }
    }
}

@Composable
private fun StudyOSNav() {
    val context = LocalContext.current
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { }

    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= 33 &&
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
        ) {
            permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    var route by remember { mutableStateOf("launcher") }

    BackHandler(enabled = route != "launcher") {
        route = "launcher"
    }

    AnimatedContent(
        targetState = route,
        transitionSpec = {
            (scaleIn(animationSpec = tween(280), initialScale = 0.88f) + fadeIn(animationSpec = tween(280)))
                .togetherWith(scaleOut(animationSpec = tween(280), targetScale = 1.08f) + fadeOut(animationSpec = tween(280)))
        },
        label = "appOpen"
    ) { target ->
        when (target) {
            "launcher" -> LauncherScreen(nav = { route = it })
            "pomodoro" -> PomodoroScreen(back = { route = "launcher" })
            "store" -> StoreScreen(back = { route = "launcher" })
            "settings" -> SettingsScreen(back = { route = "launcher" })
            "lockdown" -> LockdownScreen(back = { route = "launcher" })
            "stocks" -> StocksScreen(back = { route = "launcher" })
        }
    }
}