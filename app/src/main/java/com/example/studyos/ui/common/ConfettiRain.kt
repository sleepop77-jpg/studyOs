package com.example.studyos.ui.common

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import kotlin.math.sin
import kotlin.random.Random

data class ConfettiParticle(
    val x: Float,
    val y: Float,
    val speed: Float,
    val color: Long,
    val size: Float,
    val sway: Float,
    val rotationSpeed: Float
)

@Composable
fun ConfettiRain(colors: List<Long>, active: Boolean) {
    if (!active) return
    val particles = remember {
        List(80) {
            ConfettiParticle(
                x = Random.nextFloat(),
                y = Random.nextFloat() * -1f,
                speed = 0.8f + Random.nextFloat() * 1.5f,
                color = colors.random(),
                size = 4f + Random.nextFloat() * 6f,
                sway = (Random.nextFloat() - 0.5f) * 3f,
                rotationSpeed = (Random.nextFloat() - 0.5f) * 10f
            )
        }
    }
    
    var offsetY by remember { mutableFloatStateOf(0f) }
    var time by remember { mutableFloatStateOf(0f) }
    
    LaunchedEffect(Unit) {
        while (true) {
            offsetY += 1f
            time += 0.05f
            delay(16)
        }
    }
    
    Canvas(modifier = Modifier.fillMaxSize()) {
        val w = size.width
        val h = size.height
        particles.forEach { p ->
            val yPos = ((p.y * h + offsetY * p.speed * 4f) % (h + 100f))
            val xPos = p.x * w + sin(time * p.sway) * 40f
            val rot = time * p.rotationSpeed * 50f
            
            rotate(rot, pivot = Offset(xPos, yPos)) {
                drawRect(
                    color = Color(p.color),
                    topLeft = Offset(xPos, yPos),
                    size = Size(p.size.dp.toPx(), p.size.dp.toPx() * 1.5f)
                )
            }
        }
    }
}
