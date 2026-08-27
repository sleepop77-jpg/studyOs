package com.example.studyos.ui.splash

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.studyos.ui.common.AnimatedBackground
import com.example.studyos.ui.common.RedAura
import com.example.studyos.ui.common.homeBrush
import com.example.studyos.ui.launcher.InteractiveMascot
import com.example.studyos.ui.launcher.MascotState
import kotlinx.coroutines.delay
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.min
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
    var phase by remember { mutableStateOf(0f) }
    var leaving by remember { mutableStateOf(false) }
    val fadeOut by animateFloatAsState(
        targetValue = if (leaving) 0f else 1f,
        animationSpec = tween(500, easing = FastOutSlowInEasing),
        label = "splashFade"
    )

    LaunchedEffect(Unit) {
        val steps = 90
        val stepMs = 3000L / steps
        for (i in 0..steps) {
            phase = i.toFloat() / steps.toFloat()
            delay(stepMs)
        }
        leaving = true
        delay(500)
        onDone()
    }

    val camScale = 0.92f + 0.08f * easeOut(seg(phase, 0f, 0.25f)) + 0.06f * easeIn(seg(phase, 0.86f, 1f))

    Box(modifier = Modifier.fillMaxSize().background(homeBrush()).alpha(fadeOut)) {
        AnimatedBackground()
        RedAura()

        Box(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer {
                    scaleX = camScale
                    scaleY = camScale
                }
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val cx = size.width / 2f
                val cy = size.height / 2f
                val maxR = min(size.width, size.height) * 0.55f

                for (k in 0 until 2) {
                    val t = seg(phase, 0.30f + k * 0.06f, 0.60f + k * 0.06f)
                    if (t in 0.01f..0.99f) {
                        drawCircle(
                            color = Color(0xFFFFD700).copy(alpha = (1f - t) * 0.5f),
                            radius = easeOut(t) * maxR,
                            center = Offset(cx, cy),
                            style = Stroke(width = 3.dp.toPx() * (1f - t) + 1f)
                        )
                    }
                }

                val at = seg(phase, 0.76f, 0.95f)
                if (at in 0.01f..0.99f) {
                    drawCircle(
                        color = Color(0xFFFF5252).copy(alpha = (1f - at) * 0.45f),
                        radius = easeOut(at) * maxR * 0.8f,
                        center = Offset(cx, cy),
                        style = Stroke(width = 2.5.dp.toPx() * (1f - at) + 1f)
                    )
                }

                for (i in 0 until 24) {
                    val t = seg(phase, 0.30f + (i % 6) * 0.012f, 0.75f + (i % 5) * 0.02f)
                    if (t in 0.01f..0.99f) {
                        val ang = (i / 24f) * 2f * PI.toFloat() + i * 0.7f
                        val dist = easeOut(t) * maxR * (0.5f + (i % 4) * 0.14f)
                        val px = cx + cos(ang) * dist
                        val py = cy + sin(ang) * dist * 0.8f
                        val col = when (i % 3) {
                            0 -> Color(0xFFFFD700)
                            1 -> Color(0xFF4CAF50)
                            else -> Color(0xFFFF5252)
                        }
                        drawCircle(
                            color = col.copy(alpha = (1f - t) * 0.8f),
                            radius = (2f + (i % 3) * 1f).dp.toPx() * (1f - t * 0.5f),
                            center = Offset(px, py)
                        )
                    }
                }

                val ft = seg(phase, 0.78f, 0.92f)
                if (ft in 0.01f..0.99f) {
                    val flashAlpha = sin(ft * PI.toFloat()) * 0.35f
                    drawCircle(
                        brush = Brush.radialGradient(
                            listOf(Color.White.copy(alpha = flashAlpha), Color.Transparent),
                            center = Offset(cx, cy),
                            radius = maxR * 0.9f
                        ),
                        center = Offset(cx, cy),
                        radius = maxR * 0.9f
                    )
                }
            }

            val enter = easeOutBack(seg(phase, 0.06f, 0.30f))
            val absorbT = seg(phase, 0.74f, 0.86f)
            val pulse = 1f + sin(absorbT * PI.toFloat()) * 0.12f
            val mScale = (enter * pulse).coerceAtLeast(0.001f)
            val tilt = -34f + sin(phase * PI.toFloat() * 4f) * 5f * (1f - phase)
            val bob = sin(phase * 2f * PI.toFloat() * 2f) * 6f
            val arc = easeOut(seg(phase, 0.15f, 0.9f)) * 0.85f

            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Box(
                    modifier = Modifier
                        .size(300.dp)
                        .graphicsLayer {
                            rotationY = tilt
                            scaleX = mScale
                            scaleY = mScale
                            translationY = bob.dp.toPx()
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
                repeat(10) { i ->
                    val launch = easeOut(seg(phase, 0.30f + i * 0.012f, 0.52f + i * 0.012f))
                    val ret = easeIn(seg(phase, 0.66f + i * 0.008f, 0.86f + i * 0.008f))
                    val radius = 210f * launch * (1f - ret)
                    val ang = (i / 10f) * 2f * PI.toFloat() + 0.35f
                    val dx = cos(ang) * radius
                    val dy = sin(ang) * radius * 0.85f
                    val spin = phase * 1440f + i * 36f
                    val flip = abs(cos(spin * PI.toFloat() / 180f)) * 0.85f + 0.15f
                    val sizeMul = 0.8f + (i % 4) * 0.18f
                    val alpha = seg(phase, 0.30f + i * 0.012f, 0.40f + i * 0.012f) * (1f - seg(phase, 0.80f, 0.90f))
                    val col = when (i % 5) {
                        0, 1, 3 -> Color(0xFFFFD700)
                        2 -> Color(0xFF4CAF50)
                        else -> Color(0xFFFF5252)
                    }

                    Text(
                        "\$",
                        color = col,
                        fontSize = (40 * sizeMul).sp,
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

            val wt = easeOut(seg(phase, 0.78f, 0.95f))
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
}