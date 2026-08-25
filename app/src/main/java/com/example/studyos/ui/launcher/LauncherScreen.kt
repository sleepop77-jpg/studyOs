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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
    Column(
        modifier = Modifier.fillMaxSize().background(bgBrush).padding(top = 24.dp, bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 18.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("StudyOS", color = Color.White, fontWeight = FontWeight.Black, fontSize = 20.sp, letterSpacing = 1.sp)
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                if (isAdmin) Text("ADMIN", color = Color(0xFFC41C3B), fontWeight = FontWeight.Black, fontSize = 10.sp)
                IconButton(
                    onClick = { nav("settings") },
                    modifier = Modifier.size(38.dp).clip(CircleShape).background(Color.White.copy(alpha = 0.22f))
                ) {
                    Icon(SIcons.Gear, contentDescription = "Settings", tint = Color.White, modifier = Modifier.size(19.dp))
                }
            }
        }
        Box(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)
                .clip(RoundedCornerShape(18.dp)).background(Color.White.copy(alpha = 0.2f))
                .clickable { nav("pomodoro") }.padding(horizontal = 16.dp, vertical = 10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(SIcons.Star, contentDescription = null, tint = Color(0xFFFFD700), modifier = Modifier.size(20.dp))
                    Text("$fame", fontWeight = FontWeight.Black, fontSize = 18.sp, color = Color.White, modifier = Modifier.padding(start = 4.dp))
                    Text(" FAME", fontWeight = FontWeight.Bold, fontSize = 10.sp, color = Color.White.copy(alpha = 0.75f))
                }
                Text("${streak}d streak", color = Color(0xFFFFD180), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                Text("$shame shame", color = Color(0xFFFFD4D4), fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
        }
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.18f)),
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp).shimmer(shimmer)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth().padding(18.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                InteractiveMascot(
                    state = if (running) MascotState.STUDYING else MascotState.IDLE,
                    size = 120.dp,
                    showArc = true,
                    progressArc = 0.85f
                )
                Text(
                    if (running) "Deep Focus Active" else "Tap mascot for motivation",
                    color = Color.White, fontWeight = FontWeight.Bold, fontSize = 15.sp
                )
                Text("+2 Fame/min in active sessions", color = Color(0xFFFFD700), fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
            }
        }
        Text(
            "STUDYOS APPLICATIONS",
            color = Color.White.copy(alpha = 0.9f), fontWeight = FontWeight.Bold, fontSize = 12.sp, letterSpacing = 1.2.sp,
            modifier = Modifier.padding(horizontal = 18.dp)
        )
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceAround) {
            AppTile(SIcons.Timer, "Timer", Color(0xFFD9534F)) { nav("pomodoro") }
            AppTile(SIcons.Lock, "Blocker", Color(0xFF4A2C2C)) { nav("lockdown") }
            AppTile(SIcons.Bag, "Store", Color(0xFFFFD700)) { nav("store") }
        }
        Spacer(Modifier.height(8.dp))
    }
}

@Composable
private fun AppTile(icon: ImageVector, label: String, color: Color, onClick: () -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.clickable(onClick = onClick).padding(4.dp)) {
        Box(modifier = Modifier.size(60.dp).clip(RoundedCornerShape(16.dp)).background(color), contentAlignment = Alignment.Center) {
            Icon(icon, contentDescription = label, tint = Color.White, modifier = Modifier.size(30.dp))
        }
        Text(label, color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(top = 6.dp))
    }
}
