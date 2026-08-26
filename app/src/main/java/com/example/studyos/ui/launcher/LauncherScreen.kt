package com.example.studyos.ui.launcher

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
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
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.studyos.core.Admin
import com.example.studyos.core.Economy
import com.example.studyos.core.Timer
import com.example.studyos.ui.common.AnimatedBackground
import com.example.studyos.ui.common.RedAura
import com.example.studyos.ui.common.RedPatchesBackground
import com.example.studyos.ui.common.SIcons
import com.example.studyos.ui.common.homeBrush

@Composable
fun LauncherScreen(nav: (String) -> Unit) {
    val context = LocalContext.current
    val fame by Economy.fame.collectAsState()
    val shame by Economy.shame.collectAsState()
    val streak by Economy.streak.collectAsState()
    val running by Timer.running.collectAsState()
    val isAdmin by Admin.enabled.collectAsState()
    val bgBrush = homeBrush()

    remember { DoodleStore.init(context); true }

    Box(modifier = Modifier.fillMaxSize().background(bgBrush)) {
        AnimatedBackground()
        RedPatchesBackground()
        RedAura()

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 32.dp, bottom = 104.dp, start = 20.dp, end = 20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("StudyOS", color = Color.White, fontWeight = FontWeight.Black, fontSize = 24.sp, letterSpacing = 0.5.sp)
                    Text("Focus Economy", color = Color.White.copy(alpha = 0.6f), fontWeight = FontWeight.Medium, fontSize = 11.sp, letterSpacing = 1.sp)
                }

                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    if (isAdmin) {
                        Box(
                            modifier = Modifier.clip(RoundedCornerShape(8.dp)).background(Color(0xFFC41C3B).copy(alpha = 0.9f)).padding(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Text("ADMIN", color = Color.White, fontWeight = FontWeight.Black, fontSize = 9.sp, letterSpacing = 1.2.sp)
                        }
                    }

                    IconButton(
                        onClick = { nav("settings") },
                        modifier = Modifier.size(38.dp).clip(CircleShape).background(Color.White)
                    ) {
                        Icon(SIcons.Gear, contentDescription = "Settings", tint = Color.Black, modifier = Modifier.size(18.dp))
                    }
                }
            }

            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.06f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().clickable { nav("pomodoro") }.padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(SIcons.Star, contentDescription = null, tint = Color(0xFFFFD700), modifier = Modifier.size(22.dp))
                        Spacer(Modifier.width(8.dp))
                        Column {
                            Text("$fame", fontWeight = FontWeight.Black, fontSize = 20.sp, color = Color.White)
                            Text("FAME", fontWeight = FontWeight.Bold, fontSize = 9.sp, color = Color.White.copy(alpha = 0.6f), letterSpacing = 1.2.sp)
                        }
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("${streak}d", fontWeight = FontWeight.Black, fontSize = 20.sp, color = Color(0xFFFFD180))
                        Text("STREAK", fontWeight = FontWeight.Bold, fontSize = 9.sp, color = Color.White.copy(alpha = 0.6f), letterSpacing = 1.2.sp)
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text("$shame", fontWeight = FontWeight.Black, fontSize = 20.sp, color = Color(0xFFFFD4D4))
                        Text("SHAME", fontWeight = FontWeight.Bold, fontSize = 9.sp, color = Color.White.copy(alpha = 0.6f), letterSpacing = 1.2.sp)
                    }
                }
            }

            Box(
                modifier = Modifier.weight(1f).fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                MascotDoodleEditor(running = running)
            }
        }

        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 20.dp)
        ) {
            Surface(
                shape = RoundedCornerShape(26.dp),
                color = Color.White.copy(alpha = 0.07f)
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(14.dp),
                    modifier = Modifier.padding(horizontal = 18.dp, vertical = 12.dp)
                ) {
                    DockIcon(SIcons.Timer, "Timer") { nav("pomodoro") }
                    DockIcon(SIcons.Lock, "Blocker") { nav("lockdown") }
                    DockIcon(SIcons.Star, "Stocks") { nav("stocks") }
                    DockIcon(SIcons.Bag, "Store") { nav("store") }
                }
            }
        }
    }
}

@Composable
private fun MascotDoodleEditor(running: Boolean) {
    var editMode by remember { mutableStateOf(false) }
    var color by remember { mutableStateOf(Color(0xFFFFD700)) }
    val palette = listOf(
        Color.White,
        Color(0xFFFFD700),
        Color(0xFFFF5252),
        Color(0xFF4CAF50),
        Color(0xFF20B2AA),
        Color(0xFF9C27B0),
        Color(0xFFFF9800),
        Color(0xFF00BCD4)
    )

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Box(modifier = Modifier.size(240.dp)) {
            InteractiveMascot(
                state = if (running) MascotState.STUDYING else MascotState.IDLE,
                size = 240.dp,
                showArc = true,
                progressArc = 0.85f
            )
            DoodleCanvas(
                enabled = editMode,
                color = color,
                strokeWidth = 4f,
                modifier = Modifier.fillMaxSize()
            )
        }

        Text(
            if (running) "Deep Focus Active" else "Tap Mascot for Motivation",
            color = Color.White, fontWeight = FontWeight.Bold, fontSize = 15.sp, letterSpacing = 0.3.sp
        )

        Row(
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                onClick = { editMode = !editMode },
                shape = RoundedCornerShape(50.dp),
                color = if (editMode) Color(0xFF4CAF50) else Color.White.copy(alpha = 0.12f)
            ) {
                Text(
                    if (editMode) "Done" else "Edit Art",
                    color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
            }
            if (editMode) {
                Surface(
                    onClick = { DoodleStore.clear() },
                    shape = RoundedCornerShape(50.dp),
                    color = Color(0xFFD9534F)
                ) {
                    Text(
                        "Clear",
                        color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                    )
                }
            }
        }

        if (editMode) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp).horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                palette.forEach { c ->
                    val selected = color == c
                    Box(
                        modifier = Modifier
                            .size(if (selected) 34.dp else 28.dp)
                            .clip(CircleShape)
                            .background(c)
                            .clickable { color = c }
                    )
                }
            }
        }
    }
}

@Composable
private fun DockIcon(icon: ImageVector, label: String, onClick: () -> Unit) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.82f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMedium),
        label = "dockScale"
    )

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .scale(scale)
                .clip(RoundedCornerShape(15.dp))
                .background(Color.Black)
                .clickable(interactionSource = interactionSource, indication = null) { onClick() },
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = label, tint = Color.White, modifier = Modifier.size(22.dp))
        }
        Text(
            label,
            color = Color.White,
            fontSize = 10.sp,
            fontWeight = FontWeight.Medium,
            letterSpacing = 0.3.sp
        )
    }
}