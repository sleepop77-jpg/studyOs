package com.example.studyos.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.studyos.core.Admin
import com.example.studyos.core.Economy
import com.example.studyos.core.Store
import com.example.studyos.ui.theme.AccentTeal
import com.example.studyos.ui.theme.FameGold
import com.example.studyos.ui.theme.OnSurfaceDark
import com.example.studyos.ui.theme.WarningRed
import kotlinx.coroutines.launch

@Composable
fun SettingsScreen(back: () -> Unit) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val isAdmin by Admin.enabled.collectAsState()
    var taps by remember { mutableIntStateOf(0) }
    var showCode by remember { mutableStateOf(false) }
    Column(
        modifier = Modifier.fillMaxSize().background(com.example.studyos.ui.common.homeBrush()).verticalScroll(rememberScrollState()).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Text(
            "StudyOS Settings", color = Color.White, fontWeight = FontWeight.Black, fontSize = 20.sp,
            modifier = Modifier.padding(top = 16.dp).clickable {
                taps += 1
                if (taps >= 7) { taps = 0; showCode = true }
            }
        )
        Card(shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = Color.White), modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text("ECONOMY RULES", fontWeight = FontWeight.Bold, fontSize = 11.sp, color = Color(0xFFC94440), letterSpacing = 1.sp)
                Text("+2 Fame per minute in active sessions\n+1 Shame per minute idle (5 AM to 10 PM)\nDanger hours 4 to 6 PM: x3 Shame\nFame buys store cosmetics", fontSize = 13.sp, color = OnSurfaceDark, lineHeight = 18.sp)
            }
        }
        if (isAdmin) {
            Card(shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = Color(0xFF330000)), modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("ADMIN MODE ACTIVE", fontWeight = FontWeight.Bold, fontSize = 11.sp, color = WarningRed, letterSpacing = 1.sp)
                    Button(onClick = { Economy.addFame(10000) }, colors = ButtonDefaults.buttonColors(containerColor = FameGold)) {
                        Text("+10,000 Fame", color = OnSurfaceDark, fontWeight = FontWeight.Bold)
                    }
                    Button(onClick = { Store.unlockAll() }, colors = ButtonDefaults.buttonColors(containerColor = AccentTeal)) {
                        Text("Unlock All Store Items", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                    Button(onClick = { Admin.set(context, false) }, colors = ButtonDefaults.buttonColors(containerColor = WarningRed)) {
                        Text("Disable Admin Mode", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
        Text("Tap the title 7 times if you know what you are doing.", color = Color.White.copy(alpha = 0.5f), fontSize = 10.sp)
    }
    if (showCode) {
        var code by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { showCode = false },
            title = { Text("Enter Admin Code", fontWeight = FontWeight.Bold) },
            text = { OutlinedTextField(value = code, onValueChange = { code = it }, label = { Text("Secret code") }, singleLine = true) },
            confirmButton = {
                Button(onClick = {
                    if (code.trim() == Admin.SECRET_CODE) {
                        Admin.set(context, true)
                        Economy.addFame(10000)
                        Store.unlockAll()
                    }
                    showCode = false
                }, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD9534F))) { Text("Unlock") }
            },
            dismissButton = { TextButton(onClick = { showCode = false }) { Text("Cancel") } }
        )
    }
}
