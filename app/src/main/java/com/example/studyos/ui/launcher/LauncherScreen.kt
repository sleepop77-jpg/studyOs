package com.example.studyos.ui.launcher

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
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
import com.example.studyos.ui.common.customShimmer
import com.example.studyos.ui.common.homeBrush

@Composable
fun LauncherScreen(nav: (String) -> Unit) {
    val fame by Economy.fame.collectAsState()
    val shame by Economy.shame.collectAsState()
    val streak by Economy.streak.collectAsState()
    val running by Timer.running.collectAsState()
    val isAdmin by Admin.enabled.collectAsState()
    val bgBrush = homeBrush()

    Box(modifier = Modifier.fillMaxSize().background(bgBrush)) {
        AnimatedBackground()
        RedPatchesBackground()
        RedAura()

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 32.dp, bottom = 120.dp, start = 20.dp, end = 20.dp),
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

            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.06f)),
                modifier = Modifier.fillMaxWidth().customShimmer()
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    InteractiveMascot(
                        state = if (running) MascotState.STUDYING else MascotState.IDLE,
                        size = 140.dp,
                        showArc = true,
                        progressArc = 0.85f
                    )
                    Text(
                        if (running) "Deep Focus Active" else "Tap Mascot for Motivation",
                        color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp, letterSpacing = 0.3.sp
                    )
                    Box(
                        modifier = Modifier.clip(RoundedCornerShape(12.dp)).background(Color(0xFFFFD700).copy(alpha = 0.15f)).padding(horizontal = 14.dp, vertical = 8.dp)
                    ) {
                        Text("+2 Fame/min in Active Sessions", color = Color(0xFFFFD700), fontSize = 12.sp, fontWeight = FontWeight.SemiBold, letterSpacing = 0.5.sp)
                    }
                }
            }

            Spacer(Modifier.height(8.dp))
        }

        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 24.dp)
        ) {
            Surface(
                shape = RoundedCornerShape(32.dp),
                color = Color.White.copy(alpha = 0.07f)
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(20.dp),
                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 14.dp)
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
                .size(56.dp)
                .scale(scale)
                .clip(RoundedCornerShape(16.dp))
                .background(Color.Black)
                .clickable(interactionSource = interactionSource, indication = null) { onClick() },
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = label, tint = Color.White, modifier = Modifier.size(26.dp))
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