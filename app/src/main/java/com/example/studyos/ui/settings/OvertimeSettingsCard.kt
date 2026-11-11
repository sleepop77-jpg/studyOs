package com.example.studyos.ui.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.studyos.core.OvertimeSettings

@Composable
fun OvertimeSettingsCard() {
    val context = LocalContext.current
    val selectedMinutes by OvertimeSettings.minutes.collectAsState()

    var customMinutes by remember(selectedMinutes) {
        mutableIntStateOf(selectedMinutes)
    }

    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White.copy(alpha = 0.08f)
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                "OVERTIME TIMER",
                fontWeight = FontWeight.Black,
                fontSize = 11.sp,
                color = Color(0xFF69F0AE),
                letterSpacing = 1.5.sp
            )

            Text(
                "After your original focus block, StudyMore automatically starts repeating overtime blocks until you reset the session.",
                color = Color.White.copy(alpha = 0.7f),
                fontSize = 12.sp,
                lineHeight = 18.sp
            )

            Text(
                "QUICK INTERVALS",
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 11.sp
            )

            OvertimePresetRow(
                values = listOf(10, 15, 20, 25),
                selected = selectedMinutes,
                onSelect = {
                    customMinutes = it
                    OvertimeSettings.setMinutes(context, it)
                }
            )

            OvertimePresetRow(
                values = listOf(30, 45, 60),
                selected = selectedMinutes,
                onSelect = {
                    customMinutes = it
                    OvertimeSettings.setMinutes(context, it)
                }
            )

            Spacer(Modifier.height(2.dp))

            Text(
                "CUSTOM INTERVAL",
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 11.sp
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Button(
                    onClick = {
                        customMinutes = (customMinutes - 5).coerceIn(1, 180)
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF1A0505),
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("-5", fontWeight = FontWeight.Black)
                }

                Text(
                    "$customMinutes min",
                    color = Color.White,
                    fontWeight = FontWeight.Black,
                    fontSize = 16.sp,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center
                )

                Button(
                    onClick = {
                        customMinutes = (customMinutes + 5).coerceIn(1, 180)
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF1A0505),
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("+5", fontWeight = FontWeight.Black)
                }
            }

            Slider(
                value = customMinutes.toFloat(),
                onValueChange = {
                    customMinutes = it.toInt().coerceIn(1, 180)
                },
                valueRange = 1f..180f
            )

            Button(
                onClick = {
                    OvertimeSettings.setMinutes(context, customMinutes)
                },
                enabled = customMinutes != selectedMinutes,
                modifier = Modifier.fillMaxWidth().height(42.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFD9534F),
                    contentColor = Color.White
                )
            ) {
                Text(
                    "SAVE CUSTOM INTERVAL",
                    fontWeight = FontWeight.Black,
                    fontSize = 12.sp
                )
            }

            Text(
                "Default: 25 minutes. Custom range: 1–180 minutes. Changes apply to the next session; an active session keeps its selected interval.",
                color = Color.White.copy(alpha = 0.55f),
                fontSize = 11.sp,
                lineHeight = 16.sp
            )
        }
    }
}

@Composable
private fun OvertimePresetRow(
    values: List<Int>,
    selected: Int,
    onSelect: (Int) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(7.dp)
    ) {
        values.forEach { value ->
            Surface(
                onClick = { onSelect(value) },
                shape = RoundedCornerShape(10.dp),
                color = if (selected == value) {
                    Color(0xFFD9534F)
                } else {
                    Color.White.copy(alpha = 0.08f)
                },
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier.padding(vertical = 11.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "${value}m",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )
                }
            }
        }
    }
}
