package com.example.studyos.ui.pomodoro

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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.studyos.core.Timer
import com.example.studyos.ui.common.SIcons
import com.example.studyos.ui.launcher.InteractiveMascot
import com.example.studyos.ui.launcher.MascotState
import com.example.studyos.ui.theme.FameGold
import com.example.studyos.ui.theme.PrimaryCoral
import com.example.studyos.ui.theme.PrimaryCoralDark

@Composable
fun PomodoroScreen(back: () -> Unit) {
    val seconds by Timer.seconds.collectAsState()
    val total by Timer.total.collectAsState()
    val running by Timer.running.collectAsState()
    val subject by Timer.subject.collectAsState()
    val continuous by com.example.studyos.core.Economy.continuousSecs.collectAsState()
    val isBurning = continuous >= 10800
    val progress = if (total > 0) seconds.toFloat() / total.toFloat() else 1f
    val brush = if (isBurning) Brush.verticalGradient(listOf(Color(0xFF8B1E0F), Color(0xFF240703)))
    else Brush.verticalGradient(listOf(Color(0xFFD9534F), Color(0xFFC94440)))
    Column(
        modifier = Modifier.fillMaxSize().background(brush).verticalScroll(rememberScrollState()).padding(top = 24.dp, bottom = 32.dp, start = 20.dp, end = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = back, modifier = Modifier.size(40.dp).clip(CircleShape).background(Color.White.copy(alpha = 0.2f))) {
                Icon(SIcons.Back, contentDescription = "Back", tint = Color.White)
            }
            Spacer(Modifier.weight(1f))
            Text(subject, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
            Spacer(Modifier.weight(1f))
            Spacer(Modifier.size(40.dp))
        }
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            listOf("Mathematics", "Physics", "Chemistry", "Biology").forEach { s ->
                val sel = subject == s
                Text(
                    s, color = if (sel) PrimaryCoralDark else Color.White,
                    fontWeight = FontWeight.Bold, fontSize = 11.sp,
                    modifier = Modifier.clip(RoundedCornerShape(10.dp))
                        .background(if (sel) Color.White else Color.White.copy(alpha = 0.18f))
                        .clickable { Timer.subject.value = s }
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                )
            }
        }
        Box(modifier = Modifier.size(230.dp), contentAlignment = Alignment.Center) {
            InteractiveMascot(state = if (isBurning) MascotState.BURNING else if (running) MascotState.STUDYING else MascotState.IDLE, size = 210.dp, showArc = true, progressArc = progress)
        }
        Text(String.format("%02d:%02d", seconds / 60, seconds % 60), color = Color.White, fontSize = 52.sp, fontWeight = FontWeight.Black, letterSpacing = 2.sp)
        if (isBurning) {
            Text("3-HOUR BURNING MODE ACTIVE", color = FameGold, fontSize = 11.sp, fontWeight = FontWeight.Black)
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf(15, 25, 45, 60, 90, 180).forEach { m ->
                val sel = total == m * 60
                Text(
                    "${m}m", color = if (sel) PrimaryCoralDark else Color.White,
                    fontSize = 12.sp, fontWeight = FontWeight.Bold,
                    modifier = Modifier.clip(RoundedCornerShape(12.dp))
                        .background(if (sel) Color.White else Color.White.copy(alpha = 0.18f))
                        .clickable { Timer.setMinutes(m) }
                        .padding(horizontal = 10.dp, vertical = 8.dp)
                )
            }
        }
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(24.dp)) {
            IconButton(onClick = { Timer.reset() }, modifier = Modifier.size(46.dp)) {
                Icon(SIcons.Back, contentDescription = "Reset", tint = Color.White.copy(alpha = 0.9f), modifier = Modifier.size(24.dp))
            }
            IconButton(onClick = { Timer.toggle() }, modifier = Modifier.size(72.dp).clip(CircleShape).background(Color.White)) {
                Icon(if (running) SIcons.Pause else SIcons.Play, contentDescription = "Toggle", tint = PrimaryCoral, modifier = Modifier.size(40.dp))
            }
            Spacer(Modifier.size(46.dp))
        }
        Text("Completing a session banks Fame at 2 per minute. Leaving it running is the whole game.", color = Color.White.copy(alpha = 0.75f), fontSize = 11.sp)
    }
}
