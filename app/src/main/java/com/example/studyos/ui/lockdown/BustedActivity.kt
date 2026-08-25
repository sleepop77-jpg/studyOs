package com.example.studyos.ui.lockdown

import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.studyos.MainActivity
import com.example.studyos.ui.common.ConfettiRain
import com.example.studyos.ui.launcher.InteractiveMascot
import com.example.studyos.ui.launcher.MascotState

class BustedActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val appName = intent.getStringExtra("busted_app_name") ?: "a sealed app"
        @Suppress("DEPRECATION")
        val vib = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vib.vibrate(VibrationEffect.createWaveform(longArrayOf(0, 300, 150, 300, 150, 600), -1))
        } else {
            vib.vibrate(longArrayOf(0, 300, 150, 300, 150, 600), -1)
        }
        setContent {
            SuperBustedContent(appName = appName) {
                finish()
                try {
                    startActivity(
                        Intent(this, MainActivity::class.java).apply {
                            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                        }
                    )
                } catch (_: Exception) { }
            }
        }
    }
}

@Composable
private fun SuperBustedContent(appName: String, onReturn: () -> Unit) {
    val shake = remember { Animatable(0f) }
    LaunchedEffect(Unit) {
        repeat(6) { i ->
            shake.animateTo(if (i % 2 == 0) 1f else -1f, tween(50))
        }
        shake.animateTo(0f)
    }
    val siren = rememberInfiniteTransition(label = "siren")
    val pulse by siren.animateFloat(
        initialValue = 0.25f,
        targetValue = 0.75f,
        animationSpec = infiniteRepeatable(tween(700, easing = LinearEasing), RepeatMode.Reverse),
        label = "pulse"
    )
    Box(
        modifier = Modifier
            .fillMaxSize()
            .offset(x = (shake.value * 14f).dp)
            .background(
                Brush.radialGradient(
                    colors = listOf(
                        Color(0xFFC41C3B).copy(alpha = pulse * 0.55f),
                        Color(0xFF1A0505),
                        Color(0xFF000000)
                    )
                )
            )
    ) {
        ConfettiRain(
            colors = listOf(0xFFC41C3B, 0xFF8B0000, 0xFFFF5252, 0xFF2B0503),
            active = true
        )
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxSize().padding(28.dp)
        ) {
            Spacer(modifier = Modifier.height(48.dp))
            Surface(shape = RoundedCornerShape(10.dp), color = Color(0xFFC41C3B)) {
                Text(
                    "LOCKDOWN BREACH DETECTED",
                    color = Color.White,
                    fontWeight = FontWeight.Black,
                    fontSize = 11.sp,
                    letterSpacing = 2.sp,
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                "BUSTED.",
                color = Color(0xFFFFD700),
                fontWeight = FontWeight.Black,
                fontSize = 68.sp,
                letterSpacing = 4.sp,
                style = TextStyle(
                    shadow = Shadow(color = Color(0xCCC41C3B), offset = Offset(0f, 10f), blurRadius = 30f)
                )
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                "You opened $appName.",
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                textAlign = TextAlign.Center
            )
            Text(
                "Shame added. The mascot saw everything.",
                color = Color(0xFFC41C3B),
                fontWeight = FontWeight.Black,
                fontSize = 14.sp,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(24.dp))
            InteractiveMascot(state = MascotState.FRUSTRATED, size = 190.dp, showArc = false)
            Spacer(modifier = Modifier.weight(1f))
            Button(
                onClick = onReturn,
                colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth().height(56.dp)
            ) {
                Text("BACK TO MY SESSION", color = Color(0xFF8B0000), fontWeight = FontWeight.Black, fontSize = 15.sp, letterSpacing = 1.sp)
            }
        }
    }
}
