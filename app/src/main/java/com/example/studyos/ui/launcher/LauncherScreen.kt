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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.studyos.core.Admin
import com.example.studyos.core.Economy
import com.example.studyos.core.Store
import com.example.studyos.core.Timer
import com.example.studyos.ui.common.SIcons
import com.example.studyos.ui.launcher.AnimatedSkins
import com.example.studyos.ui.theme.FameGold
import com.example.studyos.ui.theme.PrimaryCoral
import com.example.studyos.ui.theme.PrimaryNightMaroon
import com.example.studyos.ui.theme.WarningRed

@Composable
fun LauncherScreen(nav: (String) -> Unit) {
    val fame by Economy.fame.collectAsState()
    val shame by Economy.shame.collectAsState()
    val streak by Economy.streak.collectAsState()
    val running by Timer.running.collectAsState()
    val continuous by Economy.continuousSecs.collectAsState()
    val equippedTheme by Store.equippedTheme.collectAsState(initial = null)
    val isAdmin by Admin.enabled.collectAsState()
    val state = when {
        running && continuous >= 10800 -> MascotState.BURNING
        running -> MascotState.STUDYING
        shame > fame && shame > 50 -> MascotState.FRUSTRATED
        else -> MascotState.IDLE
    }
    val backgroundBrush = when (equippedTheme) {
        AnimatedSkins.AURORA -> Brush.verticalGradient(listOf(Color(0xFF0B1026), Color(0xFF1E6E5A), Color(0xFF0B1026)))
        "item_math_matrix" -> Brush.verticalGradient(listOf(Color(0xFF003000), Color(0xFF001500)))
        "item_spanish_fiesta" -> Brush.verticalGradient(listOf(Color(0xFFF5A623), Color(0xFFD9534F), Color(0xFF9C27B0)))
        else -> Brush.verticalGradient(listOf(Color(0xFFD9534F), Color(0xFFC94440)))
    }
    Column(
        modifier = Modifier.fillMaxSize().background(backgroundBrush).padding(top = 24.dp, bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 18.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("StudyOS", color = Color.White, fontWeight = FontWeight.Black, fontSize = 20.sp, letterSpacing = 1.sp)
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (isAdmin) {
                    Text("ADMIN", color = WarningRed, fontWeight = FontWeight.Black, fontSize = 10.sp, modifier = Modifier.padding(end = 8.dp))
                }
                IconButton(onClick = { nav("settings") }, modifier = Modifier.size(38.dp).clip(CircleShape).background(Color.White.copy(alpha = 0.22f))) {
                    Icon(SIcons.Gear, contentDescription = "Settings", tint = Color.White, modifier = Modifier.size(19.dp))
                }
            }
        }
        Box(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)
                .clip(RoundedCornerShape(18.dp)).background(Color.White.copy(alpha = 0.2f))
                .clickable { nav("pomodoro") }.padding(horizontal = 16.dp, vertical = 10.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(SIcons.Star, contentDescription = null, tint = FameGold, modifier = Modifier.size(18.dp))
                    Text("$fame", color = Color.White, fontWeight = FontWeight.Black, fontSize = 16.sp, modifier = Modifier.padding(start = 4.dp))
                    Text(" FAME", color = Color.White.copy(alpha = 0.7f), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }
                Text("${streak}d streak", color = Color(0xFFFFD180), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                Text("$shame shame", color = Color(0xFFFFD4D4), fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
        }
        Column(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)
                .clip(RoundedCornerShape(24.dp)).background(Color.White.copy(alpha = 0.18f)).padding(18.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            InteractiveMascot(state = state, size = 120.dp, showArc = true, progressArc = 0.85f)
            Spacer(Modifier.height(8.dp))
            Text(
                when (state) {
                    MascotState.BURNING -> "BURNING OVERDRIVE"
                    MascotState.STUDYING -> "Deep Focus Active"
                    MascotState.FRUSTRATED -> "Study now to kill Shame"
                    else -> "Tap mascot for motivation"
                },
                color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp
            )
            Text("+2 Fame/min in active sessions", color = FameGold, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
        }
        Text("STUDYOS APPLICATIONS", color = Color.White.copy(alpha = 0.9f), fontWeight = FontWeight.Bold, fontSize = 12.sp, letterSpacing = 1.2.sp, modifier = Modifier.padding(horizontal = 18.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceAround) {
            AppTile(SIcons.Timer, "Timer", Color(0xFFD9534F)) { nav("pomodoro") }
            AppTile(SIcons.Lock, "Blocker", Color(0xFF4A2C2C)) { nav("lockdown") }
            AppTile(SIcons.Bag, "Store", Color(0xFFFFD700)) { nav("store") }
        }
        Spacer(Modifier.weight(1f))
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
