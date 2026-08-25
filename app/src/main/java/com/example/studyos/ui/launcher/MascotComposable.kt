package com.example.studyos.ui.launcher

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.studyos.core.Store
import com.example.studyos.ui.theme.FameGold
import com.example.studyos.ui.theme.PrimaryCoral
import com.example.studyos.ui.theme.PrimaryCoralDark
import com.example.studyos.ui.theme.PrimaryNightMaroon
import com.example.studyos.ui.theme.SuccessGreen
import com.example.studyos.ui.theme.AccentCyan
import com.example.studyos.ui.theme.WarningRed
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

enum class MascotState { IDLE, STUDYING, FRUSTRATED, BURNING }

@Composable
fun InteractiveMascot(
    state: MascotState,
    modifier: Modifier = Modifier,
    size: Dp = 140.dp,
    showArc: Boolean = true,
    progressArc: Float = 1f
) {
    val scope = rememberCoroutineScope()
    var bubble by remember { mutableStateOf<String?>(null) }
    val quotes = remember(state) {
        when (state) {
            MascotState.BURNING -> listOf("I AM ON FIRE! 3+ HOURS UNSTOPPABLE!", "SUPERCHARGED OVERDRIVE!")
            MascotState.STUDYING -> listOf("Typing at 140 WPM! Keep this momentum!", "Fame is pouring in! +2 Fame/min!", "Focus locked in! We don't stop now!")
            MascotState.FRUSTRATED -> listOf("Shame is rising! Start the timer!", "Save me! 25 minutes of focus!")
            else -> listOf("Ready to lock in for a 4.0 GPA!", "Fame economy is booming!", "No procrastination on my watch!")
        }
    }
    Box(
        modifier = modifier
            .size(size)
            .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null) {
                bubble = quotes.random()
                scope.launch {
                    delay(2500)
                    bubble = null
                }
            },
        contentAlignment = Alignment.Center
    ) {
        AnimatedVisibility(
            visible = bubble != null,
            enter = fadeIn() + scaleIn(initialScale = 0.8f),
            exit = fadeOut() + scaleOut(targetScale = 0.8f),
            modifier = Modifier.align(Alignment.TopCenter).offset(y = (-40).dp)
        ) {
            Surface(shape = RoundedCornerShape(14.dp), color = Color.White, shadowElevation = 6.dp) {
                Text(
                    bubble ?: "",
                    color = PrimaryNightMaroon,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                )
            }
        }
        MascotCanvas(state = state, size = size, showArc = showArc, progressArc = progressArc)
    }
}

@Composable
fun MascotCanvas(state: MascotState, size: Dp, showArc: Boolean, progressArc: Float) {
    val equippedSkin by Store.equippedMascot.collectAsState(initial = null)
    val t = rememberInfiniteTransition(label = "mascot")
    val breath by t.animateFloat(0.97f, 1.04f, infiniteRepeatable(tween(1400, easing = FastOutSlowInEasing), RepeatMode.Reverse), label = "breath")
    val typeL by t.animateFloat(-7f, 7f, infiniteRepeatable(tween(120, easing = LinearEasing), RepeatMode.Reverse), label = "tl")
    val typeR by t.animateFloat(7f, -7f, infiniteRepeatable(tween(120, easing = LinearEasing), RepeatMode.Reverse), label = "tr")
    val skinPhase by t.animateFloat(0f, 1f, infiniteRepeatable(tween(2200, easing = LinearEasing), RepeatMode.Restart), label = "skin")
    val glow by t.animateFloat(0.4f, 0.95f, infiniteRepeatable(tween(400, easing = LinearEasing), RepeatMode.Reverse), label = "glow")
    val isStudying = state == MascotState.STUDYING || state == MascotState.BURNING
    Box(modifier = Modifier.size(size)) {
        Canvas(Modifier.size(size)) {
            val w = this.size.width
            val h = this.size.height
            val cx = w / 2f
            val cy = h / 2f
            if (showArc) {
                val r = w * 0.45f
                val rect = Rect(cx - r, cy - r - h * 0.05f, cx + r, cy + r - h * 0.05f)
                drawArc(Color.White.copy(alpha = 0.25f), 160f, 220f, false, topLeft = rect.topLeft, size = rect.size, style = Stroke(4.dp.toPx(), cap = StrokeCap.Round))
                if (progressArc > 0f) {
                    drawArc(if (state == MascotState.BURNING) Color(0xFFFF5722) else Color.White, 160f, 220f * progressArc, false, topLeft = rect.topLeft, size = rect.size, style = Stroke(4.5.dp.toPx(), cap = StrokeCap.Round))
                }
            }
            if (state == MascotState.BURNING) {
                drawCircle(brush = Brush.radialGradient(listOf(Color(0xFFFF5722).copy(alpha = 0.5f * glow), Color.Transparent), center = Offset(cx, cy - h * 0.04f), radius = w * 0.5f), center = Offset(cx, cy - h * 0.04f), radius = w * 0.5f)
            }
            val scale = if (isStudying) breath else 1f
            val hrX = w * 0.23f * scale
            val hrY = h * 0.21f * scale
            val head = Offset(cx, cy - h * 0.04f)
            drawOval(Color.Black.copy(alpha = 0.18f), topLeft = Offset(head.x - hrX, head.y - hrY + 3.dp.toPx()), size = Size(hrX * 2, hrY * 2))
            val headColor = if (state == MascotState.BURNING) Color(0xFFFF5252) else Color(0xFFFFF7F6)
            drawOval(headColor, topLeft = Offset(head.x - hrX, head.y - hrY), size = Size(hrX * 2, hrY * 2))
            drawOval(if (state == MascotState.BURNING) Color(0xFFB71C1C) else PrimaryCoralDark.copy(alpha = 0.35f), topLeft = Offset(head.x - hrX, head.y - hrY), size = Size(hrX * 2, hrY * 2), style = Stroke(1.8.dp.toPx()))
            val blush = if (state == MascotState.BURNING) FameGold else PrimaryCoral
            drawCircle(blush.copy(alpha = 0.4f), 4.5.dp.toPx(), Offset(head.x - 14.dp.toPx(), head.y + 7.dp.toPx()))
            drawCircle(blush.copy(alpha = 0.4f), 4.5.dp.toPx(), Offset(head.x + 14.dp.toPx(), head.y + 7.dp.toPx()))
            val leaf = Path().apply {
                moveTo(head.x, head.y - hrY)
                quadraticTo(head.x + 8.dp.toPx(), head.y - hrY - 11.dp.toPx(), head.x + 15.dp.toPx(), head.y - hrY - 9.dp.toPx())
                quadraticTo(head.x + 5.dp.toPx(), head.y - hrY - 2.dp.toPx(), head.x, head.y - hrY)
                close()
            }
            drawPath(leaf, if (state == MascotState.BURNING) Color(0xFFFF9800) else SuccessGreen)
            drawFace(state, head)
            drawLaptop(cx, cy, w, h, isStudying, state == MascotState.BURNING, typeL, typeR, head)
            drawEquippedSkin(equippedSkin, cx, cy, w, h, head)
            drawExtraSkins(equippedSkin, cx, cy, w, h, head, skinPhase)
        }
    }
}

private fun DrawScope.drawFace(state: MascotState, head: Offset) {
    val ink = PrimaryCoralDark
    when (state) {
        MascotState.BURNING -> {
            drawRoundRect(Color(0xFF1E1E24), Offset(head.x - 14.dp.toPx(), head.y - 4.dp.toPx()), Size(12.dp.toPx(), 8.dp.toPx()), cornerRadius = androidx.compose.ui.geometry.CornerRadius(2.dp.toPx()))
            drawRoundRect(Color(0xFF1E1E24), Offset(head.x + 2.dp.toPx(), head.y - 4.dp.toPx()), Size(12.dp.toPx(), 8.dp.toPx()), cornerRadius = androidx.compose.ui.geometry.CornerRadius(2.dp.toPx()))
            drawLine(FameGold, Offset(head.x - 2.dp.toPx(), head.y - 1.dp.toPx()), Offset(head.x + 2.dp.toPx(), head.y - 1.dp.toPx()), 2.dp.toPx())
            val grin = Path().apply {
                moveTo(head.x - 7.dp.toPx(), head.y + 8.dp.toPx())
                quadraticTo(head.x, head.y + 14.dp.toPx(), head.x + 7.dp.toPx(), head.y + 8.dp.toPx())
                close()
            }
            drawPath(grin, FameGold)
        }
        MascotState.FRUSTRATED -> {
            drawLine(ink, Offset(head.x - 13.dp.toPx(), head.y - 1.dp.toPx()), Offset(head.x - 7.dp.toPx(), head.y + 2.dp.toPx()), 2.2.dp.toPx(), cap = StrokeCap.Round)
            drawLine(ink, Offset(head.x - 13.dp.toPx(), head.y + 5.dp.toPx()), Offset(head.x - 7.dp.toPx(), head.y + 2.dp.toPx()), 2.2.dp.toPx(), cap = StrokeCap.Round)
            drawLine(ink, Offset(head.x + 13.dp.toPx(), head.y - 1.dp.toPx()), Offset(head.x + 7.dp.toPx(), head.y + 2.dp.toPx()), 2.2.dp.toPx(), cap = StrokeCap.Round)
            drawLine(ink, Offset(head.x + 13.dp.toPx(), head.y + 5.dp.toPx()), Offset(head.x + 7.dp.toPx(), head.y + 2.dp.toPx()), 2.2.dp.toPx(), cap = StrokeCap.Round)
            drawLine(ink, Offset(head.x - 6.dp.toPx(), head.y + 10.dp.toPx()), Offset(head.x + 6.dp.toPx(), head.y + 10.dp.toPx()), 2.2.dp.toPx(), cap = StrokeCap.Round)
            val sweat = Path().apply {
                moveTo(head.x + 24.dp.toPx(), head.y - 2.dp.toPx())
                lineTo(head.x + 27.dp.toPx(), head.y + 5.dp.toPx())
                lineTo(head.x + 21.dp.toPx(), head.y + 5.dp.toPx())
                close()
            }
            drawPath(sweat, AccentCyan)
        }
        else -> {
            drawCircle(ink, 3.dp.toPx(), Offset(head.x - 10.dp.toPx(), head.y + 1.dp.toPx()))
            drawCircle(ink, 3.dp.toPx(), Offset(head.x + 10.dp.toPx(), head.y + 1.dp.toPx()))
            val mouth = Path().apply {
                moveTo(head.x - 5.dp.toPx(), head.y + 9.dp.toPx())
                quadraticTo(head.x, head.y + 13.dp.toPx(), head.x + 5.dp.toPx(), head.y + 9.dp.toPx())
            }
            drawPath(mouth, ink, style = Stroke(2.dp.toPx(), cap = StrokeCap.Round))
        }
    }
}

private fun DrawScope.drawLaptop(cx: Float, cy: Float, w: Float, h: Float, studying: Boolean, burning: Boolean, tl: Float, tr: Float, head: Offset) {
    val deskY = cy + h * 0.20f
    drawRoundRect(if (burning) Color(0xFF5D1D16) else Color(0xFF4A2B2B).copy(alpha = 0.5f), Offset(cx - w * 0.38f, deskY), Size(w * 0.76f, h * 0.16f), cornerRadius = androidx.compose.ui.geometry.CornerRadius(10.dp.toPx()))
    val lw = w * 0.36f
    val lh = h * 0.19f
    val ll = cx - lw / 2f
    val lt = deskY - lh * 0.78f
    drawRoundRect(if (burning) Color(0xFF330A0A) else Color(0xFF1E1E24), Offset(ll, lt), Size(lw, lh * 0.82f), cornerRadius = androidx.compose.ui.geometry.CornerRadius(5.dp.toPx()))
    drawRoundRect(if (burning) Color(0xFF4E1608) else Color(0xFF0F172A), Offset(ll + 2.5.dp.toPx(), lt + 2.5.dp.toPx()), Size(lw - 5.dp.toPx(), lh * 0.82f - 5.dp.toPx()), cornerRadius = androidx.compose.ui.geometry.CornerRadius(3.dp.toPx()))
    if (studying) {
        drawLine(SuccessGreen, Offset(ll + 5.dp.toPx(), lt + 6.dp.toPx()), Offset(ll + 14.dp.toPx(), lt + 6.dp.toPx()), 1.5.dp.toPx())
        drawLine(AccentCyan, Offset(ll + 16.dp.toPx(), lt + 6.dp.toPx()), Offset(ll + 26.dp.toPx(), lt + 6.dp.toPx()), 1.5.dp.toPx())
        drawLine(FameGold, Offset(ll + 7.dp.toPx(), lt + 10.dp.toPx()), Offset(ll + 20.dp.toPx(), lt + 10.dp.toPx()), 1.5.dp.toPx())
    }
    val baseTop = lt + lh * 0.78f
    drawRoundRect(if (burning) Color(0xFF8B2516) else Color(0xFFD5D8DC), Offset(cx - lw * 0.62f, baseTop), Size(lw * 1.24f, h * 0.055f), cornerRadius = androidx.compose.ui.geometry.CornerRadius(4.dp.toPx()))
    val pawY = baseTop + 4.dp.toPx()
    val pawL = cx - lw * 0.4f
    val pawR = cx + lw * 0.4f
    val dy = if (studying) tl.dp.toPx() else 0f
    val dy2 = if (studying) tr.dp.toPx() else 0f
    drawCircle(if (burning) Color(0xFFFFCC80) else Color(0xFFFFF7F6), 5.5.dp.toPx(), Offset(pawL, pawY + dy))
    drawCircle(if (burning) Color(0xFFFFCC80) else Color(0xFFFFF7F6), 5.5.dp.toPx(), Offset(pawR, pawY + dy2))
}

private fun DrawScope.drawEquippedSkin(skin: String?, cx: Float, cy: Float, w: Float, h: Float, head: Offset) {
    when (skin) {
        "item_cyberpunk" -> {
            drawRoundRect(Color(0xFF00E5FF), Offset(head.x - 16.dp.toPx(), head.y - 6.dp.toPx()), Size(32.dp.toPx(), 9.dp.toPx()), cornerRadius = androidx.compose.ui.geometry.CornerRadius(4.dp.toPx()))
        }
        "item_night_owl_skin" -> {
            val cap = Path().apply {
                moveTo(head.x - 14.dp.toPx(), head.y - 14.dp.toPx())
                quadraticTo(head.x, head.y - 30.dp.toPx(), head.x + 16.dp.toPx(), head.y - 18.dp.toPx())
                lineTo(head.x + 20.dp.toPx(), head.y - 10.dp.toPx())
                close()
            }
            drawPath(cap, Color(0xFF3F51B5))
            drawCircle(Color.White, 3.5.dp.toPx(), Offset(head.x + 20.dp.toPx(), head.y - 9.dp.toPx()))
        }
        "item_golden_desk" -> {
            drawLine(FameGold, Offset(cx - w * 0.38f, cy + h * 0.20f), Offset(cx + w * 0.38f, cy + h * 0.20f), 3.dp.toPx())
        }
    }
}
