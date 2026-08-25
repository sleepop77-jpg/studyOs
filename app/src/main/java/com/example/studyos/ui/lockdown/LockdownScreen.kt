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
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.drawable.toBitmap
import com.example.studyos.core.LockdownManager
import com.example.studyos.ui.common.SIcons

data class AppEntry(val pkg: String, val label: String, val icon: Drawable)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LockdownScreen(back: () -> Unit) {
    val context = LocalContext.current
    var enabled by remember { mutableStateOf(LockdownManager.isEnabled(context)) }
    var hasAccess by remember { mutableStateOf(LockdownManager.hasUsageAccess(context)) }
    var blocked by remember { mutableStateOf(LockdownManager.blockedPackages(context)) }
    
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

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("APP BLOCKER", color = Color.White, fontWeight = FontWeight.Black) },
                navigationIcon = {
                    IconButton(onClick = back) {
                        Icon(SIcons.Back, contentDescription = "Back", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        },
        containerColor = Color.Transparent
    ) { pad ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF241515))
                .padding(pad)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF5E6E5)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Lockdown Mode", color = Color(0xFF2D2D2D), fontWeight = FontWeight.Black, fontSize = 16.sp)
                            Text(
                                "During any focus session, opening a blocked app yanks you back and burns Shame.",
                                color = Color(0xFF756565),
                                fontSize = 11.sp
                            )
                        }
                        Switch(
                            checked = enabled,
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
                            try {
                                context.startActivity(Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS))
                            } catch (_: Exception) { }
                            hasAccess = LockdownManager.hasUsageAccess(context)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFC41C3B)),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.fillMaxWidth().height(46.dp)
                    ) {
                        Text("Grant Usage Access (Required)", color = Color.White, fontWeight = FontWeight.Black, fontSize = 13.sp)
                    }
                }
            }
            
            item {
                Text("Sealed apps (${blocked.size})", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
            }
            
            items(apps, key = { it.pkg }) { app ->
                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.14f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Image(
                            bitmap = app.icon.toBitmap(48, 48).asImageBitmap(),
                            contentDescription = null,
                            modifier = Modifier.size(30.dp)
                        )
                        Text(
                            app.label,
                            color = Color.White,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 13.sp,
                            modifier = Modifier.weight(1f).padding(horizontal = 10.dp)
                        )
                        Checkbox(
                            checked = blocked.contains(app.pkg),
                            onCheckedChange = { add ->
                                blocked = if (add) blocked + app.pkg else blocked - app.pkg
                                LockdownManager.setBlockedPackages(context, blocked)
                            },
                            colors = CheckboxDefaults.colors(checkedColor = Color(0xFFD9534F))
                        )
                    }
                }
            }
            item { Spacer(modifier = Modifier.height(24.dp)) }
        }
    }
}
