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

    const val CYBERPUNK = "item_cyberpunk"
const val NIGHT_OWL = "item_night_owl_skin"
const val GOLDEN_DESK = "item_golden_desk"
const val ASTRAL = "item_astral_crown"
const val DRAGON = "item_dragon_aura"
const val KATANA = "item_neon_katana"
const val COSMIC = "item_cosmic_scholar"
const val QUIET_ORB = "item_quiet_orb"
const val QUIET_ORB = "item_quiet_orb"

val MASCOT_SET = setOf(
HALO, NINJA, PARTY,
CYBERPUNK, NIGHT_OWL, GOLDEN_DESK,
ASTRAL, DRAGON, KATANA,
NEON_RING, ORBIT, COSMIC, QUIET_ORB
)
}

fun DrawScope.drawExtraSkins(skin: String?, cx: Float, cy: Float, w: Float, h: Float, headCenter: Offset, phase: Float, state: MascotState = MascotState.IDLE) {
when (skin) {
AnimatedSkins.HALO -> drawScholarHalo(headCenter, w, h, phase)
AnimatedSkins.NINJA -> drawNinjaHeadband(headCenter, w, h, phase)
AnimatedSkins.PARTY -> drawPartyMode(cx, cy, w, h, headCenter, phase)
AnimatedSkins.NEON_RING -> drawNeonRing(headCenter, w, h, phase)
AnimatedSkins.ORBIT -> drawOrbit(headCenter, w, h, phase)
AnimatedSkins.COSMIC -> drawCosmicScholar(cx, cy, w, h, headCenter, phase, state)
AnimatedSkins.CYBERPUNK -> drawCyberpunkVisor(headCenter, w, h)
AnimatedSkins.NIGHT_OWL -> drawNightOwlCap(headCenter, w, h)
AnimatedSkins.GOLDEN_DESK -> drawGoldenDesk(cx, cy, w, h)
AnimatedSkins.ASTRAL -> drawAstralCrown(headCenter, w, h, phase)
AnimatedSkins.DRAGON -> drawDragonAura(headCenter, w, h, phase)
AnimatedSkins.KATANA -> drawNeonKatana(cx, cy, w, h)
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

private fun DrawScope.drawCyberpunkVisor(head: Offset, w: Float, h: Float) {
val visorWidth = w * 0.42f
val visorHeight = h * 0.10f
val left = head.x - visorWidth / 2f
val top = head.y - h * 0.065f
val rectSize = Size(visorWidth, visorHeight)

drawRoundRect(
color = Color(0xFF073642),
topLeft = Offset(left, top),
size = rectSize,
cornerRadius = androidx.compose.ui.geometry.CornerRadius(4.dp.toPx())
)
drawRoundRect(
color = Color(0xFF00E5FF),
topLeft = Offset(left, top),
size = rectSize,
cornerRadius = androidx.compose.ui.geometry.CornerRadius(4.dp.toPx()),
style = Stroke(width = 1.5.dp.toPx())
)
drawLine(Color(0xFFB2EBF2), Offset(left + visorWidth * 0.2f, top + visorHeight * 0.3f), Offset(left + visorWidth * 0.43f, top + visorHeight * 0.3f), 1.dp.toPx())
}

private fun DrawScope.drawNightOwlCap(head: Offset, w: Float, h: Float) {
val cap = Path().apply {
moveTo(head.x - w * 0.19f, head.y - h * 0.13f)
quadraticTo(head.x - w * 0.12f, head.y - h * 0.34f, head.x + w * 0.10f, head.y - h * 0.29f)
quadraticTo(head.x + w * 0.22f, head.y - h * 0.25f, head.x + w * 0.21f, head.y - h * 0.12f)
lineTo(head.x - w * 0.19f, head.y - h * 0.13f)
close()
}
drawPath(cap, Color(0xFF303F78))
drawLine(Color(0xFF7986CB), Offset(head.x - w * 0.18f, head.y - h * 0.12f), Offset(head.x + w * 0.21f, head.y - h * 0.12f), 2.dp.toPx())
drawCircle(Color(0xFFFFE082), h * 0.035f, Offset(head.x + w * 0.12f, head.y - h * 0.27f))
}

private fun DrawScope.drawGoldenDesk(cx: Float, cy: Float, w: Float, h: Float) {
val y = cy + h * 0.20f
drawRoundRect(
color = Color(0xFFFFD700),
topLeft = Offset(cx - w * 0.38f, y),
size = Size(w * 0.76f, 3.dp.toPx()),
cornerRadius = androidx.compose.ui.geometry.CornerRadius(2.dp.toPx())
)
}

private fun DrawScope.drawAstralCrown(head: Offset, w: Float, h: Float, phase: Float) {
val crownWidth = w * 0.40f
val crownHeight = h * 0.15f
val left = head.x - crownWidth / 2f
val right = head.x + crownWidth / 2f
val baseY = head.y - h * 0.22f
val topY = baseY - crownHeight

val crown = Path().apply {
moveTo(left, baseY)
lineTo(left, baseY - crownHeight * 0.55f)
lineTo(left + crownWidth * 0.23f, baseY - crownHeight * 0.24f)
lineTo(head.x, topY)
lineTo(right - crownWidth * 0.23f, baseY - crownHeight * 0.24f)
lineTo(right, baseY - crownHeight * 0.55f)
lineTo(right, baseY)
close()
}

drawPath(crown, brush = Brush.linearGradient(listOf(Color(0xFF8055C7), Color(0xFFFFD700)), start = Offset(head.x, topY), end = Offset(head.x, baseY)))
drawPath(crown, color = Color(0xFFFFE082), style = Stroke(width = 1.2.dp.toPx()))

for (i in 0 until 3) {
val angle = phase * 2f * Math.PI.toFloat() + i * 2.1f
val x = head.x + cos(angle) * w * 0.27f
val y = topY - h * 0.035f + sin(angle) * h * 0.035f
drawCircle(Color(0xFFFFF9C4), 1.8.dp.toPx(), Offset(x, y))
}
}

private fun DrawScope.drawDragonAura(head: Offset, w: Float, h: Float, phase: Float) {
val pulse = 0.65f + 0.25f * sin(phase * 2f * Math.PI.toFloat())

for (side in listOf(-1f, 1f)) {
val flame = Path().apply {
moveTo(head.x + side * w * 0.31f, head.y + h * 0.18f)
quadraticTo(head.x + side * w * 0.48f, head.y - h * 0.02f, head.x + side * w * 0.28f, head.y - h * 0.24f)
quadraticTo(head.x + side * w * 0.43f, head.y - h * 0.13f, head.x + side * w * 0.36f, head.y + h * 0.12f)
}
drawPath(flame, Color(0xFFFF5722).copy(alpha = pulse), style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round))
drawPath(flame, Color(0xFFFFC107).copy(alpha = pulse * 0.75f), style = Stroke(width = 1.2.dp.toPx(), cap = StrokeCap.Round))
}

for (i in 0 until 8) {
val angle = phase * 2f * Math.PI.toFloat() + i * (Math.PI.toFloat() / 4f)
val radiusX = w * 0.36f
val radiusY = h * 0.27f
val x = head.x + cos(angle) * radiusX
val y = head.y + sin(angle) * radiusY
drawCircle(Color(0xFFFF6D00).copy(alpha = 0.45f + pulse * 0.4f), 2.dp.toPx(), Offset(x, y))
}
}

private fun DrawScope.drawNeonKatana(cx: Float, cy: Float, w: Float, h: Float) {
val handle = Offset(cx + w * 0.30f, cy + h * 0.28f)
val blade = Offset(cx + w * 0.41f, cy - h * 0.17f)

drawLine(Color(0xFF00E5FF).copy(alpha = 0.25f), handle, blade, 5.dp.toPx(), cap = StrokeCap.Round)
drawLine(Color(0xFFB2EBF2), handle, blade, 1.8.dp.toPx(), cap = StrokeCap.Round)
drawLine(Color(0xFF263238), Offset(handle.x - 3.dp.toPx(), handle.y + 2.dp.toPx()), Offset(handle.x + 3.dp.toPx(), handle.y - 2.dp.toPx()), 5.dp.toPx(), cap = StrokeCap.Round)
drawLine(Color(0xFFFFD54F), Offset(handle.x - 4.dp.toPx(), handle.y - 1.dp.toPx()), Offset(handle.x + 4.dp.toPx(), handle.y + 1.dp.toPx()), 1.2.dp.toPx())
}

@Composable
fun AnimatedMascotPreview(skinId: String, size: Dp = 56.dp) {
    val transition = rememberInfiniteTransition(label = "skin_preview")
    val phase by transition.animateFloat(0f, 1f, infiniteRepeatable(tween(2200, easing = LinearEasing), RepeatMode.Restart), label = "phase")

    Canvas(Modifier.size(size)) {
        val w = this.size.width
        val h = this.size.height
        val headCenter = Offset(w / 2f, h * 0.58f)

if (skinId == AnimatedSkins.QUIET_ORB) {
val radius = w * 0.30f
drawCircle(
brush = Brush.radialGradient(
listOf(Color.White, Color(0xFFEAEAEA)),
center = Offset(headCenter.x - radius * 0.3f, headCenter.y - radius * 0.4f),
radius = radius * 1.8f
),
radius = radius,
center = headCenter
)
drawCircle(
Color(0xFF202020),
2.1f.dp.toPx(),
Offset(headCenter.x - 6.dp.toPx(), headCenter.y - 2.dp.toPx())
)
drawCircle(
Color(0xFF202020),
2.1f.dp.toPx(),
Offset(headCenter.x + 6.dp.toPx(), headCenter.y - 2.dp.toPx())
)
} else {
drawOval(color = Color(0xFFFFF7F6), topLeft = Offset(headCenter.x - w * 0.26f, headCenter.y - h * 0.24f), size = Size(w * 0.52f, h * 0.48f))
drawCircle(Color(0xFF2D2D2D), 1.8f.dp.toPx(), Offset(headCenter.x - 6.dp.toPx(), headCenter.y - 2.dp.toPx()))
drawCircle(Color(0xFF2D2D2D), 1.8f.dp.toPx(), Offset(headCenter.x + 6.dp.toPx(), headCenter.y - 2.dp.toPx()))
drawExtraSkins(skinId, w / 2f, h / 2f, w, h, headCenter, phase)
}
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
            "theme_void_nexus" -> listOf(lerp(Color(0xFF120725), Color(0xFF35145C), shift), Color(0xFF05030B))
"theme_sakura_drift" -> listOf(lerp(Color(0xFF18213B), Color(0xFF69334F), shift), Color(0xFF090A18))
"theme_crimson_focus" -> listOf(lerp(Color(0xFF1D0505), Color(0xFFC41C3B), shift), Color(0xFF8B0000))
            else -> listOf(Color(0xFF000000), Color(0xFF0A0A0A))
        }
            drawRect(brush = Brush.verticalGradient(colors), size = this.size)
}
}

@Composable
fun BullPreview(size: Dp = 56.dp) {
    val composition by rememberLottieComposition(LottieCompositionSpec.JsonString(BullLottie.JSON))
    if (composition != null) {
        LottieAnimation(
            composition = composition,
            iterations = LottieConstants.IterateForever,
            modifier = Modifier.size(size)
        )
    } else {
        CanvasBull(modifier = Modifier.size(size))
    }
}