package com.example.studyos.ui.lockdown

import android.content.Intent
import android.graphics.drawable.Drawable
import android.provider.Settings
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.drawable.toBitmap
import com.example.studyos.core.LockdownManager
import com.example.studyos.core.Timer
import com.example.studyos.ui.common.SIcons

data class AppEntry(val pkg: String, val label: String, val icon: Drawable)

private val QUICK_APPS = listOf(
    "com.instagram.android" to "Instagram",
    "com.google.android.youtube" to "YouTube",
    "com.zhiliaoapp.musically" to "TikTok",
    "com.whatsapp" to "WhatsApp",
    "com.snapchat.android" to "Snapchat",
    "com.twitter.android" to "X / Twitter",
    "com.reddit.frontpage" to "Reddit",
    "com.discord" to "Discord",
    "com.netflix.mediaclient" to "Netflix",
    "org.telegram.messenger" to "Telegram"
)

@Composable
fun LockdownScreen(back: () -> Unit) {
    val context = LocalContext.current
    var enabled by remember { mutableStateOf(LockdownManager.isEnabled(context)) }
    var hasAccess by remember { mutableStateOf(LockdownManager.hasUsageAccess(context)) }
    var hasOverlay by remember { mutableStateOf(LockdownManager.hasOverlayPermission(context)) }
    var blocked by remember { mutableStateOf(LockdownManager.blockedPackages(context)) }
    var query by remember { mutableStateOf("") }
    val timerRunning by Timer.running.collectAsState()

    val apps = remember {
        val pm = context.packageManager
        pm.getInstalledApplications(0)
            .mapNotNull { info ->
                if (info.packageName == context.packageName) return@mapNotNull null
                if (pm.getLaunchIntentForPackage(info.packageName) == null) return@mapNotNull null
                AppEntry(info.packageName, pm.getApplicationLabel(info).toString(), pm.getApplicationIcon(info))
            }
            .sortedBy { it.label.lowercase() }
    }
    val filtered = apps.filter { query.isBlank() || it.label.contains(query, ignoreCase = true) }
    val homeBrush = Brush.verticalGradient(listOf(Color(0xFFD9534F), Color(0xFFC94440)))

    Column(Modifier.fillMaxSize().background(homeBrush)) {
        Row(
            Modifier.fillMaxWidth().padding(top = 24.dp, start = 4.dp, end = 16.dp, bottom = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = back) { Icon(SIcons.Back, contentDescription = "Back", tint = Color.White) }
            Text("APP BLOCKER", color = Color.White, fontWeight = FontWeight.Black, fontSize = 18.sp, letterSpacing = 2.sp)
        }
        LazyColumn(
            Modifier.fillMaxSize().padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Card(shape = RoundedCornerShape(20.dp), colors = CardDefaults.cardColors(containerColor = Color(0xFFF5E6E5)), modifier = Modifier.fillMaxWidth()) {
                    Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                        Column(Modifier.weight(1f)) {
                            Text("Lockdown Mode", color = Color(0xFF2D2D2D), fontWeight = FontWeight.Black, fontSize = 16.sp)
                            Text(
                                if (timerRunning) "Sealed while a focus session is running." else "During any focus session, opening a sealed app yanks you back and burns Shame.",
                                color = Color(0xFF756565), fontSize = 11.sp
                            )
                        }
                        Switch(
                            checked = enabled,
                            enabled = !timerRunning,
                            onCheckedChange = {
                                enabled = it
                                LockdownManager.setEnabled(context, it)
                            },
                            colors = SwitchDefaults.colors(checkedTrackColor = Color(0xFFD9534F))
                        )
                    }
                }
            }
            if (enabled && !hasAccess) {
                item {
                    Button(
                        onClick = {
                            try { context.startActivity(Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS)) } catch (_: Exception) { }
                            hasAccess = LockdownManager.hasUsageAccess(context)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFC41C3B)),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.fillMaxWidth().height(46.dp)
                    ) { Text("1. Grant Usage Access (detect apps)", color = Color.White, fontWeight = FontWeight.Black, fontSize = 13.sp) }
                }
            }
            if (enabled && !hasOverlay) {
                item {
                    Button(
                        onClick = {
                            try { context.startActivity(Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION)) } catch (_: Exception) { }
                            hasOverlay = LockdownManager.hasOverlayPermission(context)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFC41C3B)),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.fillMaxWidth().height(46.dp)
                    ) { Text("2. Grant Display Over Apps (show BUSTED screen)", color = Color.White, fontWeight = FontWeight.Black, fontSize = 13.sp) }
                }
            }
            item {
                Text("Quick-seal known distractions", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
            }
            item {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(QUICK_APPS, key = { it.first }) { (pkg, label) ->
                        val isOn = blocked.contains(pkg)
                        Text(
                            label,
                            color = if (isOn) Color.White else Color(0xFF2D2D2D),
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            modifier = Modifier
                                .background(if (isOn) Color(0xFFC41C3B) else Color.White.copy(alpha = 0.85f), RoundedCornerShape(12.dp))
                                .clickable {
                                    if (!timerRunning) {
                                        blocked = if (isOn) blocked - pkg else blocked + pkg
                                        LockdownManager.setBlockedPackages(context, blocked)
                                    }
                                }
                                .padding(horizontal = 12.dp, vertical = 8.dp)
                        )
                    }
                }
            }
            item {
                OutlinedTextField(
                    value = query,
                    onValueChange = { query = it },
                    label = { Text("Search all installed apps") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
            item { Text("Sealed apps: ${blocked.size}", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp) }
            items(filtered, key = { it.pkg }) { app ->
                Card(shape = RoundedCornerShape(14.dp), colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.14f)), modifier = Modifier.fillMaxWidth()) {
                    Row(Modifier.padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
                        Image(bitmap = app.icon.toBitmap(48, 48).asImageBitmap(), contentDescription = null, modifier = Modifier.size(30.dp))
                        Text(app.label, color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 13.sp, modifier = Modifier.weight(1f).padding(horizontal = 10.dp))
                        Checkbox(
                            checked = blocked.contains(app.pkg),
                            enabled = !timerRunning,
                            onCheckedChange = { add ->
                                blocked = if (add) blocked + app.pkg else blocked - app.pkg
                                LockdownManager.setBlockedPackages(context, blocked)
                            },
                            colors = CheckboxDefaults.colors(checkedColor = Color(0xFFD9534F))
                        )
                    }
                }
            }
            item { Spacer(Modifier.height(24.dp)) }
        }
    }
}
