package com.example.studyos.ui.pomodoro

import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.studyos.core.Timer
import com.example.studyos.ui.common.RedPatchesBackground
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
    val progress = if (total > 0) 1f - (seconds.toFloat() / total.toFloat()) else 0f

    var celebrate by remember { mutableStateOf(false) }
    var wasRunning by remember { mutableStateOf(false) }
    var sliderMinutes by remember(total) {
        mutableStateOf((total / 60).toFloat().coerceIn(1f, 180f))
    }

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

    Box(Modifier.fillMaxSize().background(bgBrush)) {
        RedPatchesBackground()

        Column(
            modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 24.dp, vertical = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = back,
                    enabled = canControl,
                    modifier = Modifier.size(42.dp).clip(CircleShape).background(Color.White.copy(alpha = 0.12f))
                ) {
                    Icon(SIcons.Back, contentDescription = "Back", tint = if (canControl) Color.White else Color.White.copy(alpha = 0.35f), modifier = Modifier.size(20.dp))
                }

                Text(subject, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 15.sp, letterSpacing = 0.5.sp)

                Spacer(Modifier.size(42.dp))
            }

            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.08f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text("STRICT MODE", color = Color.White, fontWeight = FontWeight.Black, fontSize = 12.sp, letterSpacing = 1.sp)
                        Text(
                            if (strict && running) "Locked. No pause/reset until finished."
                            else if (strict) "Once started, this session cannot be paused."
                            else "Normal timer controls.",
                            color = Color.White.copy(alpha = 0.65f),
                            fontSize = 11.sp
                        )
                    }

                    Switch(
                        checked = strict,
                        onCheckedChange = { Timer.setStrict(it) },
                        enabled = !(running && strict),
                        colors = SwitchDefaults.colors(
                            checkedTrackColor = Color(0xFFE53935),
                            checkedThumbColor = Color.White,
                            uncheckedTrackColor = Color.White.copy(alpha = 0.25f),
                            uncheckedThumbColor = Color.White
                        )
                    )
                }
            }

            Box(contentAlignment = Alignment.Center, modifier = Modifier.size(240.dp)) {
                InteractiveMascot(
                    state = if (running) MascotState.STUDYING else MascotState.IDLE,
                    size = 240.dp,
                    showArc = true,
                    progressArc = if (total > 0) seconds.toFloat() / total.toFloat() else 1f
                )
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    String.format("%02d:%02d", seconds / 60, seconds % 60),
                    color = Color.White, fontSize = 56.sp, fontWeight = FontWeight.Black, letterSpacing = 3.sp, textAlign = TextAlign.Center
                )

                Text(
                    if (running) "FOCUSING" else "READY",
                    color = if (running) Color(0xFF4CAF50) else Color.White.copy(alpha = 0.6f),
                    fontSize = 11.sp, fontWeight = FontWeight.Black, letterSpacing = 2.sp
                )
            }

            LeftToRightProgressBar(progress = progress)

            Row(
                horizontalArrangement = Arrangement.spacedBy(32.dp),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(vertical = 8.dp)
            ) {
                IconButton(
                    onClick = { Timer.reset() },
                    enabled = canControl,
                    modifier = Modifier.size(48.dp).clip(CircleShape).background(Color.White.copy(alpha = 0.08f))
                ) {
                    Icon(SIcons.Back, contentDescription = "Reset", tint = if (canControl) Color.White.copy(alpha = 0.9f) else Color.White.copy(alpha = 0.3f), modifier = Modifier.size(24.dp))
                }

                IconButton(
                    onClick = { Timer.toggle() },
                    enabled = canControl,
                    modifier = Modifier.size(72.dp).clip(CircleShape).background(Color.White)
                ) {
                    Icon(
                        if (running) SIcons.Pause else SIcons.Play,
                        contentDescription = if (running) "Pause" else "Play",
                        tint = if (canControl) Color(0xFFD9534F) else Color(0xFFD9534F).copy(alpha = 0.4f),
                        modifier = Modifier.size(36.dp)
                    )
                }

                Spacer(Modifier.size(48.dp))
            }

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf(15, 25, 45, 60, 90, 180).forEach { mins ->
                    val isSelected = total == mins * 60

                    Surface(
                        onClick = {
                            if (canControl) {
                                Timer.setMinutes(mins)
                                sliderMinutes = mins.toFloat()
                            }
                        },
                        shape = RoundedCornerShape(14.dp),
                        color = if (isSelected && canControl) Color.White else Color.White.copy(alpha = 0.08f),
                        modifier = Modifier.weight(1f).height(40.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                "${mins}m",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isSelected && canControl) Color(0xFF4A2C2C) else Color.White.copy(alpha = if (canControl) 1f else 0.45f),
                                letterSpacing = 0.5.sp
                            )
                        }
                    }
                }
            }

            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.08f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text("CUSTOM TIMER", color = Color.White, fontWeight = FontWeight.Black, fontSize = 12.sp, letterSpacing = 1.sp)
                    Text("Max 3 hours. Strict Mode locks the timer once it starts.", color = Color.White.copy(alpha = 0.65f), fontSize = 11.sp)

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = { sliderMinutes = (sliderMinutes - 5f).coerceIn(1f, 180f) },
                            enabled = canControl,
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1A0505), contentColor = Color.White),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.width(64.dp).height(36.dp)
                        ) {
                            Text("-5", fontSize = 11.sp, fontWeight = FontWeight.Black)
                        }

                        Text(
                            "${sliderMinutes.toInt()} min",
                            color = Color.White,
                            fontWeight = FontWeight.Black,
                            fontSize = 16.sp,
                            modifier = Modifier.weight(1f),
                            textAlign = TextAlign.Center
                        )

                        Button(
                            onClick = { sliderMinutes = (sliderMinutes + 5f).coerceIn(1f, 180f) },
                            enabled = canControl,
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1A0505), contentColor = Color.White),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.width(64.dp).height(36.dp)
                        ) {
                            Text("+5", fontSize = 11.sp, fontWeight = FontWeight.Black)
                        }
                    }

                    Slider(
                        value = sliderMinutes,
                        onValueChange = { sliderMinutes = it.coerceIn(1f, 180f) },
                        valueRange = 1f..180f,
                        enabled = canControl,
                        colors = SliderDefaults.colors(
                            thumbColor = Color(0xFFE53935),
                            activeTrackColor = Color(0xFFE53935),
                            inactiveTrackColor = Color.White.copy(alpha = 0.12f),
                            disabledThumbColor = Color.White.copy(alpha = 0.3f),
                            disabledActiveTrackColor = Color(0xFFE53935).copy(alpha = 0.4f),
                            disabledInactiveTrackColor = Color.White.copy(alpha = 0.08f)
                        )
                    )

                    Button(
                        onClick = { Timer.setMinutes(sliderMinutes.toInt()) },
                        enabled = canControl && sliderMinutes.toInt() != total / 60,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE53935), contentColor = Color.White),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth().height(44.dp)
                    ) {
                        Text("SET CUSTOM TIMER", fontWeight = FontWeight.Black, fontSize = 13.sp, letterSpacing = 0.5.sp)
                    }
                }
            }

            Box(
                modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).background(Color.White.copy(alpha = 0.06f)).padding(14.dp)
            ) {
                Text(
                    "Leaving the app voids the session. Finishing banks Fame.",
                    color = Color.White.copy(alpha = 0.65f), fontSize = 11.sp, fontWeight = FontWeight.Medium, letterSpacing = 0.3.sp, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth()
                )
            }
        }

        if (celebrate) {
            KonfettiView(
                modifier = Modifier.fillMaxSize(),
                parties = listOf(
                    Party(
                        speed = 0f, maxSpeed = 30f, damping = 0.9f, spread = Spread.ROUND,
                        colors = listOf(0xFFFFD700.toInt(), 0xFFD9534F.toInt(), 0xFF4CAF50.toInt(), 0xFF20B2AA.toInt()),
                        emitter = Emitter(duration = 100, TimeUnit.MILLISECONDS).max(100),
                        position = Position.Relative(0.0, 0.0).between(Position.Relative(1.0, 0.0))
                    )
                )
            )
        }
    }
}

@Composable
private fun LeftToRightProgressBar(progress: Float) {
    val transition = rememberInfiniteTransition(label = "progress_shine")
    val shine by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1400, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "progress_shine_phase"
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(12.dp)
            .clip(RoundedCornerShape(6.dp))
            .background(Color.White.copy(alpha = 0.08f))
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(progress.coerceIn(0f, 1f))
                .height(12.dp)
                .background(
                    Brush.horizontalGradient(
                        listOf(
                            Color(0xFF7F0F1F),
                            Color(0xFFE53935)
                        )
                    )
                )
        )

        Canvas(modifier = Modifier.fillMaxSize()) {
            val shineWidth = size.width * 0.18f
            val x = shine * (size.width + shineWidth) - shineWidth

            drawRect(
                brush = Brush.horizontalGradient(
                    colors = listOf(
                        Color.Transparent,
                        Color.White.copy(alpha = 0.35f),
                        Color.Transparent
                    ),
                    startX = x,
                    endX = x + shineWidth
                ),
                topLeft = Offset(x, 0f),
                size = Size(shineWidth, size.height)
            )
        }
    }
}
