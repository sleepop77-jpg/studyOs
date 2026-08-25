package com.example.studyos.ui.lockdown

import android.content.Intent
import android.graphics.drawable.Drawable
import android.provider.Settings
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.toBitmap
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.example.studyos.core.BustedOverlay
import com.example.studyos.core.LockdownManager
import com.example.studyos.core.LockdownService
import com.example.studyos.core.Timer
import com.example.studyos.ui.common.SIcons
import com.example.studyos.ui.common.homeBrush
import com.hjq.permissions.OnPermissionCallback
import com.hjq.permissions.Permission
import com.hjq.permissions.XXPermissions

data class AppEntry(val pkg: String, val label: String, val icon: Drawable)

private val KNOWN_DISTRACTIONS = listOf(
    "com.instagram.android" to "Instagram",
    "com.google.android.youtube" to "YouTube",
    "com.whatsapp" to "WhatsApp",
    "com.snapchat.android" to "Snapchat",
    "com.twitter.android" to "X / Twitter",
    "com.reddit.frontpage" to "Reddit",
    "com.discord" to "Discord",
    "com.netflix.mediaclient" to "Netflix",
    "org.telegram.messenger" to "Telegram",
    "com.android.vending" to "Play Store"
)

@Composable
fun LockdownScreen(back: () -> Unit) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    var enabled by remember { mutableStateOf(LockdownManager.isEnabled(context)) }
    var hasAccess by remember { mutableStateOf(LockdownManager.hasUsageAccess(context)) }
    var hasOverlay by remember { mutableStateOf(BustedOverlay.canShow(context)) }
    var blocked by remember { mutableStateOf(LockdownManager.blockedPackages(context)) }
    var query by remember { mutableStateOf("") }
    val timerRunning by Timer.running.collectAsState()

    fun syncService() {
        val i = Intent(context, LockdownService::class.java)
        if (hasAccess) {
            ContextCompat.startForegroundService(context, i)
        } else {
            context.stopService(i)
        }
    }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                enabled = LockdownManager.isEnabled(context)
                hasAccess = LockdownManager.hasUsageAccess(context)
                hasOverlay = BustedOverlay.canShow(context)
                blocked = LockdownManager.blockedPackages(context)
                syncService()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    val apps = remember {
        val pm = context.packageManager
        val installed = pm.getInstalledApplications(0)
            .mapNotNull { info ->
                if (info.packageName == context.packageName) return@mapNotNull null
                if (pm.getLaunchIntentForPackage(info.packageName) == null) return@mapNotNull null
                AppEntry(info.packageName, pm.getApplicationLabel(info).toString(), pm.getApplicationIcon(info))
            }
        val installedPkgs = installed.map { it.pkg }.toSet()
        val missing = KNOWN_DISTRACTIONS
            .filter { !installedPkgs.contains(it.first) }
            .map { (pkg, label) -> AppEntry(pkg, label, ContextCompat.getDrawable(context, android.R.drawable.sym_def_app_icon)!!) }
        (installed + missing).sortedBy { it.label.lowercase() }
    }
    val filtered = apps.filter { query.isBlank() || it.label.contains(query, ignoreCase = true) || it.pkg.contains(query, ignoreCase = true) }
    val bgBrush = homeBrush()

    Column(Modifier.fillMaxSize().background(bgBrush)) {
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
                                if (timerRunning) "Sealed while a focus session is running." else "Arms automatically when your timer runs. Opening a sealed app yanks you back and burns Shame.",
                                color = Color(0xFF756565), fontSize = 11.sp
                            )
                        }
                        Switch(
                            checked = enabled,
                            enabled = !timerRunning,
                            onCheckedChange = {
                                enabled = it
                                LockdownManager.setEnabled(context, it)
                                syncService()
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
                            XXPermissions.with(context)
                                .permission(Permission.SYSTEM_ALERT_WINDOW)
                                .request(object : OnPermissionCallback {
                                    override fun onGranted(permissions: MutableList<String>, all: Boolean) {
                                        hasOverlay = all
                                    }
                                    override fun onDenied(permissions: MutableList<String>, never: Boolean) {
                                        hasOverlay = false
                                    }
                                })
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFC41C3B)),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.fillMaxWidth().height(46.dp)
                    ) { Text("2. Allow Display Over Apps (BUSTED screen)", color = Color.White, fontWeight = FontWeight.Black, fontSize = 13.sp) }
                }
            }
            item {
                Button(
                    onClick = {
                        val ok = BustedOverlay.show(context) {
                            BustedOverlayContent("Test App", 3) { BustedOverlay.hide() }
                        }
                        if (!ok) {
                            try {
                                context.startActivity(
                                    Intent(context, BustedActivity::class.java).apply {
                                        flags = Intent.FLAG_ACTIVITY_NEW_TASK
                                        putExtra("busted_app_name", "Test App")
                                    }
                                )
                            } catch (_: Exception) { }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF20B2AA)),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth().height(46.dp)
                ) { Text("TEST BUSTED SCREEN", color = Color.White, fontWeight = FontWeight.Black, fontSize = 13.sp) }
            }
            item {
                OutlinedTextField(
                    value = query,
                    onValueChange = { query = it },
                    label = { Text("Search apps") },
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
