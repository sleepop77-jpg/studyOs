package com.example.studyos.ui.launcher

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlin.math.cos
import kotlin.math.sin

object AnimatedSkins {
    const val HALO = "item_halo_scholar"
    const val NINJA = "item_ninja_headband"
    const val PARTY = "item_party_mode"
    const val AURORA = "item_aurora_dream"
    val MASCOT_SET = setOf(HALO, NINJA, PARTY)
}

fun DrawScope.drawExtraSkins(skin: String?, cx: Float, cy: Float, w: Float, h: Float, headCenter: Offset, phase: Float) {
    when (skin) {
        AnimatedSkins.HALO -> drawScholarHalo(headCenter, w, h, phase)
        AnimatedSkins.NINJA -> drawNinjaHeadband(headCenter, w, h, phase)
        AnimatedSkins.PARTY -> drawPartyMode(cx, cy, w, h, headCenter, phase)
    }
}

private fun DrawScope.drawScholarHalo(headCenter: Offset, w: Float, h: Float, phase: Float) {
    val t = phase * 2f * Math.PI.toFloat()
    val bob = sin(t) * 2.5f.dp.toPx()
    val haloCx = headCenter.x
    val haloCy = headCenter.y - h * 0.30f + bob
    val rx = w * 0.15f
    val ry = rx * 0.34f
    val pulse = 0.55f + 0.45f * ((sin(t * 2f) + 1f) / 2f)

    drawOval(color = Color(0xFFFFD700).copy(alpha = 0.22f * pulse), topLeft = Offset(haloCx - rx * 1.5f, haloCy - ry * 1.5f), size = Size(rx * 3f, ry * 3f))
    drawOval(color = Color(0xFFFFD700), topLeft = Offset(haloCx - rx, haloCy - ry), size = Size(rx * 2f, ry * 2f), style = Stroke(width = 3.5f.dp.toPx(), cap = StrokeCap.Round))

    for (i in 0 until 3) {
        val ang = t + i * (2f * Math.PI.toFloat() / 3f)
        drawCircle(color = Color(0xFFFFF9C4).copy(alpha = pulse), radius = 2f.dp.toPx(), center = Offset(haloCx + cos(ang) * rx * 1.45f, haloCy + sin(ang) * ry * 1.45f))
    }
}

private fun DrawScope.drawNinjaHeadband(headCenter: Offset, w: Float, h: Float, phase: Float) {
    val t = phase * 2f * Math.PI.toFloat()
    val hrX = w * 0.23f
    val bandY = headCenter.y - h * 0.10f
    val bandH = 6f.dp.toPx()

    val auraPulse = 0.4f + 0.6f * ((sin(t * 1.5f) + 1f) / 2f)
    val auraColor = Color(0xFF8B1414)
    
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(
                auraColor.copy(alpha = 0.35f * auraPulse),
                auraColor.copy(alpha = 0.15f * auraPulse),
                Color.Transparent
            ),
            center = headCenter,
            radius = w * 0.45f
        ),
        center = headCenter,
        radius = w * 0.45f
    )

    for (i in 0 until 8) {
        val particleT = (t + i * 0.8f) % (2f * Math.PI.toFloat())
        val particleRadius = w * 0.28f + sin(particleT * 2f) * 8f.dp.toPx()
        val particleX = headCenter.x + cos(particleT) * particleRadius
        val particleY = headCenter.y + sin(particleT) * particleRadius * 0.7f
        val particleAlpha = 0.3f + 0.5f * ((sin(particleT * 3f) + 1f) / 2f)
        
        drawCircle(
            color = Color(0xFFFFD700).copy(alpha = particleAlpha * auraPulse),
            radius = 1.8f.dp.toPx(),
            center = Offset(particleX, particleY)
        )
    }

    drawRoundRect(color = Color(0xFFB71C1C), topLeft = Offset(headCenter.x - hrX * 0.98f, bandY), size = Size(hrX * 1.96f, bandH), cornerRadius = androidx.compose.ui.geometry.CornerRadius(bandH / 2f))
    drawCircle(Color(0xFFFFD700), 3.5f.dp.toPx(), Offset(headCenter.x, bandY + bandH / 2f))

    val knot = Offset(headCenter.x + hrX * 0.95f, bandY + bandH / 2f)
    drawCircle(Color(0xFF8B1414), 4f.dp.toPx(), knot)

    for (tail in 0 until 2) {
        val path = Path().apply {
            moveTo(knot.x, knot.y)
            val segs = 6
            val len = w * 0.055f
            var px = knot.x
            var py = knot.y
            for (s in 1..segs) {
                val wave = sin(t * 2f + s * 0.9f + tail * 1.7f) * (2.2f.dp.toPx() * s / segs)
                val nx = knot.x + s * len
                val ny = knot.y + tail * 4f.dp.toPx() + s * 1.2f.dp.toPx() + wave
                quadraticTo(px + len / 2f, py + wave, nx, ny)
                px = nx
                py = ny
            }
        }
        drawPath(path, color = Color(0xFFD32F2F).copy(alpha = if (tail == 0) 0.95f else 0.7f), style = Stroke(width = 3f.dp.toPx(), cap = StrokeCap.Round))
    }
}

private fun DrawScope.drawPartyMode(cx: Float, cy: Float, w: Float, h: Float, headCenter: Offset, phase: Float) {
    val headTop = headCenter.y - h * 0.21f
    val hatBaseY = headTop + 4f.dp.toPx()
    val hatH = h * 0.16f
    val hatW = w * 0.16f

    val hat = Path().apply {
        moveTo(headCenter.x - hatW / 2f, hatBaseY)
        lineTo(headCenter.x, hatBaseY - hatH)
        lineTo(headCenter.x + hatW / 2f, hatBaseY)
        close()
    }

    drawPath(hat, brush = Brush.linearGradient(listOf(Color(0xFF9C27B0), Color(0xFFF5A623)), start = Offset(headCenter.x, hatBaseY - hatH), end = Offset(headCenter.x, hatBaseY)))

    val bob = sin(phase * 2f * Math.PI.toFloat() * 2f) * 1.5f.dp.toPx()
    drawCircle(Color(0xFFFFD700), 3.5f.dp.toPx(), Offset(headCenter.x, hatBaseY - hatH + bob))

    val colors = listOf(Color(0xFFFFD700), Color(0xFFD9534F), Color(0xFF20B2AA), Color(0xFF9C27B0), Color(0xFF00BCD4), Color(0xFFF5A623))

    for (i in 0 until 12) {
        val speed = 0.7f + (i % 5) * 0.08f
        val fall = (phase * speed + i * 0.083f) % 1f
        val y = headTop - h * 0.25f + (cy + h * 0.30f - (headTop - h * 0.25f)) * fall
        val sway = sin(phase * 2f * Math.PI.toFloat() + i * 1.3f) * 4f.dp.toPx()
        val x = cx + (((i * 37) % 100) / 100f - 0.5f) * w * 0.85f + sway
        val alpha = if (fall > 0.8f) (1f - fall) / 0.2f else 1f
        val col = colors[i % colors.size].copy(alpha = alpha)

        if (i % 3 == 0) drawCircle(col, 2.2f.dp.toPx(), Offset(x, y))
        else drawRoundRect(col, topLeft = Offset(x - 2f.dp.toPx(), y - 1.2f.dp.toPx()), size = Size(4f.dp.toPx(), 2.4f.dp.toPx()), cornerRadius = androidx.compose.ui.geometry.CornerRadius(1f.dp.toPx()))
    }
}

@Composable
fun AnimatedMascotPreview(skinId: String, size: Dp = 56.dp) {
    val transition = rememberInfiniteTransition(label = "skin_preview")
    val phase by transition.animateFloat(0f, 1f, infiniteRepeatable(tween(2200, easing = LinearEasing), RepeatMode.Restart), label = "phase")

    Canvas(Modifier.size(size)) {
        val w = this.size.width
        val h = this.size.height
        val headCenter = Offset(w / 2f, h * 0.58f)

        drawOval(color = Color(0xFFFFF7F6), topLeft = Offset(headCenter.x - w * 0.26f, headCenter.y - h * 0.24f), size = Size(w * 0.52f, h * 0.48f))
        drawCircle(Color(0xFF2D2D2D), 1.8f.dp.toPx(), Offset(headCenter.x - 6.dp.toPx(), headCenter.y - 2.dp.toPx()))
        drawCircle(Color(0xFF2D2D2D), 1.8f.dp.toPx(), Offset(headCenter.x + 6.dp.toPx(), headCenter.y - 2.dp.toPx()))

        drawExtraSkins(skinId, w / 2f, h / 2f, w, h, headCenter, phase)
    }
}

@Composable
fun AnimatedThemePreview(themeId: String, size: Dp = 56.dp) {
    val transition = rememberInfiniteTransition(label = "theme_preview")
    val shift by transition.animateFloat(0f, 1f, infiniteRepeatable(tween(3000, easing = LinearEasing), RepeatMode.Reverse), label = "shift")

    Canvas(Modifier.size(size)) {
        val colors = when (themeId) {
            AnimatedSkins.AURORA -> listOf(lerp(Color(0xFF0B1026), Color(0xFF123B4A), shift), lerp(Color(0xFF1E6E5A), Color(0xFF3BA98B), shift), Color(0xFF0B1026))
            "item_math_matrix" -> listOf(Color(0xFF003000), Color(0xFF001500))
            "item_spanish_fiesta" -> listOf(Color(0xFFF5A623), Color(0xFFD9534F), Color(0xFF9C27B0))
            else -> listOf(Color(0xFFD9534F), Color(0xFFC94440))
        }

        drawRect(brush = Brush.verticalGradient(colors), size = this.size)
    }
}
