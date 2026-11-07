package com.example.studyos.ui.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.studyos.core.Timer
import com.example.studyos.core.VisualSettings

@Composable
fun VisualSettingsCard() {
    val context = LocalContext.current
    val motionMode by VisualSettings.motionMode.collectAsState()
    val refresh by VisualSettings.timerRefreshSeconds.collectAsState()
    val brightness by VisualSettings.effectBrightness.collectAsState()

    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White.copy(alpha = 0.08f)
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(13.dp)
        ) {
            Text(
                "MOTION & PERFORMANCE",
                fontWeight = FontWeight.Black,
                fontSize = 11.sp,
                color = Color(0xFFFF8A80),
                letterSpacing = 1.5.sp
            )

            Text(
                "Control animations, timer display updates and visual intensity.",
                color = Color.White.copy(alpha = 0.65f),
                fontSize = 12.sp,
                lineHeight = 18.sp
            )

            Text(
                "ANIMATION MODE",
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 11.sp
            )

            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                listOf(
                    0 to "Full",
                    1 to "Reduced",
                    2 to "Static"
                ).forEach { (value, label) ->
                    Surface(
                        onClick = {
                            VisualSettings.setMotionMode(context, value)
                        },
                        shape = RoundedCornerShape(10.dp),
                        color = if (motionMode == value) {
                            Color(0xFFD9534F)
                        } else {
                            Color.White.copy(alpha = 0.08f)
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Box(
                            modifier = Modifier.padding(
                                horizontal = 4.dp,
                                vertical = 11.dp
                            ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                label,
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp
                            )
                        }
                    }
                }
            }

            Text(
                when (motionMode) {
                    0 -> "Original animations and movement."
                    1 -> "Slower movement, fewer particles and calmer transitions."
                    else -> "No continuous mascot or background movement."
                },
                color = Color.White.copy(alpha = 0.55f),
                fontSize = 11.sp
            )

            Text(
                "CLOCK DISPLAY",
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 11.sp
            )

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf(1 to "Every second", 5 to "Every 5 seconds")
                    .forEach { (value, label) ->
                        Surface(
                            onClick = {
                                VisualSettings.setTimerRefreshSeconds(
                                    context,
                                    value
                                )
                                Timer.refreshDisplay()
                            },
                            shape = RoundedCornerShape(10.dp),
                            color = if (refresh == value) {
                                Color(0xFFD9534F)
                            } else {
                                Color.White.copy(alpha = 0.08f)
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Box(
                                modifier = Modifier.padding(
                                    horizontal = 5.dp,
                                    vertical = 11.dp
                                ),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    label,
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 10.sp
                                )
                            }
                        }
                    }
            }

            Text(
                "The countdown remains accurate every second. Only the displayed clock refreshes at this interval.",
                color = Color.White.copy(alpha = 0.55f),
                fontSize = 11.sp,
                lineHeight = 16.sp
            )

            Text(
                "BACKGROUND EFFECT INTENSITY   ${(brightness * 100).toInt()}%",
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 11.sp
            )

            Slider(
                value = brightness,
                onValueChange = {
                    VisualSettings.updateEffectBrightness(it)
                },
                onValueChangeFinished = {
                    VisualSettings.saveEffectBrightness(context)
                },
                valueRange = 0.25f..1f,
                steps = 14
            )

            Text(
                "Controls the brightness of particles, aura and decorative effects, not your phone's hardware brightness.",
                color = Color.White.copy(alpha = 0.55f),
                fontSize = 11.sp,
                lineHeight = 16.sp
            )
        }
    }
}
