package com.example.studyos.ui.common

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import com.example.studyos.core.Store

@Composable
fun homeBrush(): Brush {
    val theme by Store.equippedTheme.collectAsState(initial = null)
    return when (theme) {
        "item_aurora_dream" -> Brush.verticalGradient(listOf(Color(0xFF0B1026), Color(0xFF1E6E5A), Color(0xFF0B1026)))
        "item_math_matrix" -> Brush.verticalGradient(listOf(Color(0xFF003000), Color(0xFF001500)))
        "item_spanish_fiesta" -> Brush.verticalGradient(listOf(Color(0xFFF5A623), Color(0xFFD9534F), Color(0xFF9C27B0)))
        else -> Brush.verticalGradient(listOf(Color(0xFFD9534F), Color(0xFFC94440)))
    }
}
