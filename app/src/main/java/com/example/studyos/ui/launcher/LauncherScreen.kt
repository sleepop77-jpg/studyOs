package com.example.studyos.ui.launcher

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Store
import androidx.compose.material.icons.filled.ThumbDown
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Whatshot
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.studyos.core.Admin
import com.example.studyos.core.Economy
import com.example.studyos.core.Timer
import com.example.studyos.ui.common.customShimmer
import com.example.studyos.ui.common.homeBrush

@Composable
fun LauncherScreen(nav: (String) -> Unit) {
    val fame by Economy.fame.collectAsState()
    val shame by Economy.shame.collectAsState()
    val streak by Economy.streak.collectAsState()
    val running by Timer.running.collectAsState()
    val isAdmin by Admin.enabled.collectAsState()
    val seconds by Timer.seconds.collectAsState()
    val total by Timer.total.collectAsState()
    val progressArc = if (total > 0) seconds.toFloat() / total.toFloat() else 1f

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(homeBrush())
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = CircleShape,
                    color = Color.White.copy(alpha = 0.22f),
                    modifier = Modifier.size(44.dp)
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.School,
                            contentDescription = "StudyOS",
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "StudyOS",
                        color = Color.White,
                        fontWeight = FontWeight.Black,
                        fontSize = 22.sp,
                        letterSpacing = 0.5.sp
                    )
                    Text(
                        text = "Focus. Earn Fame. Stay unbothered.",
                        color = Color.White.copy(alpha = 0.72f),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                if (isAdmin) {
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = Color(0xFFC41C3B).copy(alpha = 0.92f)
                    ) {
                        Text(
                            text = "ADMIN",
                            color = Color.White,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 1.sp,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                }

                IconButton(
                    onClick = { nav("settings") },
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.2f))
                ) {
                    Icon(
                        imageVector = Icons.Filled.Settings,
                        contentDescription = "Settings",
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Card(
                shape = RoundedCornerShape(28.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color.White.copy(alpha = 0.16f)
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .customShimmer()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        StatusChip(
                            text = if (running) "FOCUS ACTIVE" else "READY TO LOCK IN",
                            color = if (running) Color(0xFF4CAF50) else Color(0xFFFFD700)
                        )
                        StatusChip(
                            text = "${streak}D STREAK",
                            color = Color(0xFFFFB74D)
                        )
                    }

                    InteractiveMascot(
                        state = if (running) MascotState.STUDYING else MascotState.IDLE,
                        size = 132.dp,
                        showArc = true,
                        progressArc = progressArc
                    )

                    Text(
                        text = if (running) {
                            "Deep focus session in progress"
                        } else {
                            "Tap the mascot and start your session"
                        },
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )

                    Text(
                        text = "+2 Fame/min in active sessions",
                        color = Color(0xFFFFD700),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                StatCard(
                    icon = Icons.Filled.Star,
                    label = "FAME",
                    value = "$fame",
                    tint = Color(0xFFFFD700),
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    icon = Icons.Filled.Whatshot,
                    label = "STREAK",
                    value = "${streak}d",
                    tint = Color(0xFFFFB74D),
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    icon = Icons.Filled.ThumbDown,
                    label = "SHAME",
                    value = "$shame",
                    tint = Color(0xFFFF8A80),
                    modifier = Modifier.weight(1f)
                )
            }

            Text(
                text = "QUICK APPS",
                color = Color.White.copy(alpha = 0.82f),
                fontWeight = FontWeight.Bold,
                fontSize = 11.sp,
                letterSpacing = 1.5.sp,
                modifier = Modifier.padding(start = 2.dp, top = 4.dp)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                ActionTile(
                    icon = Icons.Filled.Timer,
                    title = "Timer",
                    subtitle = "Pomodoro focus",
                    tint = Color(0xFFD9534F),
                    modifier = Modifier.weight(1f),
                    onClick = { nav("pomodoro") }
                )
                ActionTile(
                    icon = Icons.Filled.Lock,
                    title = "Blocker",
                    subtitle = "Seal distractions",
                    tint = Color(0xFF9575CD),
                    modifier = Modifier.weight(1f),
                    onClick = { nav("lockdown") }
                )
                ActionTile(
                    icon = Icons.Filled.Store,
                    title = "Store",
                    subtitle = "Spend Fame",
                    tint = Color(0xFFFFD700),
                    modifier = Modifier.weight(1f),
                    onClick = { nav("store") }
                )
            }

            Spacer(modifier = Modifier.height(10.dp))
        }
    }
}

@Composable
private fun StatusChip(text: String, color: Color) {
    Surface(
        shape = CircleShape,
        color = color.copy(alpha = 0.22f)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(color)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = text,
                color = Color.White,
                fontSize = 10.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 1.sp
            )
        }
    }
}

@Composable
private fun StatCard(
    icon: ImageVector,
    label: String,
    value: String,
    tint: Color,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White.copy(alpha = 0.14f)
        ),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 14.dp, horizontal = 10.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(7.dp)
        ) {
            Surface(
                shape = CircleShape,
                color = tint.copy(alpha = 0.24f),
                modifier = Modifier.size(40.dp)
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = label,
                        tint = tint,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Text(
                text = value,
                color = Color.White,
                fontSize = 20.sp,
                fontWeight = FontWeight.Black
            )

            Text(
                text = label,
                color = Color.White.copy(alpha = 0.72f),
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )
        }
    }
}

@Composable
private fun ActionTile(
    icon: ImageVector,
    title: String,
    subtitle: String,
    tint: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White.copy(alpha = 0.14f)
        ),
        modifier = modifier
            .clickable(onClick = onClick)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = tint.copy(alpha = 0.26f),
                modifier = Modifier.size(46.dp)
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = title,
                        tint = tint,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            Text(
                text = title,
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp
            )

            Text(
                text = subtitle,
                color = Color.White.copy(alpha = 0.66f),
                fontSize = 10.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}
