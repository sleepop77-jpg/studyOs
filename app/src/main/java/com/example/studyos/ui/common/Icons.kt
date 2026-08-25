package com.example.studyos.ui.common

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

private fun icon(builder: ImageVector.Builder.() -> Unit): ImageVector =
    ImageVector.Builder(
        name = "ic", defaultWidth = 24.dp, defaultHeight = 24.dp,
        viewportWidth = 24f, viewportHeight = 24f
    ).apply(builder).build()

object SIcons {
    val Back: ImageVector by lazy {
        icon { path(stroke = SolidColor(Color.Black), strokeLineWidth = 2.4f, strokeLineCap = StrokeCap.Round, strokeLineJoin = StrokeJoin.Round) { moveTo(15f, 6f); lineTo(9f, 12f); lineTo(15f, 18f) } }
    }
    val Star: ImageVector by lazy {
        icon { path(fill = SolidColor(Color.Black)) { moveTo(12f, 3f); lineTo(14.6f, 8.6f); lineTo(20.5f, 9.3f); lineTo(16f, 13.4f); lineTo(17.3f, 19.3f); lineTo(12f, 16.2f); lineTo(6.7f, 19.3f); lineTo(8f, 13.4f); lineTo(3.5f, 9.3f); lineTo(9.4f, 8.6f); close() } }
    }
    val Lock: ImageVector by lazy {
        icon {
            path(fill = SolidColor(Color.Black)) { moveTo(5f, 11f); lineTo(19f, 11f); lineTo(19f, 20f); lineTo(5f, 20f); close() }
            path(stroke = SolidColor(Color.Black), strokeLineWidth = 2.2f, strokeLineCap = StrokeCap.Round) { moveTo(8f, 11f); lineTo(8f, 8f); curveTo(8f, 4f, 16f, 4f, 16f, 8f); lineTo(16f, 11f) }
        }
    }
    val Check: ImageVector by lazy {
        icon { path(stroke = SolidColor(Color.Black), strokeLineWidth = 2.6f, strokeLineCap = StrokeCap.Round, strokeLineJoin = StrokeJoin.Round) { moveTo(5f, 13f); lineTo(10f, 18f); lineTo(19f, 7f) } }
    }
    val Bag: ImageVector by lazy {
        icon {
            path(fill = SolidColor(Color.Black)) { moveTo(5f, 8f); lineTo(19f, 8f); lineTo(18f, 20f); lineTo(6f, 20f); close() }
            path(stroke = SolidColor(Color.Black), strokeLineWidth = 2f, strokeLineCap = StrokeCap.Round) { moveTo(9f, 8f); lineTo(9f, 6f); arcTo(androidx.compose.ui.geometry.Rect(9f, 3f, 15f, 9f), 180f, 180f, false); lineTo(15f, 8f) }
        }
    }
    val Timer: ImageVector by lazy {
        icon {
            path(fill = SolidColor(Color.Black)) { moveTo(10f, 2f); lineTo(14f, 2f); lineTo(14f, 4f); lineTo(10f, 4f); close() }
            path(stroke = SolidColor(Color.Black), strokeLineWidth = 2.2f) { moveTo(19f, 13f); arcTo(5f, 6f, 19f, 20f, 0f, 180f, false); arcTo(5f, 6f, 19f, 20f, 180f, 180f, false) }
            path(stroke = SolidColor(Color.Black), strokeLineWidth = 2.2f, strokeLineCap = StrokeCap.Round) { moveTo(12f, 13f); lineTo(12f, 9f) }
        }
    }
    val Gear: ImageVector by lazy {
        icon { path(stroke = SolidColor(Color.Black), strokeLineWidth = 2.2f, strokeLineCap = StrokeCap.Round) { moveTo(4f, 7f); lineTo(20f, 7f); moveTo(4f, 12f); lineTo(20f, 12f); moveTo(4f, 17f); lineTo(20f, 17f) } }
    }
    val Play: ImageVector by lazy {
        icon { path(fill = SolidColor(Color.Black)) { moveTo(8f, 5f); lineTo(19f, 12f); lineTo(8f, 19f); close() } }
    }
    val Pause: ImageVector by lazy {
        icon { path(fill = SolidColor(Color.Black)) { moveTo(7f, 5f); lineTo(10f, 5f); lineTo(10f, 19f); lineTo(7f, 19f); close(); moveTo(14f, 5f); lineTo(17f, 5f); lineTo(17f, 19f); lineTo(14f, 19f); close() } }
    }
}
