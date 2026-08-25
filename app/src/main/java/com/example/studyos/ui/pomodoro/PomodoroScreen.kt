package com.example.studyos.ui.pomodoro

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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
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
    val subject by Timer.subject.collectAsState()
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
    val bgBrush = homeBrush()
    Box(Modifier.fillMaxSize().background(bgBrush)) {
        Column(
            modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 20.dp, vertical = 12.dp),
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
                    modifier = Modifier.size(40.dp).clip(CircleShape).background(Color.White.copy(alpha = 0.2f))
                ) {
                    Icon(SIcons.Back, contentDescription = "Back", tint = Color.White)
                }
                Text(subject, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Spacer(Modifier.size(40.dp))
            }
            Box(contentAlignment = Alignment.Center, modifier = Modifier.size(230.dp)) {
                InteractiveMascot(
                    state = if (running) MascotState.STUDYING else MascotState.IDLE,
                    size = 220.dp,
                    showArc = true,
                    progressArc = if (total > 0) seconds.toFloat() / total.toFloat() else 1f
                )
            }
            Text(
                String.format("%02d:%02d", seconds / 60, seconds % 60),
                color = Color.White, fontSize = 48.sp, fontWeight = FontWeight.Bold, letterSpacing = 2.sp, textAlign = TextAlign.Center
            )
            Row(
                horizontalArrangement = Arrangement.spacedBy(28.dp),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(vertical = 4.dp)
            ) {
                IconButton(onClick = { Timer.reset() }, modifier = Modifier.size(46.dp)) {
                    Icon(SIcons.Back, contentDescription = "Reset", tint = Color.White.copy(alpha = 0.9f), modifier = Modifier.size(26.dp))
                }
                IconButton(
                    onClick = { Timer.toggle() },
                    modifier = Modifier.size(64.dp).clip(CircleShape).background(Color.White)
                ) {
                    Icon(
                        if (running) SIcons.Pause else SIcons.Play,
                        contentDescription = if (running) "Pause" else "Play",
                        tint = Color(0xFFD9534F),
                        modifier = Modifier.size(36.dp)
                    )
                }
                Spacer(Modifier.size(46.dp))
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                listOf(15, 25, 45, 60, 90, 180).forEach { mins ->
                    val isSelected = total == mins * 60
                    Surface(
                        onClick = { Timer.setMinutes(mins) },
                        shape = RoundedCornerShape(14.dp),
                        color = if (isSelected) Color.White else Color.White.copy(alpha = 0.18f),
                        modifier = Modifier.height(34.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center, modifier = Modifier.padding(horizontal = 10.dp)) {
                            Text("${mins}m", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = if (isSelected) Color(0xFF4A2C2C) else Color.White)
                        }
                    }
                }
            }
            Text(
                "Leaving the app voids the session. Finishing banks Fame.",
                color = Color.White.copy(alpha = 0.75f), fontSize = 11.sp
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
                        colors = listOf(0xFFFFD700.toInt(), 0xFFD9534F.toInt(), 0xFF4CAF50.toInt(), 0xFF20B2AA.toInt()),
                        emitter = Emitter(duration = 100, TimeUnit.MILLISECONDS).max(100),
                        position = Position.Relative(0.0, 0.0).between(Position.Relative(1.0, 0.0))
                    )
                )
            )
        }
    }
}
