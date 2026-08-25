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
import com.example.studyos.core.Store

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
        else -> Brush.verticalGradient(
            listOf(Color(0xFF050505), Color(0xFF0A0404), Color(0xFF140505))
        )
    }
}

@Composable
fun RedPatchesBackground() {
    Canvas(modifier = Modifier.fillMaxSize()) {
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(Color(0xFFC41C3B).copy(alpha = 0.15f), Color.Transparent),
                center = Offset(size.width * 0.85f, size.height * 0.12f),
                radius = size.width * 0.55f
            ),
            center = Offset(size.width * 0.85f, size.height * 0.12f),
            radius = size.width * 0.55f
        )
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(Color(0xFF8B0000).copy(alpha = 0.12f), Color.Transparent),
                center = Offset(size.width * 0.12f, size.height * 0.88f),
                radius = size.width * 0.65f
            ),
            center = Offset(size.width * 0.12f, size.height * 0.88f),
            radius = size.width * 0.65f
        )
    }
}
