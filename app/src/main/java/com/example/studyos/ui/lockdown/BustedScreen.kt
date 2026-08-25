package com.example.studyos.ui.lockdown

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.studyos.ui.launcher.InteractiveMascot
import com.example.studyos.ui.launcher.MascotState
import com.example.studyos.ui.theme.PrimaryCoral

@Composable
fun BustedOverlayContent(appName: String, penalty: Int, onReturn: () -> Unit) {
    MaterialTheme(colorScheme = darkColorScheme(primary = PrimaryCoral)) {
        Box(
            modifier = Modifier.fillMaxSize().background(Color(0xFF1A0505)),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(28.dp)
            ) {
                Text("BUSTED.", color = Color(0xFFFFD700), fontWeight = FontWeight.Black, fontSize = 48.sp, letterSpacing = 2.sp)
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    "You opened $appName.\nYour focus is broken.",
                    color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp, textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    "+$penalty Shame added. -10 Fame deducted.",
                    color = Color(0xFFFF5252), fontWeight = FontWeight.Bold, fontSize = 14.sp, textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(24.dp))
                InteractiveMascot(state = MascotState.FRUSTRATED, size = 180.dp, showArc = false)
                Spacer(modifier = Modifier.height(32.dp))
                Button(
                    onClick = onReturn,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD9534F)),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.height(54.dp)
                ) {
                    Text("RETURN TO STUDYOS", color = Color.White, fontWeight = FontWeight.Black, fontSize = 16.sp)
                }
            }
        }
    }
}
