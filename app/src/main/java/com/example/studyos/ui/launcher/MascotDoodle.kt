package com.example.studyos.ui.launcher

import android.content.Context
import android.content.SharedPreferences
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.flow.MutableStateFlow

data class DoodleStroke(val color: Long, val width: Float, val points: List<Offset>)

object DoodleStore {
    val strokes = MutableStateFlow<List<DoodleStroke>>(emptyList())
    private lateinit var prefs: SharedPreferences

    fun init(context: Context) {
        if (::prefs.isInitialized) return
        prefs = context.applicationContext.getSharedPreferences("studyos_doodle", Context.MODE_PRIVATE)
        strokes.value = decode(prefs.getString("strokes", "") ?: "")
    }

    fun addStroke(s: DoodleStroke) {
        strokes.value = (strokes.value + s).takeLast(60)
        save()
    }

    fun clear() {
        strokes.value = emptyList()
        save()
    }

    private fun save() {
        if (!::prefs.isInitialized) return
        prefs.edit().putString("strokes", encode(strokes.value)).apply()
    }

    private fun encode(list: List<DoodleStroke>): String =
        list.joinToString("\n") { s ->
            "${s.color};${s.width};" + s.points.joinToString("|") { "${it.x},${it.y}" }
        }

    private fun decode(raw: String): List<DoodleStroke> {
        if (raw.isBlank()) return emptyList()
        return raw.split("\n").mapNotNull { line ->
            val parts = line.split(";")
            if (parts.size < 3) return@mapNotNull null
            val color = parts[0].toLongOrNull() ?: return@mapNotNull null
            val width = parts[1].toFloatOrNull() ?: return@mapNotNull null
            val pts = parts[2].split("|").mapNotNull { p ->
                val xy = p.split(",")
                if (xy.size == 2) {
                    val x = xy[0].toFloatOrNull() ?: return@mapNotNull null
                    val y = xy[1].toFloatOrNull() ?: return@mapNotNull null
                    Offset(x, y)
                } else null
            }
            if (pts.size < 2) null else DoodleStroke(color, width, pts)
        }
    }
}

@Composable
fun DoodleCanvas(
    enabled: Boolean,
    color: Color,
    strokeWidth: Float,
    modifier: Modifier = Modifier
) {
    val strokes by DoodleStore.strokes.collectAsState()
    val current = remember { mutableStateOf<List<Offset>>(emptyList()) }
    var brushPos by remember { mutableStateOf<Offset?>(null) }

    Canvas(
        modifier = modifier.then(
            if (enabled) {
                Modifier.pointerInput(color, strokeWidth) {
                    detectDragGestures(
                        onDragStart = { offset -> 
                            current.value = listOf(offset)
                            brushPos = offset
                        },
                        onDrag = { change, _ -> 
                            current.value = current.value + change.position
                            brushPos = change.position
                        },
                        onDragEnd = {
                            val pts = current.value
                            if (pts.size > 1) {
                                DoodleStore.addStroke(DoodleStroke(color.toArgb().toLong(), strokeWidth, pts))
                            }
                            current.value = emptyList()
                            brushPos = null
                        },
                        onDragCancel = { 
                            current.value = emptyList()
                            brushPos = null
                        }
                    )
                }
            } else Modifier
        )
    ) {
        fun drawStroke(s: DoodleStroke) {
            val pts = s.points
            if (pts.size < 2) return
            val path = Path().apply {
                moveTo(pts[0].x, pts[0].y)
                for (i in 1 until pts.size - 1) {
                    val mid = Offset((pts[i].x + pts[i + 1].x) / 2f, (pts[i].y + pts[i + 1].y) / 2f)
                    quadraticBezierTo(pts[i].x, pts[i].y, mid.x, mid.y)
                }
                lineTo(pts.last().x, pts.last().y)
            }
            drawPath(
                path,
                Color(s.color.toInt()),
                style = Stroke(s.width.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
            )
        }

        strokes.forEach { drawStroke(it) }
        val cur = current.value
        if (cur.size > 1) drawStroke(DoodleStroke(color.toArgb().toLong(), strokeWidth, cur))
        
        // Draw paintbrush cursor
        brushPos?.let { pos ->
            val brushSize = 24.dp.toPx()
            val handleLength = 32.dp.toPx()
            
            // Brush tip (circle)
            drawCircle(
                color = color,
                radius = brushSize / 2f,
                center = pos
            )
            
            // Brush handle (line going up-right)
            drawLine(
                color = Color(0xFF8B4513),
                start = pos,
                end = Offset(pos.x + handleLength * 0.7f, pos.y - handleLength * 0.7f),
                strokeWidth = 6.dp.toPx(),
                cap = StrokeCap.Round
            )
            
            // Brush ferrule (metal part)
            drawCircle(
                color = Color(0xFFC0C0C0),
                radius = 4.dp.toPx(),
                center = pos
            )
        }
    }
}