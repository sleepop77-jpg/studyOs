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
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.rememberLottieComposition
import kotlin.math.cos
import kotlin.math.sin

object AnimatedSkins {
    const val HALO = "item_halo_scholar"
    const val NINJA = "item_ninja_headband"
    const val PARTY = "item_party_mode"
    const val AURORA = "item_aurora_dream"
    const val NEON_RING = "item_free_neon_ring"
    const val ORBIT = "item_free_orbit"

    const val COSMIC = "item_cosmic_scholar"
val MASCOT_SET = setOf(HALO, NINJA, PARTY, NEON_RING, ORBIT, COSMIC)
}

fun DrawScope.drawExtraSkins(skin: String?, cx: Float, cy: Float, w: Float, h: Float, headCenter: Offset, phase: Float, state: MascotState = MascotState.IDLE) {
when (skin) {
AnimatedSkins.HALO -> drawScholarHalo(headCenter, w, h, phase)
AnimatedSkins.NINJA -> drawNinjaHeadband(headCenter, w, h, phase)
AnimatedSkins.PARTY -> drawPartyMode(cx, cy, w, h, headCenter, phase)
AnimatedSkins.NEON_RING -> drawNeonRing(headCenter, w, h, phase)
AnimatedSkins.ORBIT -> drawOrbit(headCenter, w, h, phase)
AnimatedSkins.COSMIC -> drawCosmicScholar(cx, cy, w, h, headCenter, phase, state)
}
}

private fun DrawScope.drawNeonRing(headCenter: Offset, w: Float, h: Float, phase: Float) {
    val t = phase * 2f * Math.PI.toFloat()
    val pulse = 0.5f + 0.5f * sin(t * 2f)
    val radius = w * 0.30f + pulse * 4f.dp.toPx()

    drawCircle(
        color = Color(0xFF00E5FF).copy(alpha = 0.25f + 0.25f * pulse),
        radius = radius + 5f.dp.toPx(),
        center = headCenter,
        style = Stroke(width = 1.5f.dp.toPx(), cap = StrokeCap.Round)
    )
    drawCircle(
        color = Color(0xFF00E5FF).copy(alpha = 0.55f + 0.35f * pulse),
        radius = radius,
        center = headCenter,
        style = Stroke(width = 2.5f.dp.toPx(), cap = StrokeCap.Round)
    )
}

private fun DrawScope.drawOrbit(headCenter: Offset, w: Float, h: Float, phase: Float) {
val t = phase * 2f * Math.PI.toFloat()
for (i in 0 until 3) {
val ang = t * 1.5f + i * (2f * Math.PI.toFloat() / 3f)
val rx = w * 0.32f
val ry = w * 0.12f
val x = headCenter.x + cos(ang) * rx
val y = headCenter.y + sin(ang) * ry
drawCircle(Color(0xFFFFD700).copy(alpha = 0.9f), 3f.dp.toPx(), Offset(x, y))
drawCircle(Color(0xFFFFD700).copy(alpha = 0.3f), 5f.dp.toPx(), Offset(x, y))
}
}

private fun DrawScope.drawCosmicScholar(
cx: Float,
cy: Float,
w: Float,
h: Float,
headCenter: Offset,
phase: Float,
state: MascotState
) {
val t = phase * 2f * Math.PI.toFloat()
val isStudying = state == MascotState.STUDYING || state == MascotState.WORKING_HARD || state == MascotState.BURNING
val intensity = if (isStudying) 1.4f else 1.0f
val pulse = 0.85f + 0.15f * sin(t * 2f)

for (i in 0 until 3) {
val nebulaPhase = t * (0.3f + i * 0.2f)
val nebulaRx = w * (0.45f + i * 0.05f) * pulse
val nebulaRy = nebulaRx * 0.6f
val nebulaColor = when (i) {
0 -> Color(0xFF6A1B9A).copy(alpha = 0.15f * intensity)
1 -> Color(0xFF1565C0).copy(alpha = 0.12f * intensity)
else -> Color(0xFF00838F).copy(alpha = 0.10f * intensity)
}
drawOval(
brush = Brush.radialGradient(
colors = listOf(nebulaColor, Color.Transparent),
center = Offset(headCenter.x + cos(nebulaPhase) * 8f.dp.toPx(), headCenter.y + sin(nebulaPhase) * 6f.dp.toPx()),
radius = nebulaRx
),
topLeft = Offset(headCenter.x - nebulaRx, headCenter.y - nebulaRy),
size = Size(nebulaRx * 2, nebulaRy * 2)
)
}

for (ring in 0 until 3) {
val ringRadius = w * (0.38f + ring * 0.04f)
val ringSpeed = 1.0f + ring * 0.3f
val ringAlpha = (0.4f - ring * 0.1f) * intensity

drawOval(
color = Color(0xFFFFD700).copy(alpha = ringAlpha),
topLeft = Offset(headCenter.x - ringRadius, headCenter.y - ringRadius * 0.3f),
size = Size(ringRadius * 2, ringRadius * 0.6f),
style = Stroke(width = 1.5f.dp.toPx(), cap = StrokeCap.Round)
)

for (body in 0 until 4) {
val bodyAngle = t * ringSpeed + body * (Math.PI.toFloat() / 2f) + ring * 0.5f
val bx = headCenter.x + cos(bodyAngle) * ringRadius
val by = headCenter.y + sin(bodyAngle) * ringRadius * 0.3f

when {
body == 0 -> {
drawCircle(color = Color.White, radius = 2.5f.dp.toPx(), center = Offset(bx, by))
drawCircle(color = Color.White.copy(alpha = 0.4f), radius = 4f.dp.toPx(), center = Offset(bx, by))
}
body == 1 -> {
drawCircle(color = Color(0xFF4FC3F7), radius = 3f.dp.toPx(), center = Offset(bx, by))
drawCircle(color = Color(0xFF0277BD).copy(alpha = 0.6f), radius = 3.5f.dp.toPx(), center = Offset(bx, by), style = Stroke(width = 0.8f.dp.toPx()))
}
body == 2 -> {
drawCircle(color = Color(0xFF795548), radius = 2f.dp.toPx(), center = Offset(bx, by))
}
else -> {
drawCircle(color = Color(0xFFFFD700), radius = 2.2f.dp.toPx(), center = Offset(bx, by))
for (tail in 1..3) {
drawCircle(
color = Color(0xFFFFD700).copy(alpha = (0.5f - tail * 0.15f)),
radius = (2.2f - tail * 0.4f).dp.toPx(),
center = Offset(bx - tail * 2f.dp.toPx() * cos(bodyAngle), by - tail * 2f.dp.toPx() * sin(bodyAngle))
)
}
}
}
}
}

for (i in 0 until 12) {
val starPhase = (t * 0.5f + i * 0.8f) % (2f * Math.PI.toFloat())
val starX = headCenter.x + cos(starPhase + i) * w * (0.25f + (i % 3) * 0.08f)
val starY = headCenter.y + sin(starPhase * 0.7f + i * 1.3f) * h * (0.20f + (i % 2) * 0.06f)
val starAlpha = 0.3f + 0.5f * ((sin(starPhase * 3f) + 1f) / 2f)
val starSize = (1.2f + (i % 3) * 0.6f).dp.toPx()

drawCircle(
color = Color.White.copy(alpha = starAlpha * intensity),
radius = starSize,
center = Offset(starX, starY)
)
}

if (isStudying) {
for (i in 0 until 3) {
val shootPhase = (t * 2f + i * 2.1f) % (2f * Math.PI.toFloat())
val shootProgress = (shootPhase / (2f * Math.PI.toFloat()))
if (shootProgress < 0.3f) {
val startX = headCenter.x - w * 0.4f + i * w * 0.15f
val startY = headCenter.y - h * 0.3f + i * h * 0.1f
val endX = headCenter.x + w * 0.4f - i * w * 0.15f
val endY = headCenter.y + h * 0.3f - i * h * 0.1f
val currentX = startX + (endX - startX) * (shootProgress / 0.3f)
val currentY = startY + (endY - startY) * (shootProgress / 0.3f)
val shootAlpha = if (shootProgress < 0.15f) shootProgress / 0.15f else (0.3f - shootProgress) / 0.15f

drawCircle(color = Color.White, radius = 2f.dp.toPx(), center = Offset(currentX, currentY))
for (tail in 1..4) {
drawCircle(
color = Color.White.copy(alpha = (0.6f - tail * 0.15f) * shootAlpha),
radius = (2f - tail * 0.4f).dp.toPx(),
center = Offset(currentX - tail * 3f.dp.toPx(), currentY - tail * 3f.dp.toPx())
)
}
}
}
}

if (isStudying) {
val burstPhase = (t * 3f) % (2f * Math.PI.toFloat())
val burstAlpha = sin(burstPhase) * 0.3f * intensity
if (burstAlpha > 0f) {
drawCircle(
brush = Brush.radialGradient(
colors = listOf(
Color(0xFFFFD700).copy(alpha = burstAlpha),
Color(0xFFFF6F00).copy(alpha = burstAlpha * 0.5f),
Color.Transparent
),
center = headCenter,
radius = w * 0.5f
),
center = headCenter,
radius = w * 0.5f
)
}
}

val coreGlow = 0.6f + 0.4f * pulse * intensity
drawCircle(
brush = Brush.radialGradient(
colors = listOf(
Color(0xFFFFD700).copy(alpha = 0.8f * coreGlow),
Color(0xFFFF6F00).copy(alpha = 0.4f * coreGlow),
Color.Transparent
),
center = Offset(headCenter.x, headCenter.y - h * 0.32f),
radius = w * 0.12f
),
center = Offset(headCenter.x, headCenter.y - h * 0.32f),
radius = w * 0.12f
)
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
            "theme_crimson_focus" -> listOf(lerp(Color(0xFF1D0505), Color(0xFFC41C3B), shift), Color(0xFF8B0000))
            else -> listOf(Color(0xFF000000), Color(0xFF0A0A0A))
        }
            drawRect(brush = Brush.verticalGradient(colors), size = this.size)
}
}

@Composable
fun BullPreview(size: Dp = 56.dp) {
    val composition by rememberLottieComposition(LottieCompositionSpec.Asset("bull_market.json"))
    LottieAnimation(
        composition = composition,
        iterations = LottieConstants.IterateForever,
        modifier = Modifier.size(size)
    )
}