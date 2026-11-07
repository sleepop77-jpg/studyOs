package com.example.studyos.ui.lockdown

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.provider.Settings
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
import com.example.studyos.ui.common.AnimatedBackground
import com.example.studyos.ui.common.RedAura
import com.example.studyos.ui.common.RedPatchesBackground
import com.example.studyos.ui.common.SIcons
import com.example.studyos.ui.common.homeBrush
import com.hjq.permissions.OnPermissionCallback
import com.hjq.permissions.Permission
import com.hjq.permissions.XXPermissions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

data class AppEntry(
    val pkg: String,
    val label: String,
    val icon: Bitmap
)

private val KNOWN_DISTRACTIONS = listOf(
    "com.instagram.android" to "Instagram",
    "com.twitter.android" to "X / Twitter",
    "com.reddit.frontpage" to "Reddit",
    "com.google.android.youtube" to "YouTube",
    "com.whatsapp" to "WhatsApp",
    "com.zhiliaoapp.musically" to "TikTok",
    "com.snapchat.android" to "Snapchat",
    "com.facebook.katana" to "Facebook",
    "com.instagram.barcelona" to "Threads",
    "com.discord" to "Discord",
    "org.telegram.messenger" to "Telegram",
    "tv.twitch.android.app" to "Twitch",
    "com.netflix.mediaclient" to "Netflix",
    "com.pinterest" to "Pinterest",
    "com.android.vending" to "Play Store"
)

private object AppCatalogCache {
    @Volatile
    private var entries: List<AppEntry>? = null

    @Volatile
    private var loadedAt: Long = 0L

    fun get(): List<AppEntry>? {
        val cached = entries ?: return null
        return cached.takeIf {
            System.currentTimeMillis() - loadedAt < 10 * 60 * 1000L
        }
    }

    fun put(apps: List<AppEntry>) {
        entries = apps
        loadedAt = System.currentTimeMillis()
    }
}

@Suppress("DEPRECATION")
private fun loadAppCatalog(context: Context): List<AppEntry> {
    val pm = context.packageManager

    val fallbackIcon = runCatching {
        ContextCompat.getDrawable(
            context,
            android.R.drawable.sym_def_app_icon
        )?.toBitmap(48, 48)
    }.getOrNull() ?: Bitmap.createBitmap(
        48,
        48,
        Bitmap.Config.ARGB_8888
    )

    val installed = try {
        pm.getInstalledApplications(0)
    } catch (_: Exception) {
        emptyList()
    }

    val apps = installed.mapNotNull { info ->
        try {
            if (info.packageName == context.packageName) {
                return@mapNotNull null
            }

            if (pm.getLaunchIntentForPackage(info.packageName) == null) {
                return@mapNotNull null
            }

            val label = pm.getApplicationLabel(info).toString()
            val icon = try {
                pm.getApplicationIcon(info).toBitmap(48, 48)
            } catch (_: Exception) {
                fallbackIcon
            }

            AppEntry(
                pkg = info.packageName,
                label = label,
                icon = icon
            )
        } catch (_: Exception) {
            null
        }
    }

    val installedPackages = apps.map { it.pkg }.toSet()

    val missingDistractions = KNOWN_DISTRACTIONS
        .filter { it.first !in installedPackages }
        .map { (pkg, label) ->
            AppEntry(
                pkg = pkg,
                label = label,
                icon = fallbackIcon
            )
        }

    return apps + missingDistractions
}

private fun formatSecs(s: Long): String {
    val m = s / 60
    return if (m < 60) "${m}m" else "${m / 60}h ${m % 60}m"
}

private fun formatLimit(min: Int): String =
    if (min < 60) "${min}m" else "${min / 60}h"

@Composable
fun LockdownScreen(back: () -> Unit) {
    val context = LocalContext.current
    val appContext = context.applicationContext
    val lifecycleOwner = LocalLifecycleOwner.current

    var enabled by remember {
        mutableStateOf(LockdownManager.isEnabled(context))
    }
    var hasAccess by remember {
        mutableStateOf(LockdownManager.hasUsageAccess(context))
    }
    var hasOverlay by remember {
        mutableStateOf(BustedOverlay.canShow(context))
    }
    var blocked by remember {
        mutableStateOf(LockdownManager.blockedPackages(context))
    }
    var blockedOrder by remember {
        mutableStateOf(LockdownManager.blockedPackageOrder(context))
    }
    var query by remember { mutableStateOf("") }

    val timerRunning by Timer.running.collectAsState()
    val limits by LockdownManager.limitsFlow.collectAsState()
    val usage by LockdownManager.usageFlow.collectAsState()

    val appCatalog by produceState<List<AppEntry>?>(
        initialValue = AppCatalogCache.get(),
        key1 = appContext
    ) {
        if (value == null) {
            val loaded = withContext(Dispatchers.IO) {
                runCatching {
                    loadAppCatalog(appContext)
                }.getOrDefault(emptyList())
            }
            AppCatalogCache.put(loaded)
            value = loaded
        }
    }

    remember {
        LockdownManager.appLimits(context)
        LockdownManager.usageToday(context)
        true
    }

    fun syncService() {
        val intent = Intent(context, LockdownService::class.java)
        if (hasAccess) {
            ContextCompat.startForegroundService(context, intent)
        } else {
            context.stopService(intent)
        }
    }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                enabled = LockdownManager.isEnabled(context)
                hasAccess = LockdownManager.hasUsageAccess(context)
                hasOverlay = BustedOverlay.canShow(context)
                blocked = LockdownManager.blockedPackages(context)
                blockedOrder = LockdownManager.blockedPackageOrder(context)
                syncService()
            }
        }

        lifecycleOwner.lifecycle.addObserver(observer)

        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    val orderedApps = remember(appCatalog, blocked, blockedOrder, query) {
        val apps = appCatalog ?: emptyList()
        val blockedRanks = blockedOrder
            .withIndex()
            .associate { it.value to it.index }
        val distractionRanks = KNOWN_DISTRACTIONS
            .withIndex()
            .associate { it.value.first to it.index }

        apps.asSequence()
            .filter {
                query.isBlank() ||
                    it.label.contains(query, ignoreCase = true) ||
                    it.pkg.contains(query, ignoreCase = true)
            }
            .sortedWith(
                compareBy<AppEntry> {
                    if (it.pkg in blocked) 0 else 1
                }.thenBy {
                    if (it.pkg in blocked) {
                        blockedRanks[it.pkg] ?: Int.MAX_VALUE
                    } else {
                        distractionRanks[it.pkg] ?: Int.MAX_VALUE
                    }
                }.thenBy {
                    it.label.lowercase()
                }
            )
            .toList()
    }

    Box(
        Modifier
            .fillMaxSize()
            .background(homeBrush())
    ) {
        AnimatedBackground()
        RedPatchesBackground()
        RedAura()

        Column(Modifier.fillMaxSize()) {
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(
                        top = 32.dp,
                        start = 8.dp,
                        end = 20.dp,
                        bottom = 16.dp
                    ),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = back,
                    modifier = Modifier
                        .size(42.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color.White.copy(alpha = 0.12f))
                ) {
                    Icon(
                        SIcons.Back,
                        contentDescription = "Back",
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Text(
                    "APP BLOCKER",
                    color = Color.White,
                    fontWeight = FontWeight.Black,
                    fontSize = 20.sp,
                    letterSpacing = 1.5.sp,
                    modifier = Modifier.padding(start = 16.dp)
                )
            }

            LazyColumn(
                Modifier
                    .fillMaxSize()
                    .padding(horizontal = 20.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                item {
                    Card(
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = Color.White.copy(alpha = 0.08f)
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            Modifier.padding(20.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(
                                Modifier.weight(1f),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(
                                    "Lockdown Mode",
                                    color = Color.White,
                                    fontWeight = FontWeight.Black,
                                    fontSize = 16.sp,
                                    letterSpacing = 0.5.sp
                                )

                                Text(
                                    if (timerRunning) {
                                        "Sealed while a focus session is running. Daily limits always apply when Lockdown is on."
                                    } else {
                                        "Arms automatically when your timer runs. Daily limits always apply when Lockdown is on."
                                    },
                                    color = Color.White.copy(alpha = 0.6f),
                                    fontSize = 11.sp,
                                    lineHeight = 16.sp,
                                    letterSpacing = 0.2.sp
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
                                colors = SwitchDefaults.colors(
                                    checkedTrackColor = Color(0xFFD9534F)
                                )
                            )
                        }
                    }
                }

                if (enabled && !hasAccess) {
                    item {
                        Button(
                            onClick = {
                                try {
                                    context.startActivity(
                                        Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS)
                                    )
                                } catch (_: Exception) {
                                }
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFFC41C3B)
                            ),
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                        ) {
                            Text(
                                "1. Grant Usage Access (detect apps)",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                letterSpacing = 0.5.sp
                            )
                        }
                    }
                }

                if (enabled && !hasOverlay) {
                    item {
                        Button(
                            onClick = {
                                XXPermissions.with(context)
                                    .permission(Permission.SYSTEM_ALERT_WINDOW)
                                    .request(object : OnPermissionCallback {
                                        override fun onGranted(
                                            permissions: MutableList<String>,
                                            all: Boolean
                                        ) {
                                            hasOverlay = all
                                        }

                                        override fun onDenied(
                                            permissions: MutableList<String>,
                                            never: Boolean
                                        ) {
                                            hasOverlay = false
                                        }
                                    })
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFFC41C3B)
                            ),
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                        ) {
                            Text(
                                "2. Allow Display Over Apps (BUSTED screen)",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                letterSpacing = 0.5.sp
                            )
                        }
                    }
                }

                item {
                    Button(
                        onClick = {
                            val ok = BustedOverlay.show(context) {
                                BustedOverlayContent("Test App", 3) {
                                    BustedOverlay.hide()
                                }
                            }

                            if (!ok) {
                                try {
                                    context.startActivity(
                                        Intent(
                                            context,
                                            com.example.studyos.ui.lockdown.BustedActivity::class.java
                                        ).apply {
                                            flags = Intent.FLAG_ACTIVITY_NEW_TASK
                                            putExtra("busted_app_name", "Test App")
                                        }
                                    )
                                } catch (_: Exception) {
                                }
                            }
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF20B2AA)
                        ),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                    ) {
                        Text(
                            "TEST BUSTED SCREEN",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            letterSpacing = 0.5.sp
                        )
                    }
                }

                item {
                    OutlinedTextField(
                        value = query,
                        onValueChange = { query = it },
                        label = {
                            Text(
                                "Search apps",
                                color = Color.White.copy(alpha = 0.6f)
                            )
                        },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                item {
                    Text(
                        "Sealed apps: ${blocked.size}",
                        color = Color.White.copy(alpha = 0.7f),
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        letterSpacing = 0.5.sp
                    )
                }

                if (appCatalog == null) {
                    item {
                        Text(
                            "Loading installed apps…",
                            color = Color.White.copy(alpha = 0.65f),
                            fontSize = 13.sp,
                            modifier = Modifier.padding(vertical = 20.dp)
                        )
                    }
                } else if (orderedApps.isEmpty()) {
                    item {
                        Text(
                            "No matching apps found.",
                            color = Color.White.copy(alpha = 0.65f),
                            fontSize = 13.sp,
                            modifier = Modifier.padding(vertical = 20.dp)
                        )
                    }
                }

                items(
                    orderedApps,
                    key = { it.pkg }
                ) { app ->
                    val limitMin = limits[app.pkg] ?: 0
                    val usedSec = usage[app.pkg] ?: 0L
                    val overLimit = limitMin > 0 && usedSec >= limitMin * 60L
                    val appIcon = remember(app.pkg) {
                        app.icon.asImageBitmap()
                    }

                    Card(
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = Color.White.copy(alpha = 0.06f)
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(Modifier.padding(14.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Image(
                                    bitmap = appIcon,
                                    contentDescription = null,
                                    modifier = Modifier.size(32.dp)
                                )

                                Column(
                                    Modifier
                                        .weight(1f)
                                        .padding(horizontal = 12.dp)
                                ) {
                                    Text(
                                        app.label,
                                        color = Color.White,
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 13.sp,
                                        letterSpacing = 0.3.sp
                                    )

                                    Text(
                                        if (limitMin > 0) {
                                            "Today: ${formatSecs(usedSec)} / ${formatLimit(limitMin)}"
                                        } else {
                                            "Today: ${formatSecs(usedSec)}"
                                        },
                                        color = if (overLimit) {
                                            Color(0xFFFF8A80)
                                        } else {
                                            Color.White.copy(alpha = 0.5f)
                                        },
                                        fontSize = 11.sp
                                    )
                                }

                                Checkbox(
                                    checked = app.pkg in blocked,
                                    enabled = !timerRunning,
                                    onCheckedChange = { add ->
                                        val updated = if (add) {
                                            blocked + app.pkg
                                        } else {
                                            blocked - app.pkg
                                        }

                                        LockdownManager.setBlockedPackages(
                                            context,
                                            updated,
                                            newlyBlocked = if (add) app.pkg else null
                                        )

                                        blocked = updated
                                        blockedOrder =
                                            LockdownManager.blockedPackageOrder(context)
                                    },
                                    colors = CheckboxDefaults.colors(
                                        checkedColor = Color(0xFFD9534F)
                                    )
                                )
                            }

                            LimitPicker(current = limitMin) { min ->
                                LockdownManager.setAppLimit(
                                    context,
                                    app.pkg,
                                    min
                                )
                            }
                        }
                    }
                }

                item {
                    Spacer(Modifier.height(32.dp))
                }
            }
        }
    }
}

@Composable
private fun LimitPicker(
    current: Int,
    onSelect: (Int) -> Unit
) {
    val presets = listOf(
        0 to "Off",
        5 to "5m",
        15 to "15m",
        30 to "30m",
        60 to "1h",
        120 to "2h",
        240 to "4h",
        480 to "8h"
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 10.dp)
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        presets.forEach { (min, label) ->
            val selected = current == min

            Surface(
                onClick = { onSelect(min) },
                shape = RoundedCornerShape(10.dp),
                color = if (selected) {
                    Color(0xFFD9534F)
                } else {
                    Color.White.copy(alpha = 0.08f)
                },
                modifier = Modifier.height(28.dp)
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.padding(horizontal = 12.dp)
                ) {
                    Text(
                        label,
                        color = Color.White,
                        fontSize = 10.sp,
                        fontWeight = if (selected) {
                            FontWeight.Black
                        } else {
                            FontWeight.Medium
                        },
                        letterSpacing = 0.5.sp
                    )
                }
            }
        }
    }
}
