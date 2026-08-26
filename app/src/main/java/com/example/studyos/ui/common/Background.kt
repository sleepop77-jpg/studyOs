package com.example.studyos.ui.common

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
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
        "theme_crimson_focus" -> Brush.verticalGradient(
            listOf(Color(0xFF000000), Color(0xFF140505), Color(0xFF1D0505))
        )
        else -> Brush.verticalGradient(
            listOf(Color(0xFF000000), Color(0xFF0A0A0A), Color(0xFF000000))
        )
    }
}

@Composable
fun RedPatchesBackground() {
    val theme by Store.equippedTheme.collectAsState(initial = null)
    val isCrimson = theme == "theme_crimson_focus"

    val c1 = if (isCrimson) Color(0xFFC41C3B) else Color(0xFFFFFFFF)
    val c2 = if (isCrimson) Color(0xFF8B0000) else Color(0xFF9A9A9A)
    val a1 = if (isCrimson) 0.15f else 0.05f
    val a2 = if (isCrimson) 0.12f else 0.04f

    Canvas(modifier = Modifier.fillMaxSize()) {
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(c1.copy(alpha = a1), Color.Transparent),
                center = Offset(size.width * 0.85f, size.height * 0.12f),
                radius = size.width * 0.55f
            ),
            center = Offset(size.width * 0.85f, size.height * 0.12f),
            radius = size.width * 0.55f
        )
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(c2.copy(alpha = a2), Color.Transparent),
                center = Offset(size.width * 0.12f, size.height * 0.88f),
                radius = size.width * 0.65f
            ),
            center = Offset(size.width * 0.12f, size.height * 0.88f),
            radius = size.width * 0.65f
        )
    }
}