package com.example.studyos.ui.launcher

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
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
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.scale
import kotlin.math.PI
import kotlin.math.min
import kotlin.math.sin

object BullLottie {
    val JSON = """
{"v":"5.7.4","fr":60,"ip":0,"op":180,"w":512,"h":512,"nm":"bull_market","ddd":0,"assets":[],"layers":[
{"ty":4,"nm":"bull","ind":1,"sr":1,"ip":0,"op":180,"st":0,"ks":{"o":{"a":0,"k":100},"r":{"a":0,"k":0},"p":{"a":1,"k":[{"t":0,"s":[256,172,0]},{"t":45,"s":[256,162,0]},{"t":90,"s":[256,172,0]},{"t":135,"s":[256,164,0]},{"t":180,"s":[256,172,0]}]},"a":{"a":0,"k":[0,0,0]},"s":{"a":0,"k":[100,100,100]}},"shapes":[
{"ty":"gr","nm":"eyes","it":[{"ty":"el","p":{"a":0,"k":[-45,0]},"s":{"a":0,"k":[26,26]}},{"ty":"fl","c":{"a":0,"k":[1,1,1,1]},"o":{"a":0,"k":100}},{"ty":"el","p":{"a":0,"k":[45,0]},"s":{"a":0,"k":[26,26]}},{"ty":"fl","c":{"a":0,"k":[1,1,1,1]},"o":{"a":0,"k":100}},{"ty":"el","p":{"a":0,"k":[-45,0]},"s":{"a":0,"k":[11,11]}},{"ty":"fl","c":{"a":0,"k":[0.1,0.02,0.02,1]},"o":{"a":0,"k":100}},{"ty":"el","p":{"a":0,"k":[45,0]},"s":{"a":0,"k":[11,11]}},{"ty":"fl","c":{"a":0,"k":[0.1,0.02,0.02,1]},"o":{"a":0,"k":100}},{"ty":"tr","p":{"a":0,"k":[0,-25]},"a":{"a":0,"k":[0,0]},"s":{"a":1,"k":[{"t":0,"s":[100,100]},{"t":85,"s":[100,100]},{"t":90,"s":[100,8]},{"t":95,"s":[100,100]},{"t":180,"s":[100,100]}]},"r":{"a":0,"k":0},"o":{"a":0,"k":100}}]},
{"ty":"gr","nm":"nostrils","it":[{"ty":"el","p":{"a":0,"k":[-22,0]},"s":{"a":0,"k":[14,20]}},{"ty":"fl","c":{"a":0,"k":[0.25,0.06,0.05,1]},"o":{"a":0,"k":100}},{"ty":"el","p":{"a":0,"k":[22,0]},"s":{"a":0,"k":[14,20]}},{"ty":"fl","c":{"a":0,"k":[0.25,0.06,0.05,1]},"o":{"a":0,"k":100}},{"ty":"tr","p":{"a":0,"k":[0,52]},"a":{"a":0,"k":[0,0]},"s":{"a":0,"k":[100,100]},"r":{"a":0,"k":0},"o":{"a":0,"k":100}}]},
{"ty":"gr","nm":"muzzle","it":[{"ty":"el","p":{"a":0,"k":[0,0]},"s":{"a":0,"k":[120,84]}},{"ty":"fl","c":{"a":0,"k":[0.42,0.14,0.11,1]},"o":{"a":0,"k":100}},{"ty":"st","c":{"a":0,"k":[1,0.84,0,1]},"o":{"a":0,"k":50},"w":{"a":0,"k":3}},{"ty":"tr","p":{"a":0,"k":[0,55]},"a":{"a":0,"k":[0,0]},"s":{"a":1,"k":[{"t":0,"s":[100,100]},{"t":45,"s":[103,103]},{"t":90,"s":[100,100]},{"t":135,"s":[103,103]},{"t":180,"s":[100,100]}]},"r":{"a":0,"k":0},"o":{"a":0,"k":100}}]},
{"ty":"gr","nm":"ring","it":[{"ty":"el","p":{"a":0,"k":[0,22]},"s":{"a":0,"k":[44,44]}},{"ty":"st","c":{"a":0,"k":[1,0.84,0,1]},"o":{"a":0,"k":100},"w":{"a":0,"k":6}},{"ty":"tr","p":{"a":0,"k":[0,66]},"a":{"a":0,"k":[0,0]},"s":{"a":0,"k":[100,100]},"r":{"a":1,"k":[{"t":0,"s":[0]},{"t":30,"s":[8]},{"t":60,"s":[0]},{"t":90,"s":[-8]},{"t":120,"s":[0]},{"t":150,"s":[6]},{"t":180,"s":[0]}]},"o":{"a":0,"k":100}}]},
{"ty":"gr","nm":"horns","it":[{"ty":"sh","ks":{"a":0,"k":{"i":[[0,0],[-6,26],[6,-24]],"o":[[8,-26],[14,18],[-12,4]],"v":[[48,-58],[118,-132],[84,-38]],"c":true}}},{"ty":"fl","c":{"a":0,"k":[1,0.84,0,1]},"o":{"a":0,"k":100}},{"ty":"sh","ks":{"a":0,"k":{"i":[[0,0],[6,26],[-6,-24]],"o":[[-8,-26],[-14,18],[12,4]],"v":[[-48,-58],[-118,-132],[-84,-38]],"c":true}}},{"ty":"fl","c":{"a":0,"k":[1,0.84,0,1]},"o":{"a":0,"k":100}},{"ty":"tr","p":{"a":0,"k":[0,0]},"a":{"a":0,"k":[0,-50]},"s":{"a":0,"k":[100,100]},"r":{"a":1,"k":[{"t":0,"s":[0]},{"t":45,"s":[3]},{"t":90,"s":[0]},{"t":135,"s":[-3]},{"t":180,"s":[0]}]},"o":{"a":0,"k":100}}]},
{"ty":"gr","nm":"ears","it":[{"ty":"el","p":{"a":0,"k":[-98,-28]},"s":{"a":0,"k":[42,26]}},{"ty":"fl","c":{"a":0,"k":[0.16,0.04,0.04,1]},"o":{"a":0,"k":100}},{"ty":"el","p":{"a":0,"k":[98,-28]},"s":{"a":0,"k":[42,26]}},{"ty":"fl","c":{"a":0,"k":[0.16,0.04,0.04,1]},"o":{"a":0,"k":100}},{"ty":"tr","p":{"a":0,"k":[0,0]},"a":{"a":0,"k":[0,0]},"s":{"a":0,"k":[100,100]},"r":{"a":0,"k":0},"o":{"a":0,"k":100}}]},
{"ty":"gr","nm":"head","it":[{"ty":"el","p":{"a":0,"k":[0,-10]},"s":{"a":0,"k":[190,172]}},{"ty":"fl","c":{"a":0,"k":[0.1,0.02,0.02,1]},"o":{"a":0,"k":100}},{"ty":"st","c":{"a":0,"k":[1,0.84,0,1]},"o":{"a":0,"k":55},"w":{"a":0,"k":4}},{"ty":"tr","p":{"a":0,"k":[0,0]},"a":{"a":0,"k":[0,0]},"s":{"a":0,"k":[100,100]},"r":{"a":0,"k":0},"o":{"a":0,"k":100}}]}
]},
{"ty":4,"nm":"glow","ind":2,"sr":1,"ip":0,"op":180,"st":0,"ks":{"o":{"a":1,"k":[{"t":0,"s":[50]},{"t":45,"s":[85]},{"t":90,"s":[50]},{"t":135,"s":[85]},{"t":180,"s":[50]}]},"r":{"a":0,"k":0},"p":{"a":0,"k":[256,170,0]},"a":{"a":0,"k":[0,0,0]},"s":{"a":1,"k":[{"t":0,"s":[100,100,100]},{"t":90,"s":[112,112,100]},{"t":180,"s":[100,100,100]}]}},"shapes":[{"ty":"gr","it":[{"ty":"el","p":{"a":0,"k":[0,0]},"s":{"a":0,"k":[360,360]}},{"ty":"gf","o":{"a":0,"k":100},"r":2,"g":{"p":3,"k":{"a":0,"k":[0,1,0.84,0,0.55,0.9,0.6,0,1,0.7,0.4,0]}},"s":{"a":0,"k":[0,0]},"e":{"a":0,"k":[180,0]},"t":2,"a":{"a":0,"k":0},"h":{"a":0,"k":0}},{"ty":"tr","p":{"a":0,"k":[0,0]},"a":{"a":0,"k":[0,0]},"s":{"a":0,"k":[100,100]},"r":{"a":0,"k":0},"o":{"a":0,"k":100}}]}]},
{"ty":4,"nm":"chart","ind":3,"sr":1,"ip":0,"op":180,"st":0,"ks":{"o":{"a":0,"k":90},"r":{"a":0,"k":0},"p":{"a":0,"k":[256,420,0]},"a":{"a":0,"k":[0,0,0]},"s":{"a":0,"k":[100,100,100]}},"shapes":[{"ty":"gr","it":[{"ty":"sh","ks":{"a":0,"k":{"i":[[0,0],[0,0],[0,0],[0,0],[0,0],[0,0]],"o":[[0,0],[0,0],[0,0],[0,0],[0,0],[0,0]],"v":[[-210,70],[-130,30],[-60,52],[30,-16],[110,6],[210,-48]],"c":false}}},{"ty":"st","c":{"a":0,"k":[1,0.84,0,1]},"o":{"a":0,"k":100},"w":{"a":0,"k":5},"lc":2,"lj":2},{"ty":"tm","s":{"a":0,"k":0},"e":{"a":1,"k":[{"t":10,"s":[0]},{"t":80,"s":[100]}]},"o":{"a":0,"k":0},"m":1},{"ty":"tr","p":{"a":0,"k":[0,0]},"a":{"a":0,"k":[0,0]},"s":{"a":0,"k":[100,100]},"r":{"a":0,"k":0},"o":{"a":0,"k":100}}]}]},
{"ty":4,"nm":"bars","ind":4,"sr":1,"ip":0,"op":180,"st":0,"ks":{"o":{"a":0,"k":100},"r":{"a":0,"k":0},"p":{"a":0,"k":[256,470,0]},"a":{"a":0,"k":[0,0,0]},"s":{"a":0,"k":[100,100,100]}},"shapes":[
{"ty":"gr","nm":"bar1","it":[{"ty":"rc","p":{"a":0,"k":[0,-35]},"s":{"a":0,"k":[40,70]},"r":{"a":0,"k":6}},{"ty":"fl","c":{"a":0,"k":[0.3,0.69,0.31,1]},"o":{"a":0,"k":85}},{"ty":"tr","p":{"a":0,"k":[-176,0]},"a":{"a":0,"k":[0,0]},"s":{"a":1,"k":[{"t":0,"s":[100,2]},{"t":28,"s":[100,106]},{"t":38,"s":[100,100]},{"t":145,"s":[100,100]},{"t":176,"s":[100,2]}]},"r":{"a":0,"k":0},"o":{"a":0,"k":100}}]},
{"ty":"gr","nm":"bar2","it":[{"ty":"rc","p":{"a":0,"k":[0,-50]},"s":{"a":0,"k":[40,100]},"r":{"a":0,"k":6}},{"ty":"fl","c":{"a":0,"k":[0.3,0.69,0.31,1]},"o":{"a":0,"k":85}},{"ty":"tr","p":{"a":0,"k":[-88,0]},"a":{"a":0,"k":[0,0]},"s":{"a":1,"k":[{"t":6,"s":[100,2]},{"t":34,"s":[100,106]},{"t":44,"s":[100,100]},{"t":148,"s":[100,100]},{"t":178,"s":[100,2]}]},"r":{"a":0,"k":0},"o":{"a":0,"k":100}}]},
{"ty":"gr","nm":"bar3","it":[{"ty":"rc","p":{"a":0,"k":[0,-70]},"s":{"a":0,"k":[44,140]},"r":{"a":0,"k":6}},{"ty":"fl","c":{"a":0,"k":[1,0.84,0,1]},"o":{"a":0,"k":90}},{"ty":"tr","p":{"a":0,"k":[0,0]},"a":{"a":0,"k":[0,0]},"s":{"a":1,"k":[{"t":12,"s":[100,2]},{"t":40,"s":[100,106]},{"t":50,"s":[100,100]},{"t":150,"s":[100,100]},{"t":179,"s":[100,2]}]},"r":{"a":0,"k":0},"o":{"a":0,"k":100}}]},
{"ty":"gr","nm":"bar4","it":[{"ty":"rc","p":{"a":0,"k":[0,-55]},"s":{"a":0,"k":[40,110]},"r":{"a":0,"k":6}},{"ty":"fl","c":{"a":0,"k":[0.3,0.69,0.31,1]},"o":{"a":0,"k":85}},{"ty":"tr","p":{"a":0,"k":[88,0]},"a":{"a":0,"k":[0,0]},"s":{"a":1,"k":[{"t":18,"s":[100,2]},{"t":46,"s":[100,106]},{"t":56,"s":[100,100]},{"t":152,"s":[100,100]},{"t":179,"s":[100,2]}]},"r":{"a":0,"k":0},"o":{"a":0,"k":100}}]},
{"ty":"gr","nm":"bar5","it":[{"ty":"rc","p":{"a":0,"k":[0,-80]},"s":{"a":0,"k":[40,160]},"r":{"a":0,"k":6}},{"ty":"fl","c":{"a":0,"k":[0.3,0.69,0.31,1]},"o":{"a":0,"k":85}},{"ty":"tr","p":{"a":0,"k":[176,0]},"a":{"a":0,"k":[0,0]},"s":{"a":1,"k":[{"t":24,"s":[100,2]},{"t":52,"s":[100,106]},{"t":62,"s":[100,100]},{"t":154,"s":[100,100]},{"t":179,"s":[100,2]}]},"r":{"a":0,"k":0},"o":{"a":0,"k":100}}]}
]}
]}
""".trimIndent()
}

@Composable
fun CanvasBull(modifier: Modifier = Modifier) {
    val transition = rememberInfiniteTransition(label = "bull_canvas")
    val phase by transition.animateFloat(
        0f,
        1f,
        infiniteRepeatable(tween(3000, easing = LinearEasing), RepeatMode.Restart),
        label = "bull_phase"
    )
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val s = min(w, h) / 512f
        val twoPi = 2f * PI.toFloat()
        val tt = phase * twoPi
        val cx = w / 2f
        val headCy = h * 0.34f + sin(tt) * 4f * s

        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    Color(0xFFFFD700).copy(alpha = 0.30f + 0.15f * ((sin(tt * 2f) + 1f) / 2f)),
                    Color.Transparent
                ),
                center = Offset(cx, headCy),
                radius = 180f * s
            ),
            center = Offset(cx, headCy),
            radius = 180f * s
        )

        for (i in 0 until 5) {
            val grow = (sin(tt + i * 0.9f) + 1f) / 2f
            val bh = (60f + i * 25f) * s * (0.35f + 0.65f * grow)
            val bx = cx + (i - 2) * 88f * s
            val color = if (i == 2) Color(0xFFFFD700) else Color(0xFF4CAF50)
            drawRoundRect(
                color = color.copy(alpha = 0.8f),
                topLeft = Offset(bx - 20f * s, h * 0.92f - bh),
                size = Size(40f * s, bh),
                cornerRadius = CornerRadius(6f * s)
            )
        }

        val chart = Path().apply {
            moveTo(cx - 210f * s, h * 0.80f)
            lineTo(cx - 130f * s, h * 0.74f)
            lineTo(cx - 60f * s, h * 0.77f)
            lineTo(cx + 30f * s, h * 0.66f)
            lineTo(cx + 110f * s, h * 0.70f)
            lineTo(cx + 210f * s, h * 0.60f)
        }
        drawPath(chart, Color(0xFFFFD700).copy(alpha = 0.8f), style = Stroke(5f * s, cap = StrokeCap.Round))

        rotate(3f * sin(tt), pivot = Offset(cx, headCy - 50f * s)) {
            for (sign in listOf(-1f, 1f)) {
                val horn = Path().apply {
                    moveTo(cx + sign * 48f * s, headCy - 58f * s)
                    quadraticTo(cx + sign * 130f * s, headCy - 120f * s, cx + sign * 118f * s, headCy - 132f * s)
                    quadraticTo(cx + sign * 95f * s, headCy - 70f * s, cx + sign * 84f * s, headCy - 38f * s)
                    close()
                }
                drawPath(horn, Color(0xFFFFD700))
            }
        }

        drawOval(Color(0xFF2A0808), topLeft = Offset(cx - 119f * s, headCy - 41f * s), size = Size(42f * s, 26f * s))
        drawOval(Color(0xFF2A0808), topLeft = Offset(cx + 77f * s, headCy - 41f * s), size = Size(42f * s, 26f * s))

        drawOval(Color(0xFF1A0505), topLeft = Offset(cx - 95f * s, headCy - 95f * s), size = Size(190f * s, 172f * s))
        drawOval(Color(0xFFFFD700).copy(alpha = 0.55f), topLeft = Offset(cx - 95f * s, headCy - 95f * s), size = Size(190f * s, 172f * s), style = Stroke(4f * s))

        val breathe = 1f + 0.03f * sin(tt * 2f)
        drawOval(Color(0xFF6B241C), topLeft = Offset(cx - 60f * s * breathe, headCy + 13f * s), size = Size(120f * s * breathe, 84f * s * breathe))
        drawOval(Color(0xFF40100C), topLeft = Offset(cx - 29f * s, headCy + 42f * s), size = Size(14f * s, 20f * s))
        drawOval(Color(0xFF40100C), topLeft = Offset(cx + 15f * s, headCy + 42f * s), size = Size(14f * s, 20f * s))

        val blink = if (phase in 0.45f..0.50f) 0.15f else 1f
        scale(1f, blink, pivot = Offset(cx, headCy - 25f * s)) {
            drawCircle(Color.White, 13f * s, Offset(cx - 45f * s, headCy - 25f * s))
            drawCircle(Color.White, 13f * s, Offset(cx + 45f * s, headCy - 25f * s))
            drawCircle(Color(0xFF1A0505), 5.5f * s, Offset(cx - 45f * s, headCy - 25f * s))
            drawCircle(Color(0xFF1A0505), 5.5f * s, Offset(cx + 45f * s, headCy - 25f * s))
        }

        rotate(8f * sin(tt * 2f), pivot = Offset(cx, headCy + 66f * s)) {
            drawCircle(Color(0xFFFFD700), 22f * s, Offset(cx, headCy + 88f * s), style = Stroke(6f * s))
        }
    }
}