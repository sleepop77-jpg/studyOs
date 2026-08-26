package com.example.studyos.ui.lockdown

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Whatshot
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.drawable.toBitmap
import com.example.studyos.core.Economy
import com.example.studyos.ui.common.AnimatedBackground
import com.example.studyos.ui.common.SIcons
import com.example.studyos.ui.launcher.InteractiveMascot
import com.example.studyos.ui.launcher.MascotState
import kotlinx.coroutines.delay
import kotlin.math.roundToInt

@Composable
fun BustedOverlayContent(
    appName: String,
    penalty: Int,
    appPkg: String = "",
    limitMinutes: Int = 0,
    spentMinutes: Long = 0L,
    leftMinutes: Long = 0L,
    onUnblock: () -> Unit = {},
    onGrantTime: (Int) -> Unit = {},
    onReturn: () -> Unit
) {
    var visible by remember { mutableStateOf(false) }
    var showTimeSheet by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { visible = true }
    val streak by Economy.streak.collectAsState()
    val context = LocalContext.current

    val appIcon = remember(appPkg) {
        try {
            context.packageManager.getApplicationIcon(appPkg)
        } catch (_: Exception) {
            null
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.55f))
    ) {
        AnimatedBackground()

        AnimatedVisibility(
            visible = visible,
            enter = fadeIn(tween(250)) + slideInVertically(
                animationSpec = tween(500, easing = FastOutSlowInEasing),
                initialOffsetY = { it }
            ),
            modifier = Modifier.align(Alignment.BottomCenter)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp))
                    .background(Color(0xFF0D0D0D))
                    .padding(start = 20.dp, end = 20.dp, top = 20.dp, bottom = 28.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Icon(
                        imageVector = Icons.Filled.HourglassTop,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(26.dp)
                    )

                    Surface(
                        onClick = onUnblock,
                        shape = RoundedCornerShape(50.dp),
                        color = Color(0xFF1F1F1F)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 9.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Block,
                                contentDescription = null,
                                tint = Color.White.copy(alpha = 0.85f),
                                modifier = Modifier.size(14.dp)
                            )
                            Text(
                                "Unblock app",
                                color = Color.White.copy(alpha = 0.85f),
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }

                Box(
                    modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                    contentAlignment = Alignment.Center
                ) {
                    InteractiveMascot(
                        state = MascotState.CRYING,
                        size = 150.dp,
                        showArc = false
                    )
                }

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(24.dp))
                        .background(Color(0xFF1B1B1B))
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (appIcon != null) {
                            Image(
                                bitmap = appIcon.toBitmap(96, 96).asImageBitmap(),
                                contentDescription = null,
                                modifier = Modifier.size(34.dp).clip(CircleShape)
                            )
                        } else {
                            Box(
                                modifier = Modifier.size(34.dp).clip(CircleShape).background(Color(0xFF333333)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(SIcons.Lock, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                            }
                        }

                        Spacer(modifier = Modifier.width(10.dp))

                        Text(
                            if (limitMinutes > 0) "${limitMinutes}m limit" else "Sealed app",
                            color = Color.White.copy(alpha = 0.65f),
                            fontSize = 14.sp
                        )

                        Spacer(modifier = Modifier.weight(1f))

                        Box(
                            modifier = Modifier.size(26.dp).clip(CircleShape).background(Color(0xFFF59E0B)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Whatshot,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(16.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(6.dp))

                        Text(
                            "$streak days",
                            color = Color.White.copy(alpha = 0.65f),
                            fontSize = 14.sp
                        )
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(50.dp))
                            .background(Color(0xFFF43F5E))
                            .padding(vertical = 14.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            if (limitMinutes > 0) "LIMIT REACHED!" else "LOCKDOWN BREACH!",
                            color = Color.White,
                            fontWeight = FontWeight.Black,
                            fontSize = 14.sp,
                            letterSpacing = 1.sp
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                if (limitMinutes > 0) "${spentMinutes}m" else "+$penalty",
                                color = Color.White,
                                fontWeight = FontWeight.Black,
                                fontSize = 22.sp
                            )
                            Text(
                                if (limitMinutes > 0) "Spent today" else "Shame added",
                                color = Color.White.copy(alpha = 0.5f),
                                fontSize = 11.sp
                            )
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                if (limitMinutes > 0) "${leftMinutes}m" else "-10",
                                color = Color.White,
                                fontWeight = FontWeight.Black,
                                fontSize = 22.sp
                            )
                            Text(
                                if (limitMinutes > 0) "Limit left" else "Fame lost",
                                color = Color.White.copy(alpha = 0.5f),
                                fontSize = 11.sp
                            )
                        }
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Lightbulb,
                            contentDescription = null,
                            tint = Color(0xFFFFD700),
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            "Do something else instead: solve today's Wordle, make a cookie, stretch for 2 minutes, or drink some water.",
                            color = Color.White.copy(alpha = 0.55f),
                            fontSize = 11.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = { showTimeSheet = true },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF262626),
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(50.dp),
                    modifier = Modifier.fillMaxWidth().height(56.dp)
                ) {
                    Text("Open $appName", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                }

                Spacer(modifier = Modifier.height(12.dp))

                Button(
                    onClick = onReturn,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF153A15),
                        contentColor = Color(0xFF4ADE80)
                    ),
                    shape = RoundedCornerShape(50.dp),
                    modifier = Modifier.fillMaxWidth().height(56.dp)
                ) {
                    Text("Close $appName", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                }
            }
        }

        AnimatedVisibility(
            visible = showTimeSheet,
            enter = fadeIn(tween(200)),
            exit = fadeOut(tween(150))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.5f))
                    .clickable { showTimeSheet = false }
            )
        }

        AnimatedVisibility(
            visible = showTimeSheet,
            enter = fadeIn(tween(200)) + slideInVertically(
                animationSpec = tween(400, easing = FastOutSlowInEasing),
                initialOffsetY = { it }
            ),
            exit = fadeOut(tween(150)) + slideOutVertically(
                animationSpec = tween(200, easing = FastOutSlowInEasing),
                targetOffsetY = { it }
            ),
            modifier = Modifier.align(Alignment.BottomCenter)
        ) {
            TimeChoiceSheet(
                onBack = { showTimeSheet = false },
                onGrant = onGrantTime
            )
        }
    }
}

@Composable
private fun TimeChoiceSheet(
    onBack: () -> Unit,
    onGrant: (Int) -> Unit
) {
    var elapsed by remember { mutableStateOf(0) }
    LaunchedEffect(Unit) {
        while (true) {
            delay(1000L)
            elapsed++
        }
    }

    var scoldCount by remember { mutableStateOf(0) }
    val shakeX = remember { Animatable(0f) }
    LaunchedEffect(scoldCount) {
        if (scoldCount > 0) {
            repeat(6) { i ->
                shakeX.animateTo(if (i % 2 == 0) 1f else -1f, tween(40))
            }
            shakeX.animateTo(0f)
        }
    }

    val options = listOf(
        2 to 10,
        5 to 20,
        10 to 30,
        20 to 40
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp))
            .background(Color(0xFF0D0D0D))
            .padding(20.dp)
    ) {
        Text(
            "HOW LONG DO YOU NEED?",
            color = Color.White,
            fontWeight = FontWeight.Black,
            fontSize = 16.sp,
            letterSpacing = 1.sp
        )
        Text(
            "Buttons unlock slowly on purpose. Pick the minimum.",
            color = Color.White.copy(alpha = 0.55f),
            fontSize = 12.sp
        )

        Spacer(modifier = Modifier.height(16.dp))

        options.forEach { (mins, unlockAt) ->
            val unlocked = elapsed >= unlockAt
            val wait = unlockAt - elapsed
            val fraction = (elapsed.coerceAtMost(unlockAt)) / unlockAt.toFloat()

            if (mins == 20) {
                Button(
                    onClick = { scoldCount++ },
                    enabled = unlocked,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF3A0D12),
                        contentColor = Color(0xFFFF8A80),
                        disabledContainerColor = Color(0xFF1B1B1B),
                        disabledContentColor = Color.White.copy(alpha = 0.35f)
                    ),
                    shape = RoundedCornerShape(50.dp),
                    modifier = Modifier.fillMaxWidth().height(52.dp)
                ) {
                    Text(
                        if (unlocked) "20 min" else "20 min • unlocks in ${wait}s",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }
            } else {
                Button(
                    onClick = { onGrant(mins) },
                    enabled = unlocked,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF262626),
                        contentColor = Color.White,
                        disabledContainerColor = Color(0xFF1B1B1B),
                        disabledContentColor = Color.White.copy(alpha = 0.35f)
                    ),
                    shape = RoundedCornerShape(50.dp),
                    modifier = Modifier.fillMaxWidth().height(52.dp)
                ) {
                    Text(
                        if (unlocked) "$mins min" else "$mins min • unlocks in ${wait}s",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }
            }

            if (!unlocked) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(4.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF2A2A2A))
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(fraction)
                            .height(4.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFE53935))
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
        }

        if (scoldCount > 0) {
            Text(
                "That's too much time! Scroll for less time!",
                color = Color(0xFFFF8A80),
                fontWeight = FontWeight.Black,
                fontSize = 13.sp,
                modifier = Modifier.offset { IntOffset((shakeX.value * 10f).roundToInt(), 0) }
            )
            Spacer(modifier = Modifier.height(8.dp))
        }

        Button(
            onClick = onBack,
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF153A15),
                contentColor = Color(0xFF4ADE80)
            ),
            shape = RoundedCornerShape(50.dp),
            modifier = Modifier.fillMaxWidth().height(52.dp)
        ) {
            Text("Go back", fontWeight = FontWeight.Bold, fontSize = 14.sp)
        }
    }
}