package com.example.studyos.ui.launcher

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
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

    const val CROWN = "item_astral_crown"
    const val DRAGON = "item_dragon_aura"
    const val KATANA = "item_neon_katana"
    const val VOID = "theme_void_nexus"
    const val SAKURA = "theme_sakura_drift"

    val MASCOT_SET = setOf(
        HALO,
        NINJA,
        PARTY,
        CROWN,
        DRAGON,
        KATANA,
        "item_cyberpunk",
        "item_night_owl_skin",
        "item_golden_desk"
    )
}

fun DrawScope.drawExtraSkins(
    skin: String?,
    cx: Float,
    cy: Float,
    w: Float,
    h: Float,
    headCenter: Offset,
    phase: Float
) {
    when (skin) {
        AnimatedSkins.HALO -> drawScholarHalo(headCenter, w, h, phase)
        AnimatedSkins.NINJA -> drawNinjaHeadband(headCenter, w, h, phase)
        AnimatedSkins.PARTY -> drawPartyMode(cx, cy, w, h, headCenter, phase)
        AnimatedSkins.CROWN -> drawAstralCrown(headCenter, w, h, phase)
        AnimatedSkins.DRAGON -> drawDragonAura(headCenter, w, h, phase)
        AnimatedSkins.KATANA -> drawNeonKatana(cx, cy, w, h, phase)
    }
}

private fun DrawScope.drawScholarHalo(headCenter: Offset, w: Float, h: Float, phase: Float) {
    val t = phase * 2f * Math.PI.toFloat()
    val y = headCenter.y - h * 0.27f + sin(t) * 2.dp.toPx()

    drawOval(
        color = Color(0xFFFFD700).copy(alpha = 0.30f),
        topLeft = Offset(headCenter.x - 20.dp.toPx(), y - 5.dp.toPx()),
        size = Size(40.dp.toPx(), 10.dp.toPx()),
        style = Stroke(5.dp.toPx(), cap = StrokeCap.Round)
    )

    drawOval(
        color = Color(0xFFFFF3A0),
        topLeft = Offset(headCenter.x - 18.dp.toPx(), y - 4.dp.toPx()),
        size = Size(36.dp.toPx(), 8.dp.toPx()),
        style = Stroke(2.dp.toPx(), cap = StrokeCap.Round)
    )
}

private fun DrawScope.drawNinjaHeadband(headCenter: Offset, w: Float, h: Float, phase: Float) {
    val t = phase * 2f * Math.PI.toFloat()
    val y = headCenter.y - 9.dp.toPx()

    drawRoundRect(
        color = Color(0xFFC41C3B),
        topLeft = Offset(headCenter.x - 24.dp.toPx(), y),
        size = Size(48.dp.toPx(), 7.dp.toPx()),
        cornerRadius = CornerRadius(3.5.dp.toPx())
    )

    val tailX = headCenter.x + 21.dp.toPx()
    val tailY = y + 2.dp.toPx()
    val wave = sin(t) * 4.dp.toPx()

    val tail = Path().apply {
        moveTo(tailX, tailY)
        quadraticTo(tailX + 14.dp.toPx(), tailY - 7.dp.toPx() + wave, tailX + 24.dp.toPx(), tailY)
        quadraticTo(tailX + 12.dp.toPx(), tailY + 7.dp.toPx() - wave, tailX, tailY + 4.dp.toPx())
        close()
    }

    drawPath(tail, Color(0xFFE53935))
}

private fun DrawScope.drawPartyMode(
    cx: Float,
    cy: Float,
    w: Float,
    h: Float,
    headCenter: Offset,
    phase: Float
) {
    val twoPi = 2f * Math.PI.toFloat()

    for (i in 0 until 10) {
        val t = (phase + i * 0.11f) % 1f
        val angle = t * twoPi + i
        val radius = 28.dp.toPx() + (i % 3) * 5.dp.toPx()

        val x = headCenter.x + cos(angle) * radius
        val y = headCenter.y - 8.dp.toPx() + sin(angle) * radius * 0.75f
        val color = when (i % 4) {
            0 -> Color(0xFFFFD700)
            1 -> Color(0xFFE53935)
            2 -> Color(0xFF00E5FF)
            else -> Color(0xFF4CAF50)
        }

        drawCircle(
            color = color.copy(alpha = 0.75f),
            radius = 1.8.dp.toPx(),
            center = Offset(x, y)
        )
    }
}

private fun DrawScope.drawAstralCrown(headCenter: Offset, w: Float, h: Float, phase: Float) {
    val t = phase * 2f * Math.PI.toFloat()
    val bob = sin(t) * 2.dp.toPx()
    val cx = headCenter.x
    val cy = headCenter.y - h * 0.28f + bob

    val crown = Path().apply {
        moveTo(cx - 17.dp.toPx(), cy)
        lineTo(cx - 12.dp.toPx(), cy - 14.dp.toPx())
        lineTo(cx - 6.dp.toPx(), cy - 6.dp.toPx())
        lineTo(cx, cy - 19.dp.toPx())
        lineTo(cx + 6.dp.toPx(), cy - 6.dp.toPx())
        lineTo(cx + 12.dp.toPx(), cy - 14.dp.toPx())
        lineTo(cx + 17.dp.toPx(), cy)
        close()
    }

    drawPath(
        path = crown,
        brush = Brush.verticalGradient(
            listOf(
                Color(0xFF00E5FF).copy(alpha = 0.50f),
                Color(0xFF3D5AFE).copy(alpha = 0.28f)
            )
        )
    )

    drawPath(
        path = crown,
        color = Color(0xFF00E5FF),
        style = Stroke(2.dp.toPx(), cap = StrokeCap.Round)
    )

    for (i in 0 until 5) {
        val angle = t * 1.4f + i * (2f * Math.PI.toFloat() / 5f)
        val rx = 24.dp.toPx()
        val ry = 9.dp.toPx()

        val sx = cx + cos(angle) * rx
        val sy = cy - 8.dp.toPx() + sin(angle) * ry

        drawCircle(Color.White.copy(alpha = 0.45f), 4.dp.toPx(), Offset(sx, sy))
        drawCircle(Color(0xFFFFD700), 1.7.dp.toPx(), Offset(sx, sy))
    }
}

private fun DrawScope.drawDragonAura(headCenter: Offset, w: Float, h: Float, phase: Float) {
    val t = phase * 2f * Math.PI.toFloat()
    val cx = headCenter.x
    val cy = headCenter.y

    for (i in 0 until 6) {
        val p = t + i * 0.9f
        val side = if (i % 2 == 0) -1f else 1f
        val x = cx + side * (18.dp.toPx() + (i % 3) * 5.dp.toPx())
        val y = cy + 13.dp.toPx() + sin(p) * 5.dp.toPx()

        val flame = Path().apply {
            moveTo(x - 8.dp.toPx(), y + 10.dp.toPx())
            quadraticTo(x, y - 24.dp.toPx(), x + 8.dp.toPx(), y + 10.dp.toPx())
            quadraticTo(x, y + 5.dp.toPx(), x - 8.dp.toPx(), y + 10.dp.toPx())
            close()
        }

        drawPath(
            path = flame,
            brush = Brush.verticalGradient(
                listOf(
                    Color(0xFFFFD700).copy(alpha = 0.55f),
                    Color(0xFFFF5722).copy(alpha = 0.42f),
                    Color.Transparent
                )
            )
        )
    }

    for (i in 0 until 12) {
        val emberT = (phase * 1.8f + i * 0.17f) % 1f
        val x = cx + sin(t + i) * 32.dp.toPx()
        val y = cy + 23.dp.toPx() - emberT * 58.dp.toPx()
        val alpha = if (emberT > 0.75f) (1f - emberT) / 0.25f else 1f

        drawCircle(
            color = Color(0xFFFF9800).copy(alpha = alpha * 0.85f),
            radius = 1.4.dp.toPx(),
            center = Offset(x, y)
        )
    }
}

private fun DrawScope.drawNeonKatana(cx: Float, cy: Float, w: Float, h: Float, phase: Float) {
    val t = phase * 2f * Math.PI.toFloat()
    val pulse = 0.55f + 0.45f * ((sin(t) + 1f) / 2f)

    val deskY = cy + h * 0.20f
    val handleX = cx + w * 0.14f
    val handleY = deskY - 5.dp.toPx()

    drawRoundRect(
        color = Color(0xFF1E1E1E),
        topLeft = Offset(handleX, handleY),
        size = Size(19.dp.toPx(), 6.dp.toPx()),
        cornerRadius = CornerRadius(3.dp.toPx())
    )

    drawRoundRect(
        color = Color(0xFFD32F2F),
        topLeft = Offset(handleX + 17.dp.toPx(), handleY - 1.dp.toPx()),
        size = Size(4.dp.toPx(), 8.dp.toPx()),
        cornerRadius = CornerRadius(1.5.dp.toPx())
    )

    val blade = Path().apply {
        moveTo(handleX + 22.dp.toPx(), handleY + 1.dp.toPx())
        lineTo(cx + w * 0.38f, handleY - 5.dp.toPx())
        lineTo(cx + w * 0.40f, handleY - 3.dp.toPx())
        lineTo(handleX + 22.dp.toPx(), handleY + 5.dp.toPx())
        close()
    }

    drawPath(
        path = blade,
        color = Color(0xFF00E5FF).copy(alpha = 0.22f * pulse),
        style = Stroke(7.dp.toPx(), cap = StrokeCap.Round)
    )

    drawPath(
        path = blade,
        color = Color(0xFF00E5FF),
        style = Stroke(2.dp.toPx(), cap = StrokeCap.Round)
    )

    drawPath(
        path = blade,
        color = Color.White.copy(alpha = 0.85f * pulse),
        style = Stroke(1.dp.toPx(), cap = StrokeCap.Round)
    )
}

@Composable
fun AnimatedMascotPreview(skinId: String, size: Dp = 56.dp) {
    val transition = rememberInfiniteTransition(label = "mascot_preview")
    val phase by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1800, easing = LinearEasing)
        ),
        label = "preview_phase"
    )

    Canvas(modifier = Modifier.size(size)) {
        val w = this.size.width
        val h = this.size.height
        val cx = w / 2f
        val cy = h / 2f
        val head = Offset(cx, cy - h * 0.03f)

        drawCircle(
            brush = Brush.radialGradient(
                listOf(
                    Color(0xFFE53935).copy(alpha = 0.25f),
                    Color.Transparent
                ),
                center = head,
                radius = w * 0.55f
            ),
            center = head,
            radius = w * 0.55f
        )

        drawOval(
            color = Color(0xFFFFF7F6),
            topLeft = Offset(cx - w * 0.22f, cy - h * 0.24f),
            size = Size(w * 0.44f, h * 0.42f)
        )

        drawCircle(Color(0xFFB71C1C), 2.2.dp.toPx(), Offset(cx - 7.dp.toPx(), cy - 2.dp.toPx()))
        drawCircle(Color(0xFFB71C1C), 2.2.dp.toPx(), Offset(cx + 7.dp.toPx(), cy - 2.dp.toPx()))

        val mouth = Path().apply {
            moveTo(cx - 4.dp.toPx(), cy + 7.dp.toPx())
            quadraticTo(cx, cy + 10.dp.toPx(), cx + 4.dp.toPx(), cy + 7.dp.toPx())
        }

        drawPath(
            path = mouth,
            color = Color(0xFFB71C1C),
            style = Stroke(1.5.dp.toPx(), cap = StrokeCap.Round)
        )

        drawExtraSkins(
            skin = skinId,
            cx = cx,
            cy = cy,
            w = w,
            h = h,
            headCenter = head,
            phase = phase
        )
    }
}

@Composable
fun AnimatedThemePreview(themeId: String, modifier: Modifier = Modifier) {
    val transition = rememberInfiniteTransition(label = "theme_preview")
    val shift by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2200, easing = LinearEasing)
        ),
        label = "theme_shift"
    )

    Canvas(modifier = modifier) {
        val colors = when (themeId) {
            AnimatedSkins.AURORA -> listOf(
                lerp(Color(0xFF0B1026), Color(0xFF123B4A), shift),
                lerp(Color(0xFF1E6E5A), Color(0xFF3BA98B), shift),
                Color(0xFF0B1026)
            )

            "item_math_matrix" -> listOf(
                Color(0xFF000000),
                Color(0xFF003000),
                Color(0xFF001500)
            )

            "item_spanish_fiesta" -> listOf(
                Color(0xFFF5A623),
                Color(0xFFD9534F),
                Color(0xFF9C27B0)
            )

            AnimatedSkins.VOID -> listOf(
                lerp(Color(0xFF000000), Color(0xFF1A0033), shift),
                Color(0xFF4A148C),
                Color(0xFF000000)
            )

            AnimatedSkins.SAKURA -> listOf(
                Color(0xFF2D1B2E),
                Color(0xFF4A2545),
                Color(0xFFFF80AB).copy(alpha = 0.50f),
                Color(0xFF1A0F1C)
            )

            else -> listOf(
                Color(0xFF050505),
                Color(0xFF140505),
                Color(0xFF000000)
            )
        }

        drawRect(
            brush = Brush.verticalGradient(colors),
            size = this.size
        )

        if (themeId == AnimatedSkins.VOID) {
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color(0xFF7C4DFF).copy(alpha = 0.35f),
                        Color.Transparent
                    ),
                    center = Offset(size.width * (0.35f + shift * 0.3f), size.height * 0.35f),
                    radius = size.width * 0.45f
                ),
                center = Offset(size.width * (0.35f + shift * 0.3f), size.height * 0.35f),
                radius = size.width * 0.45f
            )
        }

        if (themeId == AnimatedSkins.SAKURA) {
            for (i in 0 until 12) {
                val x = ((i * 37) % 100) / 100f * size.width
                val y = (((shift + i * 0.11f) % 1f) * size.height)
                drawCircle(
                    color = Color(0xFFFFB7C5).copy(alpha = 0.55f),
                    radius = 1.5.dp.toPx(),
                    center = Offset(x, y)
                )
            }
        }
    }
}
