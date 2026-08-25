package com.example.studyos.ui.pomodoro

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.studyos.core.Timer
import com.example.studyos.ui.common.SIcons
import com.example.studyos.ui.common.homeBrush
import com.example.studyos.ui.launcher.InteractiveMascot
import com.example.studyos.ui.launcher.MascotState
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.delay
import nl.dionsegijn.konfetti.compose.KonfettiView
import nl.dionsegijn.konfetti.core.Party
import nl.dionsegijn.konfetti.core.Position
import nl.dionsegijn.konfetti.core.Spread
import nl.dionsegijn.konfetti.core.emitter.Emitter

@Composable
fun PomodoroScreen(back: () -> Unit) {
    val seconds by Timer.seconds.collectAsState()
    val total by Timer.total.collectAsState()
    val running by Timer.running.collectAsState()
    val strict by Timer.strict.collectAsState()
    val subject by Timer.subject.collectAsState()
    val canControl = !(strict && running)

    var celebrate by remember { mutableStateOf(false) }
    var wasRunning by remember { mutableStateOf(false) }

    LaunchedEffect(running) {
        if (wasRunning && !running && seconds >= total) {
            celebrate = true
            delay(3000)
            celebrate = false
        }
        wasRunning = running
    }

    BackHandler(enabled = strict && running) { }

    val bgBrush = homeBrush()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(bgBrush)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = back,
                    enabled = canControl,
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.2f))
                ) {
                    Icon(
                        imageVector = SIcons.Back,
                        contentDescription = "Back",
                        tint = if (canControl) Color.White else Color.White.copy(alpha = 0.35f)
                    )
                }

                Text(
                    text = subject,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )

                Spacer(modifier = Modifier.size(40.dp))
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color.White.copy(alpha = 0.16f))
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "STRICT MODE",
                        color = Color.White,
                        fontWeight = FontWeight.Black,
                        fontSize = 12.sp,
                        letterSpacing = 1.sp
                    )

                    Text(
                        text = if (strict && running) {
                            "Locked. No pause/reset until finished."
                        } else if (strict) {
                            "Once started, this session cannot be paused."
                        } else {
                            "Normal timer controls."
                        },
                        color = Color.White.copy(alpha = 0.75f),
                        fontSize = 11.sp
                    )
                }

                Switch(
                    checked = strict,
                    onCheckedChange = { Timer.setStrict(it) },
                    enabled = !(running && strict),
                    colors = SwitchDefaults.colors(
                        checkedTrackColor = Color(0xFFC41C3B),
                        checkedThumbColor = Color.White,
                        uncheckedTrackColor = Color.White.copy(alpha = 0.3f),
                        uncheckedThumbColor = Color.White
                    )
                )
            }

            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.size(230.dp)
            ) {
                InteractiveMascot(
                    state = if (running) MascotState.STUDYING else MascotState.IDLE,
                    size = 220.dp,
                    showArc = true,
                    progressArc = if (total > 0) seconds.toFloat() / total.toFloat() else 1f
                )
            }

            Text(
                text = String.format("%02d:%02d", seconds / 60, seconds % 60),
                color = Color.White,
                fontSize = 48.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 2.sp,
                textAlign = TextAlign.Center
            )

            Row(
                horizontalArrangement = Arrangement.spacedBy(28.dp),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(vertical = 4.dp)
            ) {
                IconButton(
                    onClick = { Timer.reset() },
                    enabled = canControl,
                    modifier = Modifier.size(46.dp)
                ) {
                    Icon(
                        imageVector = SIcons.Back,
                        contentDescription = "Reset",
                        tint = if (canControl) Color.White.copy(alpha = 0.9f) else Color.White.copy(alpha = 0.35f),
                        modifier = Modifier.size(26.dp)
                    )
                }

                IconButton(
                    onClick = { Timer.toggle() },
                    enabled = canControl,
                    modifier = Modifier
                        .size(64.dp)
                        .clip(CircleShape)
                        .background(Color.White)
                ) {
                    Icon(
                        imageVector = if (running) SIcons.Pause else SIcons.Play,
                        contentDescription = if (running) "Pause" else "Play",
                        tint = if (canControl) Color(0xFFD9534F) else Color(0xFFD9534F).copy(alpha = 0.4f),
                        modifier = Modifier.size(36.dp)
                    )
                }

                Spacer(modifier = Modifier.size(46.dp))
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf(15, 25, 45, 60, 90, 180).forEach { mins ->
                    val isSelected = total == mins * 60

                    Surface(
                        onClick = { if (canControl) Timer.setMinutes(mins) },
                        shape = RoundedCornerShape(14.dp),
                        color = if (isSelected && canControl) Color.White else Color.White.copy(alpha = 0.18f),
                        modifier = Modifier.height(34.dp)
                    ) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier.padding(horizontal = 10.dp)
                        ) {
                            Text(
                                text = "${mins}m",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isSelected && canControl) Color(0xFF4A2C2C) else Color.White.copy(alpha = if (canControl) 1f else 0.45f)
                            )
                        }
                    }
                }
            }

            Text(
                text = if (strict && running) {
                    "Strict session active. It will not pause. Finishing banks Fame."
                } else {
                    "Leaving the app voids the session. Finishing banks Fame."
                },
                color = Color.White.copy(alpha = 0.75f),
                fontSize = 11.sp
            )
        }

        if (celebrate) {
            KonfettiView(
                modifier = Modifier.fillMaxSize(),
                parties = listOf(
                    Party(
                        speed = 0f,
                        maxSpeed = 30f,
                        damping = 0.9f,
                        spread = Spread.ROUND,
                        colors = listOf(
                            0xFFFFD700.toInt(),
                            0xFFD9534F.toInt(),
                            0xFF4CAF50.toInt(),
                            0xFF20B2AA.toInt()
                        ),
                        emitter = Emitter(100L, TimeUnit.MILLISECONDS).max(100),
                        position = Position.Relative(0.0, 0.0).between(Position.Relative(1.0, 0.0))
                    )
                )
            )
        }
    }
}
