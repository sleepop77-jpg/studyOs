package com.example.studyos.ui.splash

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.rememberLottieComposition
import com.example.studyos.ui.common.AnimatedBackground
import com.example.studyos.ui.common.RedAura
import com.example.studyos.ui.common.homeBrush
import com.example.studyos.ui.launcher.InteractiveMascot
import com.example.studyos.ui.launcher.MascotState
import kotlinx.coroutines.delay
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin

private fun clamp01(v: Float) = v.coerceIn(0f, 1f)
private fun seg(p: Float, a: Float, b: Float) = clamp01((p - a) / (b - a))
private fun easeOut(t: Float) = 1f - (1f - t) * (1f - t) * (1f - t)
private fun easeIn(t: Float) = t * t * t
private fun easeOutBack(t: Float): Float {
    val c1 = 1.70158f
    val c3 = c1 + 1f
    val u = t - 1f
    return 1f + c3 * u * u * u + c1 * u * u
}

@Composable
fun SplashScreen(onDone: () -> Unit) {
    var started by remember { mutableStateOf(false) }
    var leaving by remember { mutableStateOf(false) }

    val phase by animateFloatAsState(
        targetValue = if (started) 1f else 0f,
        animationSpec = tween(4500, easing = LinearEasing),
        label = "splashPhase",
        finishedListener = { leaving = true }
    )

    val fadeOut by animateFloatAsState(
        targetValue = if (leaving) 0f else 1f,
        animationSpec = tween(600, easing = FastOutSlowInEasing),
        label = "splashFade"
    )

    LaunchedEffect(Unit) { started = true }
    LaunchedEffect(leaving) {
        if (leaving) {
            delay(600)
            onDone()
        }
    }

    val composition by rememberLottieComposition(LottieCompositionSpec.Asset("splash_fx.json"))

    Box(modifier = Modifier.fillMaxSize().background(homeBrush()).alpha(fadeOut)) {
        AnimatedBackground()
        RedAura()

        LottieAnimation(
            composition = composition,
            progress = { phase },
            modifier = Modifier.fillMaxSize()
        )

        val enter = easeOutBack(seg(phase, 0.05f, 0.25f))
        val absorbT = seg(phase, 0.72f, 0.82f)
        val pulse = 1f + sin(absorbT * PI.toFloat()) * 0.15f
        val mScale = (enter * pulse).coerceAtLeast(0.001f)
        val tilt = -34f + sin(phase * PI.toFloat() * 4f) * 5f * (1f - phase)
        val arc = easeOut(seg(phase, 0.15f, 0.9f)) * 0.85f

        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Box(
                modifier = Modifier
                    .size(300.dp)
                    .graphicsLayer {
                        rotationY = tilt
                        scaleX = mScale
                        scaleY = mScale
                        cameraDistance = 10f * density
                    },
                contentAlignment = Alignment.Center
            ) {
                InteractiveMascot(
                    state = if (phase > 0.8f) MascotState.STUDYING else MascotState.IDLE,
                    size = 300.dp,
                    showArc = true,
                    progressArc = arc
                )
            }
        }

        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            repeat(8) { i ->
                val launch = easeOut(seg(phase, 0.25f + i * 0.012f, 0.45f + i * 0.012f))
                val ret = easeIn(seg(phase, 0.55f + i * 0.008f, 0.75f + i * 0.008f))
                val radius = 200f * launch * (1f - ret)
                val ang = (i / 8f) * 2f * PI.toFloat() + 0.4f
                val dx = cos(ang) * radius
                val dy = sin(ang) * radius * 0.85f
                val spin = phase * 1440f + i * 45f
                val flip = abs(cos(spin * PI.toFloat() / 180f)) * 0.85f + 0.15f
                val sizeMul = 0.8f + (i % 4) * 0.2f
                val alpha = seg(phase, 0.25f + i * 0.012f, 0.35f + i * 0.012f) * (1f - seg(phase, 0.70f, 0.75f))
                val col = when (i % 5) {
                    0, 1, 3 -> Color(0xFFFFD700)
                    2 -> Color(0xFF4CAF50)
                    else -> Color(0xFFFF5252)
                }

                Text(
                    "\$",
                    color = col,
                    fontSize = (42 * sizeMul).sp,
                    fontWeight = FontWeight.Black,
                    modifier = Modifier
                        .offset { IntOffset(dx.dp.toPx().roundToInt(), dy.dp.toPx().roundToInt()) }
                        .graphicsLayer {
                            scaleX = flip * sizeMul
                            scaleY = sizeMul
                            this.alpha = alpha
                            rotationZ = sin(spin * PI.toFloat() / 180f) * 18f
                            cameraDistance = 10f * density
                        }
                )
            }
        }

        val wt = easeOut(seg(phase, 0.82f, 0.94f))
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 110.dp)
                .graphicsLayer {
                    translationY = (1f - wt) * 80f * density
                    scaleX = 0.9f + 0.1f * wt
                    scaleY = 0.9f + 0.1f * wt
                    alpha = wt
                },
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                "StudyMore",
                color = Color.White,
                fontWeight = FontWeight.Black,
                fontSize = 34.sp,
                letterSpacing = 1.5.sp
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