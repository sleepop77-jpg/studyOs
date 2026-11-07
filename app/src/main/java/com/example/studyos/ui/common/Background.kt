package com.example.studyos.ui.common

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import com.example.studyos.core.Store
import com.example.studyos.core.VisualSettings
import kotlin.math.sin

@Composable
fun homeBrush(): Brush {
    val theme by Store.equippedTheme.collectAsState(initial = null)

    return when (theme) {
        "item_aurora_dream" -> Brush.verticalGradient(
            listOf(Color(0xFF010308), Color(0xFF041018), Color(0xFF010308))
        )
        "item_math_matrix" -> Brush.verticalGradient(
            listOf(Color(0xFF000000), Color(0xFF021002), Color(0xFF000000))
        )
        "item_spanish_fiesta" -> Brush.verticalGradient(
            listOf(Color(0xFF1A0D0B), Color(0xFF573026), Color(0xFF2B1824))
        )
        "theme_void_nexus" -> Brush.verticalGradient(
            listOf(Color(0xFF05030B), Color(0xFF17102D), Color(0xFF030208))
        )
        "theme_sakura_drift" -> Brush.verticalGradient(
            listOf(Color(0xFF080B18), Color(0xFF241629), Color(0xFF080B18))
        )
        "theme_crimson_focus" -> Brush.verticalGradient(
            listOf(Color(0xFF000000), Color(0xFF140505), Color(0xFF1D0505))
        )
        else -> Brush.verticalGradient(
            listOf(Color(0xFF000000), Color(0xFF0A0A0A), Color(0xFF000000))
        )
    }
}

@Composable
fun AnimatedBackground() {
    val theme by Store.equippedTheme.collectAsState(initial = null)
    val motionMode by VisualSettings.motionMode.collectAsState()
    val brightness by VisualSettings.effectBrightness.collectAsState()

    val particleAccent = when (theme) {
        "item_aurora_dream" -> Color(0xFF40C4FF)
        "item_math_matrix" -> Color(0xFF00E676)
        "item_spanish_fiesta" -> Color(0xFFFFA726)
        "theme_void_nexus" -> Color(0xFFB388FF)
        "theme_sakura_drift" -> Color(0xFFFF80AB)
        "theme_crimson_focus" -> Color(0xFFE53935)
        else -> Color(0xFFFFD180)
    }

    val transition = rememberInfiniteTransition(label = "bg_particles")
    val phase by transition.animateFloat(
        initialValue = 0f,
        targetValue = if (motionMode == 2) 0f else 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = if (motionMode == 1) 40000 else 12000,
                easing = LinearEasing
            ),
            repeatMode = RepeatMode.Restart
        ),
        label = "particle_phase"
    )

    Canvas(modifier = Modifier.fillMaxSize()) {
        val w = size.width
        val h = size.height

        val starCount = when (motionMode) {
            0 -> 35
            1 -> 10
            else -> 10
        }

        for (i in 0 until starCount) {
            val baseX = ((i * 47) % 100) / 100f * w
            val baseY = ((i * 89) % 100) / 100f * h
            val floatY = (phase + i * 0.05f) % 1f
            val y = baseY - floatY * h * 0.15f
            val alpha = (
                0.15f + 0.25f *
                    ((sin((phase + i * 0.1f) * 6.28f) + 1f) / 2f)
                ) * brightness

            val particleSize = (1.5f + (i % 3) * 0.5f).dp.toPx()

            drawCircle(
                color = Color.White.copy(alpha = alpha),
                radius = particleSize,
                center = Offset(baseX, y)
            )
        }

        val goldCount = when (motionMode) {
            0 -> 20
            1 -> 3
            else -> 0
        }

        for (i in 0 until goldCount) {
            val x = ((i * 113) % 100) / 100f * w
            val yBase = ((i * 71) % 100) / 100f * h
            val y = yBase - ((phase + i * 0.08f) % 1f) * h * 0.12f
            val alpha = (
                0.08f + 0.12f *
                    ((sin((phase + i * 0.15f) * 6.28f) + 1f) / 2f)
                ) * brightness

            drawCircle(
                color = Color(0xFFFFD700).copy(alpha = alpha * 0.6f),
                radius = 1.3f.dp.toPx(),
                center = Offset(x, y)
            )
        }

        val ringCount = when (motionMode) {
            0 -> 12
            1 -> 4
            else -> 4
        }

        for (i in 0 until ringCount) {
            val x = ((i * 157) % 100) / 100f * w
            val yBase = ((i * 131) % 100) / 100f * h
            val y = yBase - ((phase + i * 0.06f) % 1f) * h * 0.10f
            val alpha = (
                0.05f + 0.15f *
                    ((sin((phase + i * 0.12f) * 6.28f) + 1f) / 2f)
                ) * brightness

            drawCircle(
                color = Color.White.copy(alpha = alpha),
                radius = (3f + (i % 3) * 2f).dp.toPx(),
                center = Offset(x, y),
                style = Stroke(width = 1.dp.toPx())
            )
        }

        val diamondCount = when (motionMode) {
            0 -> 10
            1 -> 3
            else -> 3
        }

        for (i in 0 until diamondCount) {
            val x = ((i * 211) % 100) / 100f * w
            val yBase = ((i * 173) % 100) / 100f * h
            val y = yBase - ((phase + i * 0.04f) % 1f) * h * 0.08f
            val alpha = (
                0.05f + 0.10f *
                    ((sin((phase + i * 0.2f) * 6.28f) + 1f) / 2f)
                ) * brightness

            val diamondSize = 2.5f.dp.toPx()
            val path = Path().apply {
                moveTo(x, y - diamondSize)
                lineTo(x + diamondSize, y)
                lineTo(x, y + diamondSize)
                lineTo(x - diamondSize, y)
                close()
            }

            drawPath(
                path = path,
                color = particleAccent.copy(alpha = alpha)
            )
        }

        val shootingStarCount = if (motionMode == 0) 8 else 0

        for (i in 0 until shootingStarCount) {
            val startX = ((i * 89) % 100) / 100f * w
            val startY = ((i * 37) % 30) / 100f * h
            val shootPhase = (phase * 3f + i * 0.35f) % 1f
            val x = startX + shootPhase * w * 0.6f
            val y = startY + shootPhase * h * 0.4f

            val alpha = (
                if (shootPhase < 0.3f) shootPhase / 0.3f
                else if (shootPhase > 0.7f) (1f - shootPhase) / 0.3f
                else 1f
                ) * brightness

            val tailLength = 40f.dp.toPx()
            val tailX = x - tailLength * 0.7f
            val tailY = y - tailLength * 0.5f

            drawCircle(
                color = Color.White.copy(alpha = alpha),
                radius = 2.5f.dp.toPx(),
                center = Offset(x, y)
            )

            drawLine(
                color = Color(0xFFFFD700).copy(alpha = alpha * 0.6f),
                start = Offset(x, y),
                end = Offset(tailX, tailY),
                strokeWidth = 1.5f.dp.toPx()
            )
        }
    }
}

@Composable
fun RedAura() {
    val theme by Store.equippedTheme.collectAsState(initial = null)
    val brightness by VisualSettings.effectBrightness.collectAsState()

    val auraColor = when (theme) {
        "item_aurora_dream" -> Color(0xFF26C6DA)
        "item_math_matrix" -> Color(0xFF43A047)
        "item_spanish_fiesta" -> Color(0xFFFFA726)
        "theme_void_nexus" -> Color(0xFF9C6ADE)
        "theme_sakura_drift" -> Color(0xFFE68AB5)
        "theme_crimson_focus" -> Color(0xFFD9534F)
        else -> Color(0xFFFFD180)
    }

    Canvas(modifier = Modifier.fillMaxSize()) {
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    auraColor.copy(alpha = 0.12f * brightness),
                    auraColor.copy(alpha = 0.045f * brightness),
                    Color.Transparent
                ),
                center = Offset(size.width * 0.5f, size.height * 0.3f),
                radius = size.width * 0.6f
            ),
            center = Offset(size.width * 0.5f, size.height * 0.3f),
            radius = size.width * 0.6f
        )
    }
}

@Composable
fun RedPatchesBackground() {
    val theme by Store.equippedTheme.collectAsState(initial = null)
    val brightness by VisualSettings.effectBrightness.collectAsState()
    val isCrimson = theme == "theme_crimson_focus"

    val c1 = if (isCrimson) Color(0xFFC41C3B) else Color.White
    val c2 = if (isCrimson) Color(0xFF8B0000) else Color(0xFF9A9A9A)
    val a1 = (if (isCrimson) 0.15f else 0.05f) * brightness
    val a2 = (if (isCrimson) 0.12f else 0.04f) * brightness

    Canvas(modifier = Modifier.fillMaxSize()) {
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(c1.copy(alpha = a1), Color.Transparent),
                center = Offset(size.width * 0.85f, size.height * 0.12f),
                radius = size.width * 0.55f
            ),
            center = Offset(size.width * 0.85f, size.height * 0.12f),
            radius = size.width * 0.55f
        )

        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(c2.copy(alpha = a2), Color.Transparent),
                center = Offset(size.width * 0.12f, size.height * 0.88f),
                radius = size.width * 0.65f
            ),
            center = Offset(size.width * 0.12f, size.height * 0.88f),
            radius = size.width * 0.65f
        )
    }
}
