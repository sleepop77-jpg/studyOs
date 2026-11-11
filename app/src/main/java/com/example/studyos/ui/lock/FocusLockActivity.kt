package com.example.studyos.ui.lock

import android.content.Context
import android.os.Build
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.studyos.core.Timer
import com.example.studyos.ui.common.AnimatedBackground
import com.example.studyos.ui.common.RedAura
import com.example.studyos.ui.common.SIcons
import com.example.studyos.ui.common.homeBrush
import com.example.studyos.ui.launcher.InteractiveMascot
import com.example.studyos.ui.launcher.MascotState

class FocusLockActivity : ComponentActivity() {
    @Suppress("DEPRECATION")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true)
            setTurnScreenOn(true)
        } else {
            window.addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED)
            window.addFlags(WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON)
        }
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        setContent {
            FocusLockScreen(onClose = { finish() })
        }
    }
}

@Composable
private fun FocusLockScreen(onClose: () -> Unit) {
    val context = LocalContext.current
    val seconds by Timer.displaySeconds.collectAsState()
    val total by Timer.total.collectAsState()
    val running by Timer.running.collectAsState()

    val preferences = remember {
        context.getSharedPreferences("focus_lock_settings", Context.MODE_PRIVATE)
    }

    var intervalMinutes by remember {
        mutableStateOf(preferences.getInt("timer_interval", 0))
    }
    var showTimerOptions by remember { mutableStateOf(false) }

    val elapsedSeconds = (total - seconds).coerceAtLeast(0)
    val showTimer = when {
        intervalMinutes == 0 -> true
        intervalMinutes < 0 -> false
        else -> elapsedSeconds % (intervalMinutes * 60) < 8
    }

    LaunchedEffect(running) {
        if (!running) {
            onClose()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(homeBrush())
    ) {
        AnimatedBackground()
        RedAura()

        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            InteractiveMascot(
                state = if (running) MascotState.STUDYING else MascotState.IDLE,
                size = 230.dp,
                showArc = true,
                progressArc = if (total > 0) seconds.toFloat() / total.toFloat() else 1f
            )

            Spacer(modifier = Modifier.height(12.dp))

            AnimatedVisibility(visible = showTimer) {
                Text(
                    String.format("%02d:%02d", seconds / 60, seconds % 60),
                    color = Color.White,
                    fontSize = 64.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 4.sp,
                    textAlign = TextAlign.Center
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
if (running) "STAY FOCUSED" else "READY",
color = Color.White.copy(alpha = 0.6f),
fontSize = 12.sp,
fontWeight = FontWeight.Black,
letterSpacing = 3.sp
)

if (originalCompleted) {
Spacer(modifier = Modifier.height(12.dp))

Column(
horizontalAlignment = Alignment.CenterHorizontally,
verticalArrangement = Arrangement.spacedBy(5.dp),
modifier = Modifier.padding(horizontal = 20.dp)
) {
Text(
"✓ Completed a ${Timer.durationLabel(originalDurationMinutes)} focus session",
color = Color(0xFF69F0AE),
fontSize = 12.sp,
fontWeight = FontWeight.Bold,
textAlign = TextAlign.Center
)

if (overtimeCompletedCount > 0) {
Text(
if (overtimeCompletedCount == 1) {
"✓ Completed one ${Timer.durationLabel(overtimeMinutes)} focus session"
} else {
"✓ Completed $overtimeCompletedCount × ${Timer.durationLabel(overtimeMinutes)} focus sessions"
},
color = Color(0xFF69F0AE),
fontSize = 12.sp,
fontWeight = FontWeight.Bold,
textAlign = TextAlign.Center
)
}

if (isOvertime && running) {
Text(
"Now studying in your ${Timer.ordinalWord(overtimeIndex)} overtime session",
color = Color(0xFF69F0AE),
fontSize = 12.sp,
fontWeight = FontWeight.Bold,
textAlign = TextAlign.Center
)
}
}
}
        }

        Row(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .fillMaxWidth()
                .padding(horizontal = 18.dp, vertical = 18.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onClose,
                modifier = Modifier.size(44.dp)
            ) {
                Icon(
                    imageVector = SIcons.Close,
                    contentDescription = "Dismiss focus lock screen",
                    tint = Color.White,
                    modifier = Modifier.size(22.dp)
                )
            }

            IconButton(
                onClick = { showTimerOptions = true },
                modifier = Modifier.size(44.dp)
            ) {
                Icon(
                    imageVector = SIcons.Timer,
                    contentDescription = "Timer visibility settings",
                    tint = Color.White,
                    modifier = Modifier.size(22.dp)
                )
            }
        }
    }

    if (showTimerOptions) {
        AlertDialog(
            onDismissRequest = { showTimerOptions = false },
            title = { Text("Timer visibility") },
            text = {
                Column {
                    val options = listOf(
                        0 to "Always",
                        10 to "Every 10 minutes",
                        20 to "Every 20 minutes",
                        30 to "Every 30 minutes",
                        45 to "Every 45 minutes",
                        60 to "Every 60 minutes",
                        -1 to "Never"
                    )

                    options.forEach { (value, label) ->
                        TextButton(
                            onClick = {
                                intervalMinutes = value
                                preferences.edit()
                                    .putInt("timer_interval", value)
                                    .apply()
                                showTimerOptions = false
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = if (intervalMinutes == value) "✓  $label" else label,
                                color = Color.White
                            )
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showTimerOptions = false }) {
                    Text("Close")
                }
            },
            containerColor = Color(0xFF241820),
            titleContentColor = Color.White,
            textContentColor = Color.White
        )
    }
}
