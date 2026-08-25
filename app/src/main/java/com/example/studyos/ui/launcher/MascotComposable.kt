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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
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
import kotlin.math.cos
import kotlin.math.sin

enum class MascotState {
    IDLE,
    STUDYING,
    FRUSTRATED,
    BURNING,
    SINGING,
    CRYING,
    PANICKED,
    SLEEPING,
    WORKING_HARD
}

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
    var liveState by remember { mutableStateOf(state) }

    LaunchedEffect(state) {
        if (state == MascotState.IDLE) {
            val story = listOf(
                MascotState.IDLE,
                MascotState.SINGING,
                MascotState.WORKING_HARD,
                MascotState.PANICKED,
                MascotState.CRYING,
                MascotState.SLEEPING
            )

            var index = 0

            while (true) {
                liveState = story[index % story.size]
                index++
                delay(7000L)
            }
        } else if (state == MascotState.STUDYING) {
            liveState = MascotState.WORKING_HARD
        } else {
            liveState = state
        }
    }

    val quotes = remember(liveState) {
        when (liveState) {
            MascotState.BURNING -> listOf(
                "I AM ON FIRE! 3+ HOURS UNSTOPPABLE!",
                "SUPERCHARGED OVERDRIVE!"
            )

            MascotState.STUDYING -> listOf(
                "Typing at 140 WPM! Keep this momentum!",
                "Fame is pouring in! +2 Fame/min!",
                "Focus locked in! We don't stop now!"
            )

            MascotState.FRUSTRATED -> listOf(
                "Shame is rising! Start the timer!",
                "Save me! 25 minutes of focus!"
            )

            MascotState.SINGING -> listOf(
                "Fame anthem mode!",
                "Singing the 4.0 GPA song!",
                "Golden walnut chorus activated!"
            )

            MascotState.CRYING -> listOf(
                "My Golden Walnuts...",
                "Too much shame...",
                "I need a study comeback."
            )

            MascotState.PANICKED -> listOf(
                "Market is shaking!",
                "Distraction detected!",
                "Do not open WhatsApp!"
            )

            MascotState.SLEEPING -> listOf(
                "Zzz... recharging for the next pump.",
                "Even investors sleep.",
                "Dreaming of green candles."
            )

            MascotState.WORKING_HARD -> listOf(
                "Deep work grind!",
                "Typing at 200 WPM!",
                "Your stock is pumping!"
            )

            else -> listOf(
                "Ready to lock in for a 4.0 GPA!",
                "Fame economy is booming!",
                "No procrastination on my watch!"
            )
        }
    }

    Box(
        modifier = modifier
            .size(size)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) {
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
            modifier = Modifier
                .align(Alignment.TopCenter)
                .offset(y = (-40).dp)
        ) {
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = Color.White,
                shadowElevation = 6.dp
            ) {
                Text(
                    bubble ?: " ",
                    color = PrimaryNightMaroon,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                )
            }
        }

        MascotCanvas(
            state = liveState,
            size = size,
            showArc = showArc,
            progressArc = progressArc
        )
    }
}

@Composable
fun MascotCanvas(
    state: MascotState,
    size: Dp,
    showArc: Boolean,
    progressArc: Float
) {
    val equippedSkin by Store.equippedMascot.collectAsState(initial = null)

    val t = rememberInfiniteTransition(label = "mascot")

    val breath by t.animateFloat(
        0.97f,
        1.04f,
        infiniteRepeatable(tween(1400, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "breath"
    )

    val slowBreath by t.animateFloat(
        0.98f,
        1.02f,
        infiniteRepeatable(tween(2200, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "slowBreath"
    )

    val typeNormal by t.animateFloat(
        -7f,
        7f,
        infiniteRepeatable(tween(120, easing = LinearEasing), RepeatMode.Reverse),
        label = "typeNormal"
    )

    val typeFast by t.animateFloat(
        -10f,
        10f,
        infiniteRepeatable(tween(70, easing = LinearEasing), RepeatMode.Reverse),
        label = "typeFast"
    )

    val skinPhase by t.animateFloat(
        0f,
        1f,
        infiniteRepeatable(tween(2200, easing = LinearEasing), RepeatMode.Restart),
        label = "skin"
    )

    val glow by t.animateFloat(
        0.4f,
        0.95f,
        infiniteRepeatable(tween(400, easing = LinearEasing), RepeatMode.Reverse),
        label = "glow"
    )

    val storyPhase by t.animateFloat(
        0f,
        1f,
        infiniteRepeatable(tween(2800, easing = LinearEasing), RepeatMode.Restart),
        label = "storyPhase"
    )

    val isStudying = state == MascotState.STUDYING || state == MascotState.BURNING || state == MascotState.WORKING_HARD
    val isSleeping = state == MascotState.SLEEPING

    val typeL = if (state == MascotState.WORKING_HARD || state == MascotState.BURNING) typeFast else typeNormal
    val typeR = -typeL

    val breathScale = when {
        isSleeping -> slowBreath
        isStudying -> breath
        else -> 1f
    }

    Box(modifier = Modifier.size(size)) {
        Canvas(Modifier.size(size)) {
            val w = this.size.width
            val h = this.size.height
            val cx = w / 2f
            val cy = h / 2f

            drawPremiumAmbience(state, cx, cy, w, h, storyPhase, glow)

            if (showArc) {
                val r = w * 0.45f
                val rect = Rect(cx - r, cy - r - h * 0.05f, cx + r, cy + r - h * 0.05f)

                drawArc(
                    Color.White.copy(alpha = 0.25f),
                    160f,
                    220f,
                    false,
                    topLeft = rect.topLeft,
                    size = rect.size,
                    style = Stroke(4.dp.toPx(), cap = StrokeCap.Round)
                )

                if (progressArc > 0f) {
                    drawArc(
                        if (state == MascotState.BURNING) Color(0xFFFF5722) else Color.White,
                        160f,
                        220f * progressArc,
                        false,
                        topLeft = rect.topLeft,
                        size = rect.size,
                        style = Stroke(4.5.dp.toPx(), cap = StrokeCap.Round)
                    )
                }
            }

            drawStoryBackground(state, cx, cy, w, h, storyPhase, glow)

            val hrX = w * 0.23f * breathScale
            val hrY = h * 0.21f * breathScale
            val head = Offset(cx, cy - h * 0.04f)

            drawOval(
                Color.Black.copy(alpha = 0.18f),
                topLeft = Offset(head.x - hrX, head.y - hrY + 3.dp.toPx()),
                size = Size(hrX * 2, hrY * 2)
            )

            val headColor = when (state) {
                MascotState.BURNING -> Color(0xFFFF5252)
                MascotState.CRYING -> Color(0xFFF2FBFF)
                MascotState.PANICKED -> Color(0xFFFFF3E8)
                MascotState.SLEEPING -> Color(0xFFF7F9FF)
                else -> Color(0xFFFFF7F6)
            }

            val outlineColor = when (state) {
                MascotState.BURNING -> Color(0xFFB71C1C)
                MascotState.CRYING -> Color(0xFF0097A7)
                MascotState.PANICKED -> Color(0xFFE53935)
                MascotState.SLEEPING -> Color(0xFF3F51B5)
                MascotState.SINGING -> Color(0xFFFFB300)
                MascotState.WORKING_HARD -> Color(0xFFB71C1C)
                else -> PrimaryCoralDark.copy(alpha = 0.35f)
            }

            drawOval(
                headColor,
                topLeft = Offset(head.x - hrX, head.y - hrY),
                size = Size(hrX * 2, hrY * 2)
            )

            drawOval(
                outlineColor,
                topLeft = Offset(head.x - hrX, head.y - hrY),
                size = Size(hrX * 2, hrY * 2),
                style = Stroke(1.8.dp.toPx())
            )

            val blush = if (state == MascotState.BURNING) FameGold else PrimaryCoral

            drawCircle(
                blush.copy(alpha = 0.4f),
                4.5.dp.toPx(),
                Offset(head.x - 14.dp.toPx(), head.y + 7.dp.toPx())
            )

            drawCircle(
                blush.copy(alpha = 0.4f),
                4.5.dp.toPx(),
                Offset(head.x + 14.dp.toPx(), head.y + 7.dp.toPx())
            )

            val leaf = Path().apply {
                moveTo(head.x, head.y - hrY)
                quadraticTo(head.x + 8.dp.toPx(), head.y - hrY - 11.dp.toPx(), head.x + 15.dp.toPx(), head.y - hrY - 9.dp.toPx())
                quadraticTo(head.x + 5.dp.toPx(), head.y - hrY - 2.dp.toPx(), head.x, head.y - hrY)
                close()
            }

            drawPath(
                leaf,
                if (state == MascotState.BURNING) Color(0xFFFF9800) else SuccessGreen
            )

            drawFace(state, head)

            drawLaptop(
                cx = cx,
                cy = cy,
                w = w,
                h = h,
                studying = isStudying && !isSleeping,
                burning = state == MascotState.BURNING,
                tl = typeL,
                tr = typeR,
                head = head,
                state = state
            )

            drawStoryFront(state, cx, cy, w, h, head, storyPhase, glow)

            drawEquippedSkin(equippedSkin, cx, cy, w, h, head)
            drawExtraSkins(equippedSkin, cx, cy, w, h, head, skinPhase)
        }
    }
}

private fun DrawScope.drawPremiumAmbience(
    state: MascotState,
    cx: Float,
    cy: Float,
    w: Float,
    h: Float,
    phase: Float,
    glow: Float
) {
    val twoPi = 2f * Math.PI.toFloat()

    val auraColor = when (state) {
        MascotState.SINGING -> Color(0xFFFFD700)
        MascotState.CRYING -> Color(0xFF00BCD4)
        MascotState.PANICKED -> Color(0xFFE53935)
        MascotState.SLEEPING -> Color(0xFF3F51B5)
        MascotState.WORKING_HARD -> Color(0xFFE53935)
        MascotState.BURNING -> Color(0xFFFF5722)
        else -> Color(0xFFE53935)
    }

    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(
                auraColor.copy(alpha = 0.18f * glow),
                Color.Transparent
            ),
            center = Offset(cx, cy - h * 0.05f),
            radius = w * 0.55f
        ),
        center = Offset(cx, cy - h * 0.05f),
        radius = w * 0.55f
    )

    for (i in 0 until 16) {
        val px = (((i * 47) % 100) / 100f) * w
        val pyBase = (((i * 89) % 100) / 100f) * h
        val floatY = (phase + i * 0.07f) % 1f
        val py = pyBase - floatY * h * 0.08f

        val alpha = 0.08f + 0.18f * ((sin((phase + i * 0.13f) * twoPi) + 1f) / 2f)

        val color = if (i % 3 == 0) {
            Color(0xFFFFD700).copy(alpha = alpha)
        } else {
            Color(0xFFE53935).copy(alpha = alpha * 0.8f)
        }

        drawCircle(
            color = color,
            radius = (1.4f + (i % 3) * 0.35f).dp.toPx(),
            center = Offset(px, py)
        )
    }
}

private fun DrawScope.drawStoryBackground(
    state: MascotState,
    cx: Float,
    cy: Float,
    w: Float,
    h: Float,
    phase: Float,
    glow: Float
) {
    val twoPi = 2f * Math.PI.toFloat()

    val chartColor = when (state) {
        MascotState.PANICKED, MascotState.CRYING -> Color(0xFFFF5252)
        MascotState.SINGING, MascotState.BURNING, MascotState.WORKING_HARD -> Color(0xFF4CAF50)
        else -> Color(0xFFE53935)
    }

    val chartPath = Path().apply {
        moveTo(cx - w * 0.38f, cy - h * 0.28f)

        for (i in 1..6) {
            val x = cx - w * 0.38f + i * w * 0.76f / 6f
            val y = cy - h * 0.28f +
                sin(phase * twoPi + i * 1.2f) * h * 0.03f +
                if (i % 2 == 0) -h * 0.02f else h * 0.02f

            lineTo(x, y)
        }
    }

    drawPath(
        path = chartPath,
        color = chartColor.copy(alpha = 0.18f),
        style = Stroke(1.5.dp.toPx(), cap = StrokeCap.Round)
    )

    val lampX = cx - w * 0.36f
    val lampBaseY = cy + h * 0.24f

    drawRoundRect(
        color = Color(0xFF2D2D2D),
        topLeft = Offset(lampX, lampBaseY - 24.dp.toPx()),
        size = Size(3.dp.toPx(), 24.dp.toPx()),
        cornerRadius = androidx.compose.ui.geometry.CornerRadius(1.5.dp.toPx())
    )

    drawCircle(
        color = Color(0xFFFFD700).copy(alpha = 0.65f * glow),
        radius = 5.dp.toPx(),
        center = Offset(lampX + 1.5.dp.toPx(), lampBaseY - 26.dp.toPx())
    )

    val bookX = cx + w * 0.30f
    val bookY = cy + h * 0.22f

    drawRoundRect(
        color = Color(0xFFB71C1C),
        topLeft = Offset(bookX, bookY),
        size = Size(18.dp.toPx(), 5.dp.toPx()),
        cornerRadius = androidx.compose.ui.geometry.CornerRadius(2.dp.toPx())
    )

    drawRoundRect(
        color = Color(0xFFFFD700),
        topLeft = Offset(bookX + 2.dp.toPx(), bookY - 5.dp.toPx()),
        size = Size(15.dp.toPx(), 5.dp.toPx()),
        cornerRadius = androidx.compose.ui.geometry.CornerRadius(2.dp.toPx())
    )

    drawRoundRect(
        color = Color(0xFF20B2AA),
        topLeft = Offset(bookX + 4.dp.toPx(), bookY - 10.dp.toPx()),
        size = Size(12.dp.toPx(), 5.dp.toPx()),
        cornerRadius = androidx.compose.ui.geometry.CornerRadius(2.dp.toPx())
    )

    if (state == MascotState.SLEEPING) {
        for (i in 0 until 5) {
            val starX = cx + w * 0.20f + i * 8.dp.toPx()
            val starY = cy - h * 0.34f + ((i * 31) % 14).dp.toPx()
            val alpha = 0.35f + 0.45f * ((sin(phase * twoPi + i.toFloat()) + 1f) / 2f)

            drawCircle(
                color = Color(0xFFDDE7FF).copy(alpha = alpha),
                radius = 1.4.dp.toPx(),
                center = Offset(starX, starY)
            )
        }
    }
}

private fun DrawScope.drawStoryFront(
    state: MascotState,
    cx: Float,
    cy: Float,
    w: Float,
    h: Float,
    head: Offset,
    phase: Float,
    glow: Float
) {
    when (state) {
        MascotState.SINGING -> {
            drawHeadphones(head)
            drawMusicNotes(head, phase)
        }

        MascotState.CRYING -> {
            drawTears(head, phase)
        }

        MascotState.PANICKED -> {
            drawPanicMarks(head, phase)
        }

        MascotState.SLEEPING -> {
            drawZzz(head, phase)
        }

        MascotState.WORKING_HARD -> {
            drawCoffee(cx, cy, w, h, phase)
            drawFocusSparks(head, phase, Color(0xFFFFD700))
        }

        MascotState.BURNING -> {
            drawFocusSparks(head, phase, Color(0xFFFF5722))
        }

        else -> {
            drawGoldenWalnuts(head, phase)
        }
    }
}

private fun DrawScope.drawFace(state: MascotState, head: Offset) {
    val ink = PrimaryCoralDark

    when (state) {
        MascotState.BURNING -> {
            drawRoundRect(
                Color(0xFF1E1E24),
                Offset(head.x - 14.dp.toPx(), head.y - 4.dp.toPx()),
                Size(12.dp.toPx(), 8.dp.toPx()),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(2.dp.toPx())
            )

            drawRoundRect(
                Color(0xFF1E1E24),
                Offset(head.x + 2.dp.toPx(), head.y - 4.dp.toPx()),
                Size(12.dp.toPx(), 8.dp.toPx()),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(2.dp.toPx())
            )

            drawLine(
                FameGold,
                Offset(head.x - 2.dp.toPx(), head.y - 1.dp.toPx()),
                Offset(head.x + 2.dp.toPx(), head.y - 1.dp.toPx()),
                2.dp.toPx()
            )

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

        MascotState.SINGING -> {
            val leftEye = Path().apply {
                moveTo(head.x - 13.dp.toPx(), head.y + 1.dp.toPx())
                quadraticTo(head.x - 10.dp.toPx(), head.y - 2.dp.toPx(), head.x - 7.dp.toPx(), head.y + 1.dp.toPx())
            }

            val rightEye = Path().apply {
                moveTo(head.x + 7.dp.toPx(), head.y + 1.dp.toPx())
                quadraticTo(head.x + 10.dp.toPx(), head.y - 2.dp.toPx(), head.x + 13.dp.toPx(), head.y + 1.dp.toPx())
            }

            drawPath(leftEye, ink, style = Stroke(2.dp.toPx(), cap = StrokeCap.Round))
            drawPath(rightEye, ink, style = Stroke(2.dp.toPx(), cap = StrokeCap.Round))

            drawOval(
                color = Color(0xFFB71C1C),
                topLeft = Offset(head.x - 4.dp.toPx(), head.y + 7.dp.toPx()),
                size = Size(8.dp.toPx(), 6.dp.toPx())
            )
        }

        MascotState.CRYING -> {
            drawLine(ink, Offset(head.x - 13.dp.toPx(), head.y - 2.dp.toPx()), Offset(head.x - 7.dp.toPx(), head.y + 1.dp.toPx()), 2.2.dp.toPx(), cap = StrokeCap.Round)
            drawLine(ink, Offset(head.x + 13.dp.toPx(), head.y - 2.dp.toPx()), Offset(head.x + 7.dp.toPx(), head.y + 1.dp.toPx()), 2.2.dp.toPx(), cap = StrokeCap.Round)

            drawCircle(ink, 2.2.dp.toPx(), Offset(head.x - 10.dp.toPx(), head.y + 2.dp.toPx()))
            drawCircle(ink, 2.2.dp.toPx(), Offset(head.x + 10.dp.toPx(), head.y + 2.dp.toPx()))

            val frown = Path().apply {
                moveTo(head.x - 5.dp.toPx(), head.y + 12.dp.toPx())
                quadraticTo(head.x, head.y + 8.dp.toPx(), head.x + 5.dp.toPx(), head.y + 12.dp.toPx())
            }

            drawPath(frown, ink, style = Stroke(2.dp.toPx(), cap = StrokeCap.Round))
        }

        MascotState.PANICKED -> {
            drawCircle(Color.White, 4.5.dp.toPx(), Offset(head.x - 10.dp.toPx(), head.y))
            drawCircle(Color.White, 4.5.dp.toPx(), Offset(head.x + 10.dp.toPx(), head.y))

            drawCircle(ink, 1.8.dp.toPx(), Offset(head.x - 10.dp.toPx(), head.y))
            drawCircle(ink, 1.8.dp.toPx(), Offset(head.x + 10.dp.toPx(), head.y))

            drawOval(
                color = ink,
                topLeft = Offset(head.x - 2.dp.toPx(), head.y + 8.dp.toPx()),
                size = Size(4.dp.toPx(), 6.dp.toPx())
            )
        }

        MascotState.SLEEPING -> {
            drawLine(ink, Offset(head.x - 13.dp.toPx(), head.y + 1.dp.toPx()), Offset(head.x - 7.dp.toPx(), head.y + 1.dp.toPx()), 2.dp.toPx(), cap = StrokeCap.Round)
            drawLine(ink, Offset(head.x + 7.dp.toPx(), head.y + 1.dp.toPx()), Offset(head.x + 13.dp.toPx(), head.y + 1.dp.toPx()), 2.dp.toPx(), cap = StrokeCap.Round)

            drawCircle(ink.copy(alpha = 0.6f), 1.5.dp.toPx(), Offset(head.x, head.y + 9.dp.toPx()))
        }

        MascotState.WORKING_HARD -> {
            drawLine(ink, Offset(head.x - 13.dp.toPx(), head.y - 2.dp.toPx()), Offset(head.x - 7.dp.toPx(), head.y), 2.2.dp.toPx(), cap = StrokeCap.Round)
            drawLine(ink, Offset(head.x + 13.dp.toPx(), head.y - 2.dp.toPx()), Offset(head.x + 7.dp.toPx(), head.y), 2.2.dp.toPx(), cap = StrokeCap.Round)

            drawCircle(ink, 2.dp.toPx(), Offset(head.x - 10.dp.toPx(), head.y + 1.dp.toPx()))
            drawCircle(ink, 2.dp.toPx(), Offset(head.x + 10.dp.toPx(), head.y + 1.dp.toPx()))

            drawLine(ink, Offset(head.x - 4.dp.toPx(), head.y + 9.dp.toPx()), Offset(head.x + 4.dp.toPx(), head.y + 9.dp.toPx()), 2.dp.toPx(), cap = StrokeCap.Round)
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

private fun DrawScope.drawLaptop(
    cx: Float,
    cy: Float,
    w: Float,
    h: Float,
    studying: Boolean,
    burning: Boolean,
    tl: Float,
    tr: Float,
    head: Offset,
    state: MascotState
) {
    val deskY = cy + h * 0.20f

    drawRoundRect(
        if (burning) Color(0xFF5D1D16) else Color(0xFF4A2B2B).copy(alpha = 0.5f),
        Offset(cx - w * 0.38f, deskY),
        Size(w * 0.76f, h * 0.16f),
        cornerRadius = androidx.compose.ui.geometry.CornerRadius(10.dp.toPx())
    )

    val lw = w * 0.36f
    val lh = h * 0.19f
    val ll = cx - lw / 2f
    val lt = deskY - lh * 0.78f

    drawRoundRect(
        if (burning) Color(0xFF330A0A) else Color(0xFF1E1E24),
        Offset(ll, lt),
        Size(lw, lh * 0.82f),
        cornerRadius = androidx.compose.ui.geometry.CornerRadius(5.dp.toPx())
    )

    drawRoundRect(
        if (burning) Color(0xFF4E1608) else Color(0xFF0F172A),
        Offset(ll + 2.5.dp.toPx(), lt + 2.5.dp.toPx()),
        Size(lw - 5.dp.toPx(), lh * 0.82f - 5.dp.toPx()),
        cornerRadius = androidx.compose.ui.geometry.CornerRadius(3.dp.toPx())
    )

    if (state == MascotState.SINGING) {
        for (i in 0 until 4) {
            val barX = ll + 6.dp.toPx() + i * 6.dp.toPx()
            val barH = 4.dp.toPx() + ((i * 37) % 7).dp.toPx()

            drawRoundRect(
                FameGold.copy(alpha = 0.8f),
                Offset(barX, lt + 14.dp.toPx() - barH),
                Size(3.dp.toPx(), barH),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(1.5.dp.toPx())
            )
        }
    } else if (studying) {
        drawLine(SuccessGreen, Offset(ll + 5.dp.toPx(), lt + 6.dp.toPx()), Offset(ll + 14.dp.toPx(), lt + 6.dp.toPx()), 1.5.dp.toPx())
        drawLine(AccentCyan, Offset(ll + 16.dp.toPx(), lt + 6.dp.toPx()), Offset(ll + 26.dp.toPx(), lt + 6.dp.toPx()), 1.5.dp.toPx())
        drawLine(FameGold, Offset(ll + 7.dp.toPx(), lt + 10.dp.toPx()), Offset(ll + 20.dp.toPx(), lt + 10.dp.toPx()), 1.5.dp.toPx())
    }

    val baseTop = lt + lh * 0.78f

    drawRoundRect(
        if (burning) Color(0xFF8B2516) else Color(0xFFD5D8DC),
        Offset(cx - lw * 0.62f, baseTop),
        Size(lw * 1.24f, h * 0.055f),
        cornerRadius = androidx.compose.ui.geometry.CornerRadius(4.dp.toPx())
    )

    if (state != MascotState.SLEEPING) {
        val pawY = baseTop + 4.dp.toPx()
        val pawL = cx - lw * 0.4f
        val pawR = cx + lw * 0.4f

        drawCircle(
            if (burning) Color(0xFFFFCC80) else Color(0xFFFFF7F6),
            5.5.dp.toPx(),
            Offset(pawL, pawY + tl.dp.toPx())
        )

        drawCircle(
            if (burning) Color(0xFFFFCC80) else Color(0xFFFFF7F6),
            5.5.dp.toPx(),
            Offset(pawR, pawY + tr.dp.toPx())
        )
    }
}

private fun DrawScope.drawHeadphones(head: Offset) {
    drawArc(
        color = Color(0xFFFFD700),
        startAngle = 180f,
        sweepAngle = 180f,
        useCenter = false,
        topLeft = Offset(head.x - 16.dp.toPx(), head.y - 24.dp.toPx()),
        size = Size(32.dp.toPx(), 32.dp.toPx()),
        style = Stroke(3.dp.toPx(), cap = StrokeCap.Round)
    )

    drawCircle(
        color = Color(0xFFB71C1C),
        radius = 6.dp.toPx(),
        center = Offset(head.x - 16.dp.toPx(), head.y)
    )

    drawCircle(
        color = Color(0xFFB71C1C),
        radius = 6.dp.toPx(),
        center = Offset(head.x + 16.dp.toPx(), head.y)
    )
}

private fun DrawScope.drawMusicNotes(head: Offset, phase: Float) {
    val twoPi = 2f * Math.PI.toFloat()

    for (i in 0 until 3) {
        val t = (phase + i * 0.25f) % 1f
        val x = head.x + (i - 1) * 14.dp.toPx() + sin(t * twoPi) * 4.dp.toPx()
        val y = head.y - 20.dp.toPx() - t * 24.dp.toPx()
        val alpha = if (t > 0.75f) (1f - t) / 0.25f else 1f

        drawCircle(
            color = Color(0xFFFFD700).copy(alpha = alpha),
            radius = 2.2.dp.toPx(),
            center = Offset(x, y)
        )

        drawLine(
            color = Color(0xFFFFD700).copy(alpha = alpha),
            start = Offset(x + 2.dp.toPx(), y),
            end = Offset(x + 2.dp.toPx(), y - 7.dp.toPx()),
            strokeWidth = 1.4.dp.toPx(),
            cap = StrokeCap.Round
        )
    }
}

private fun DrawScope.drawTears(head: Offset, phase: Float) {
    for (side in listOf(-1, 1)) {
        for (drop in 0 until 2) {
            val t = (phase + drop * 0.4f + if (side < 0) 0f else 0.2f) % 1f
            val x = head.x + side * 10.dp.toPx()
            val y = head.y + 3.dp.toPx() + t * 16.dp.toPx()
            val alpha = if (t > 0.75f) (1f - t) / 0.25f else 1f

            drawCircle(
                color = Color(0xFF00BCD4).copy(alpha = alpha),
                radius = 1.8.dp.toPx(),
                center = Offset(x, y)
            )
        }
    }
}

private fun DrawScope.drawPanicMarks(head: Offset, phase: Float) {
    val twoPi = 2f * Math.PI.toFloat()
    val blink = (sin(phase * twoPi * 3f) + 1f) / 2f

    val exX = head.x + 20.dp.toPx()
    val exY = head.y - 22.dp.toPx()

    drawRoundRect(
        color = Color(0xFFE53935).copy(alpha = 0.7f + 0.3f * blink),
        topLeft = Offset(exX - 1.5.dp.toPx(), exY),
        size = Size(3.dp.toPx(), 8.dp.toPx()),
        cornerRadius = androidx.compose.ui.geometry.CornerRadius(1.5.dp.toPx())
    )

    drawCircle(
        color = Color(0xFFE53935).copy(alpha = 0.7f + 0.3f * blink),
        radius = 1.5.dp.toPx(),
        center = Offset(exX, exY + 11.dp.toPx())
    )

    for (i in 0 until 2) {
        val t = (phase + i * 0.35f) % 1f
        val x = head.x + if (i == 0) -22.dp.toPx() else 24.dp.toPx()
        val y = head.y - 4.dp.toPx() + t * 12.dp.toPx()
        val alpha = if (t > 0.75f) (1f - t) / 0.25f else 1f

        drawCircle(
            color = Color(0xFF00BCD4).copy(alpha = alpha),
            radius = 1.6.dp.toPx(),
            center = Offset(x, y)
        )
    }
}

private fun DrawScope.drawZzz(head: Offset, phase: Float) {
    val positions = listOf(
        Offset(head.x + 18.dp.toPx(), head.y - 24.dp.toPx()),
        Offset(head.x + 26.dp.toPx(), head.y - 34.dp.toPx()),
        Offset(head.x + 34.dp.toPx(), head.y - 44.dp.toPx())
    )

    positions.forEachIndexed { index, base ->
        val t = (phase + index * 0.22f) % 1f
        val alpha = if (t > 0.75f) (1f - t) / 0.25f else 1f
        val scale = 1f + index * 0.25f

        val x = base.x + sin(t * 2f * Math.PI.toFloat()) * 2.dp.toPx()
        val y = base.y - t * 6.dp.toPx()

        val z = Path().apply {
            moveTo(x, y)
            lineTo(x + 6.dp.toPx() * scale, y)
            lineTo(x, y + 6.dp.toPx() * scale)
            lineTo(x + 6.dp.toPx() * scale, y + 6.dp.toPx() * scale)
        }

        drawPath(
            path = z,
            color = Color(0xFFDDE7FF).copy(alpha = alpha),
            style = Stroke(1.6.dp.toPx(), cap = StrokeCap.Round)
        )
    }
}

private fun DrawScope.drawCoffee(cx: Float, cy: Float, w: Float, h: Float, phase: Float) {
    val twoPi = 2f * Math.PI.toFloat()

    val cupX = cx + w * 0.28f
    val cupY = cy + h * 0.18f

    drawRoundRect(
        color = Color(0xFFD5D8DC),
        topLeft = Offset(cupX, cupY),
        size = Size(12.dp.toPx(), 10.dp.toPx()),
        cornerRadius = androidx.compose.ui.geometry.CornerRadius(2.dp.toPx())
    )

    drawRoundRect(
        color = Color(0xFF8B4513),
        topLeft = Offset(cupX + 1.5.dp.toPx(), cupY + 1.5.dp.toPx()),
        size = Size(9.dp.toPx(), 3.dp.toPx()),
        cornerRadius = androidx.compose.ui.geometry.CornerRadius(1.dp.toPx())
    )

    drawArc(
        color = Color(0xFFD5D8DC),
        startAngle = -90f,
        sweepAngle = 180f,
        useCenter = false,
        topLeft = Offset(cupX + 12.dp.toPx(), cupY + 2.dp.toPx()),
        size = Size(6.dp.toPx(), 6.dp.toPx()),
        style = Stroke(1.5.dp.toPx(), cap = StrokeCap.Round)
    )

    for (i in 0 until 2) {
        val t = (phase + i * 0.4f) % 1f
        val sx = cupX + 4.dp.toPx() + i * 4.dp.toPx() + sin(t * twoPi) * 1.5.dp.toPx()
        val sy = cupY - 2.dp.toPx() - t * 10.dp.toPx()
        val alpha = if (t > 0.75f) (1f - t) / 0.25f else 1f

        drawCircle(
            color = Color.White.copy(alpha = alpha * 0.5f),
            radius = 1.2.dp.toPx(),
            center = Offset(sx, sy)
        )
    }
}

private fun DrawScope.drawFocusSparks(head: Offset, phase: Float, color: Color) {
    val twoPi = 2f * Math.PI.toFloat()

    for (i in 0 until 8) {
        val angle = phase * twoPi + i * (twoPi / 8f)
        val radius = 26.dp.toPx() + sin(phase * twoPi * 2f + i.toFloat()) * 3.dp.toPx()

        val x = head.x + cos(angle) * radius
        val y = head.y - 8.dp.toPx() + sin(angle) * radius * 0.65f

        drawCircle(
            color = color.copy(alpha = 0.55f),
            radius = 1.3.dp.toPx(),
            center = Offset(x, y)
        )
    }
}

private fun DrawScope.drawGoldenWalnuts(head: Offset, phase: Float) {
    val twoPi = 2f * Math.PI.toFloat()

    for (i in 0 until 3) {
        val t = (phase + i * 0.3f) % 1f
        val x = head.x + (i - 1) * 18.dp.toPx()
        val y = head.y - 26.dp.toPx() + sin(t * twoPi) * 3.dp.toPx()
        val alpha = 0.45f + 0.35f * ((sin(t * twoPi) + 1f) / 2f)

        drawCircle(
            color = Color(0xFFFFD700).copy(alpha = alpha),
            radius = 2.4.dp.toPx(),
            center = Offset(x, y)
        )

        drawCircle(
            color = Color.White.copy(alpha = alpha * 0.7f),
            radius = 0.8.dp.toPx(),
            center = Offset(x - 0.8.dp.toPx(), y - 0.8.dp.toPx())
        )
    }
}

private fun DrawScope.drawEquippedSkin(skin: String?, cx: Float, cy: Float, w: Float, h: Float, head: Offset) {
    when (skin) {
        "item_cyberpunk" -> {
            drawRoundRect(
                Color(0xFF00E5FF),
                Offset(head.x - 16.dp.toPx(), head.y - 6.dp.toPx()),
                Size(32.dp.toPx(), 9.dp.toPx()),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(4.dp.toPx())
            )
        }

        "item_night_owl_skin" -> {
            val cap = Path().apply {
                moveTo(head.x - 14.dp.toPx(), head.y - 14.dp.toPx())
                quadraticTo(head.x, head.y - 30.dp.toPx(), head.x + 16.dp.toPx(), head.y - 18.dp.toPx())
                lineTo(head.x + 20.dp.toPx(), head.y - 10.dp.toPx())
                close()
            }

            drawPath(cap, Color(0xFF3F51B5))

            drawCircle(
                Color.White,
                3.5.dp.toPx(),
                Offset(head.x + 20.dp.toPx(), head.y - 9.dp.toPx())
            )
        }

        "item_golden_desk" -> {
            drawLine(
                FameGold,
                Offset(cx - w * 0.38f, cy + h * 0.20f),
                Offset(cx + w * 0.38f, cy + h * 0.20f),
                3.dp.toPx()
            )
        }
    }
}
