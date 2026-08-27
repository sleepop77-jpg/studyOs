package com.example.studyos.ui.splash

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material3.Text
import com.example.studyos.ui.launcher.InteractiveMascot
import com.example.studyos.ui.launcher.MascotState
import kotlinx.coroutines.delay
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin

@Composable
fun SplashScreen(onDone: () -> Unit) {
    var phase by remember { mutableStateOf(0f) }
    var leaving by remember { mutableStateOf(false) }
    val fadeOut by animateFloatAsState(
        targetValue = if (leaving) 0f else 1f,
        animationSpec = tween(450, easing = FastOutSlowInEasing),
        label = "splashFade"
    )

    LaunchedEffect(Unit) {
        val steps = 60
        val stepMs = 2400L / steps
        for (i in 0..steps) {
            phase = i.toFloat() / steps.toFloat()
            delay(stepMs)
        }
        leaving = true
        delay(450)
        onDone()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(Color(0xFF000000), Color(0xFF0A0A0A), Color(0xFF000000))
                )
            )
            .alpha(fadeOut),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .size(260.dp)
                .graphicsLayer {
                    rotationY = -34f
                    cameraDistance = 12f * density
                },
            contentAlignment = Alignment.Center
        ) {
            InteractiveMascot(
                state = MascotState.IDLE,
                size = 260.dp,
                showArc = false
            )
        }

        repeat(6) { i ->
            val angle = (i / 6f) * 2f * PI.toFloat()
            val delayFrac = i * 0.04f
            val localPhase = ((phase - delayFrac).coerceAtLeast(0f) / (1f - delayFrac)).coerceAtMost(1f)

            val radius = 180f
            val offset: Float
            val scaleVal: Float
            val alphaVal: Float
            when {
                localPhase < 0.4f -> {
                    val t = localPhase / 0.4f
                    val ease = FastOutSlowInEasing.transform(t)
                    offset = ease * radius
                    scaleVal = ease
                    alphaVal = ease
                }
                localPhase < 0.6f -> {
                    offset = radius
                    scaleVal = 1f
                    alphaVal = 1f
                }
                else -> {
                    val t = (localPhase - 0.6f) / 0.4f
                    val ease = FastOutSlowInEasing.transform(t)
                    offset = radius * (1f - ease)
                    scaleVal = 1f - ease * 0.7f
                    alphaVal = 1f - ease
                }
            }

            val dx = cos(angle) * offset
            val dy = sin(angle) * offset

            val spinTransition = rememberInfiniteTransition(label = "dollar_spin_$i")
            val spin by spinTransition.animateFloat(
                initialValue = 0f,
                targetValue = 360f,
                animationSpec = infiniteRepeatable(
                    animation = tween(1400, easing = LinearEasing),
                    repeatMode = RepeatMode.Restart
                ),
                label = "dollarSpin"
            )

            Text(
                "\$",
                color = Color(0xFFFFD700),
                fontSize = 44.sp,
                fontWeight = FontWeight.Black,
                modifier = Modifier
                    .offset { IntOffset(dx.dp.toPx().roundToInt(), dy.dp.toPx().roundToInt()) }
                    .graphicsLayer {
                        rotationZ = spin
                        scaleX = scaleVal * 1.2f
                        scaleY = scaleVal * 1.2f
                        this.alpha = alphaVal
                        rotationY = -34f
                        cameraDistance = 12f * density
                    }
            )
        }

        val textAlpha = if (phase > 0.7f) ((phase - 0.7f) / 0.3f).coerceAtMost(1f) else 0f
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 120.dp)
                .alpha(textAlpha),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                "StudyMore",
                color = Color.White,
                fontWeight = FontWeight.Black,
                fontSize = 28.sp,
                letterSpacing = 1.sp
            )
            Text(
                "Be the bull of your own market.",
                color = Color(0xFFFFD700),
                fontWeight = FontWeight.Medium,
                fontSize = 12.sp,
                letterSpacing = 0.8.sp
            )
        }
    }
}