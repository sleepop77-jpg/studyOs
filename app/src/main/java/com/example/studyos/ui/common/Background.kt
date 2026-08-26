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
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import com.example.studyos.core.Store
import kotlin.math.sin
import kotlin.math.cos

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
            listOf(Color(0xFF0B0303), Color(0xFF1D0505), Color(0xFF0B0303))
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
    val transition = rememberInfiniteTransition(label = "bg_particles")
    val phase by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(12000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "particle_phase"
    )

    Canvas(modifier = Modifier.fillMaxSize()) {
        val w = size.width
        val h = size.height

        // Floating stars
        for (i in 0 until 35) {
            val baseX = ((i * 47) % 100) / 100f * w
            val baseY = ((i * 89) % 100) / 100f * h
            val floatY = (phase + i * 0.05f) % 1f
            val y = baseY - floatY * h * 0.15f
            val alpha = 0.15f + 0.25f * ((sin((phase + i * 0.1f) * 6.28f) + 1f) / 2f)
            val size = (1.5f + (i % 3) * 0.5f).dp.toPx()

            drawCircle(
                color = Color.White.copy(alpha = alpha),
                radius = size,
                center = Offset(baseX, y)
            )
        }

        // Floating Gold Particles
        for (i in 0 until 20) {
            val baseX = ((i * 113) % 100) / 100f * w
            val baseY = ((i * 71) % 100) / 100f * h
            val floatY = (phase + i * 0.08f) % 1f
            val y = baseY - floatY * h * 0.12f
            val alpha = 0.08f + 0.12f * ((sin((phase + i * 0.15f) * 6.28f) + 1f) / 2f)
            val size = (1.0f + (i % 2) * 0.4f).dp.toPx()

            drawCircle(
                color = Color(0xFFFFD700).copy(alpha = alpha * 0.6f),
                radius = size,
                center = Offset(baseX, y)
            )
        }

        // Floating Rings (Random Things)
        for (i in 0 until 12) {
            val baseX = ((i * 157) % 100) / 100f * w
            val baseY = ((i * 131) % 100) / 100f * h
            val floatY = (phase + i * 0.06f) % 1f
            val y = baseY - floatY * h * 0.10f
            val alpha = 0.05f + 0.15f * ((sin((phase + i * 0.12f) * 6.28f) + 1f) / 2f)
            val size = (3f + (i % 3) * 2f).dp.toPx()

            drawCircle(
                color = Color.White.copy(alpha = alpha),
                radius = size,
                center = Offset(baseX, y),
                style = Stroke(width = 1.dp.toPx())
            )
        }
        
        // Floating Diamonds
        for (i in 0 until 10) {
            val baseX = ((i * 211) % 100) / 100f * w
            val baseY = ((i * 173) % 100) / 100f * h
            val floatY = (phase + i * 0.04f) % 1f
            val y = baseY - floatY * h * 0.08f
            val alpha = 0.05f + 0.10f * ((sin((phase + i * 0.2f) * 6.28f) + 1f) / 2f)
            val size = 2.5f.dp.toPx()
            
            val path = Path().apply {
                moveTo(baseX, y - size)
                lineTo(baseX + size, y)
                lineTo(baseX, y + size)
                lineTo(baseX - size, y)
                close()
            }
            drawPath(path = path, color = Color(0xFFE53935).copy(alpha = alpha))
        }
    }
}

@Composable
fun RedAura() {
    Canvas(modifier = Modifier.fillMaxSize()) {
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    Color(0xFFD9534F).copy(alpha = 0.18f),
                    Color(0xFFD9534F).copy(alpha = 0.08f),
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
    val isCrimson = theme == "theme_crimson_focus"

    val c1 = if (isCrimson) Color(0xFFC41C3B) else Color(0xFFFFFFFF)
    val c2 = if (isCrimson) Color(0xFF8B0000) else Color(0xFF9A9A9A)
    val a1 = if (isCrimson) 0.15f else 0.05f
    val a2 = if (isCrimson) 0.12f else 0.04f

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