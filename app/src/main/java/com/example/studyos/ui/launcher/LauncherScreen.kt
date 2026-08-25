package com.example.studyos.ui.launcher

import androidx.compose.animation.*
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.animateFloat
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.studyos.core.EconomyManager
import com.example.studyos.core.MascotState
import com.example.studyos.core.TimeBasedThemeManager
import com.example.studyos.core.TimeOfDayPhase
import com.example.studyos.data.repository.StudyRepository
import com.example.studyos.ui.common.StudyIcons
import com.example.studyos.ui.common.customShimmer
import com.example.studyos.ui.theme.*
import kotlin.math.sin

data class AppGridItem(
    val title: String,
    val icon: ImageVector,
    val color: Color,
    val badge: String? = null,
    val badgeColor: Color = Color(0xFFE53935),
    val route: String
)

@Composable
fun LauncherScreen(
    repository: StudyRepository,
    economyManager: EconomyManager,
    themeManager: TimeBasedThemeManager,
    onNavigateToRoute: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val totalFame by economyManager.totalFame.collectAsState()
    val totalShame by economyManager.totalShame.collectAsState()
    val streakDays by economyManager.currentStreakDays.collectAsState()
    val mascotState by economyManager.mascotState.collectAsState()
    val activeNotification by economyManager.activeNotification.collectAsState()
    val tasks by repository.allTasks.collectAsState(initial = emptyList())
    val notes by repository.allNotes.collectAsState(initial = emptyList())
    val studyGroups by repository.allStudyGroups.collectAsState(initial = emptyList())
    val userProfile by repository.userProfile.collectAsState(initial = null)
    val totalPomodoros by repository.totalPomodoros.collectAsState(initial = 0)
    val unfinishedTasksCount = tasks.count { !it.completed }

    val appGridItems = remember(unfinishedTasksCount, notes.size, studyGroups.size) {
        listOf(
            AppGridItem("Timer", StudyIcons.PomodoroTimer, Color(0xFFD9534F), "Focus", Color(0xFFD32F2F), "pomodoro"),
            AppGridItem("Tasks", StudyIcons.TasksGoals, Color(0xFF4CAF50), if (unfinishedTasksCount > 0) "$unfinishedTasksCount" else null, Color(0xFF2E7D32), "tasks_goals"),
            AppGridItem("Stocks", StudyIcons.StudyStocks, Color(0xFF20B2AA), "+14%", Color(0xFF00796B), "stocks"),
            AppGridItem("Flashcards", StudyIcons.Flashcards, Color(0xFF9C27B0), "Active", Color(0xFF7B1FA2), "flashcards"),
            AppGridItem("Notes OS", StudyIcons.Notes, Color(0xFFF5A623), "${notes.size}", Color(0xFFE65100), "notes"),
            AppGridItem("Analytics", StudyIcons.Analytics, Color(0xFF00BCD4), "Heatmap", Color(0xFF0097A7), "analytics"),
            AppGridItem("Fame Store", StudyIcons.FameStore, Color(0xFFFFD700), "Shop", Color(0xFFF57F17), "store"),
            AppGridItem("Leaderboard", StudyIcons.Leaderboard, Color(0xFF78909C), "Tiers", Color(0xFF455A64), "leaderboard"),
            AppGridItem("Study Squad", StudyIcons.StudyGroups, Color(0xFF3F51B5), "${studyGroups.size}", Color(0xFF283593), "groups"),
            AppGridItem("Profile", StudyIcons.Person, Color(0xFFE91E63), "VIP", Color(0xFFC2185B), "profile"),
            AppGridItem("Library", com.example.studyos.ui.library.LibraryIcons.Library, Color(0xFF8D6E63), "Quiet", Color(0xFF5D4037), "library"),
            AppGridItem("Settings", StudyIcons.Settings, Color(0xFF757575), null, Color.Gray, "settings")
        )
    }

    val currentPhase = themeManager.getCurrentPhase()
    val isNightMode = themeManager.isDarkThemeActive()
    val equippedTheme by com.example.studyos.core.EquipManager.equippedTheme.collectAsState(initial = null)
    val auroraTransition = rememberInfiniteTransition(label = "aurora")
    val auroraPhase by auroraTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(6000, easing = LinearEasing), RepeatMode.Restart),
        label = "auroraPhase"
    )

    val backgroundBrush = remember(currentPhase, isNightMode, equippedTheme, auroraPhase) {
        when {
            equippedTheme == "item_aurora_dream" -> {
                val shift = (sin(auroraPhase * 2.0 * Math.PI).toFloat() + 1f) / 2f
                Brush.verticalGradient(
                    listOf(
                        lerp(Color(0xFF0B1026), Color(0xFF123B4A), shift),
                        lerp(Color(0xFF1E6E5A), Color(0xFF3BA98B), shift),
                        Color(0xFF0B1026)
                    )
                )
            }
            equippedTheme == "item_math_matrix" -> Brush.verticalGradient(listOf(Color(0xFF003000), Color(0xFF001500)))
            equippedTheme == "item_spanish_fiesta" -> Brush.verticalGradient(listOf(Color(0xFFF5A623), Color(0xFFD9534F), Color(0xFF9C27B0)))
            isNightMode -> Brush.verticalGradient(listOf(Color(0xFF4A2C2C), Color(0xFF241515)))
            currentPhase == TimeOfDayPhase.MORNING_SUNRISE -> Brush.verticalGradient(listOf(Color(0xFFE8706C), Color(0xFFF5A623), Color(0xFFD9534F)))
            currentPhase == TimeOfDayPhase.EVENING_CORAL -> Brush.verticalGradient(listOf(Color(0xFFC94440), Color(0xFF4A2C2C)))
            else -> Brush.verticalGradient(listOf(Color(0xFFD9534F), Color(0xFFC94440)))
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(backgroundBrush)
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 18.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.clickable { onNavigateToRoute("profile") }
                ) {
                    Surface(
                        shape = CircleShape,
                        color = Color.White.copy(alpha = 0.22f),
                        modifier = Modifier.size(36.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = StudyIcons.Person,
                                contentDescription = "Profile",
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                    Column {
                        Text(
                            text = userProfile?.fullName?.takeIf { it.isNotBlank() } ?: "Scholar",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = Color.White
                        )
                        Text(
                            text = if (isNightMode) "Night Maroon Mode" else "Day Coral Mode",
                            fontSize = 11.sp,
                            color = Color.White.copy(alpha = 0.8f)
                        )
                    }
                }
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    IconButton(
                        onClick = { themeManager.toggleDarkMode() },
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.22f))
                    ) {
                        Icon(
                            imageVector = if (isNightMode) StudyIcons.LightMode else StudyIcons.DarkMode,
                            contentDescription = "Toggle Dark Mode",
                            tint = if (isNightMode) FameGold else Color.White,
                            modifier = Modifier.size(19.dp)
                        )
                    }
                    IconButton(
                        onClick = { onNavigateToRoute("settings") },
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.22f))
                    ) {
                        Icon(
                            imageVector = StudyIcons.Settings,
                            contentDescription = "Settings",
                            tint = Color.White,
                            modifier = Modifier.size(19.dp)
                        )
                    }
                }
            }

            StatusBarComposable(
                fame = totalFame,
                shame = totalShame,
                streakDays = streakDays,
                onStatusBarClick = { onNavigateToRoute("pomodoro") }
            )

            AnimatedVisibility(
                visible = activeNotification != null,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                if (activeNotification != null) {
                    Card(
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (activeNotification!!.urgencyLevel == "Savage") Color(0xFF330000) else Color(0xFF4A2800)
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                modifier = Modifier.weight(1f),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = StudyIcons.ShameDanger,
                                    contentDescription = "Alert",
                                    tint = if (activeNotification!!.urgencyLevel == "Savage") WarningRed else FameGold,
                                    modifier = Modifier.size(20.dp)
                                )
                                Column {
                                    Text(
                                        text = activeNotification!!.title,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp,
                                        color = Color.White
                                    )
                                    Text(
                                        text = activeNotification!!.message,
                                        fontSize = 12.sp,
                                        color = Color.White.copy(alpha = 0.9f)
                                    )
                                }
                            }
                            IconButton(onClick = { economyManager.dismissNotification() }) {
                                Icon(
                                    imageVector = StudyIcons.Close,
                                    contentDescription = "Dismiss",
                                    tint = Color.White,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }
            }

            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.18f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .customShimmer()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 18.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    InteractiveMascot(
                        state = mascotState,
                        size = 110.dp,
                        showArc = true,
                        progressArc = 0.85f
                    )
                    Spacer(modifier = Modifier.width(14.dp))
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = when (mascotState) {
                                MascotState.STUDYING -> "Deep Focus Active"
                                MascotState.STREAK -> "$streakDays-Day Streak On Fire!"
                                MascotState.WINNING -> "Champion Mode (+300 Fame)"
                                MascotState.HIGH_SHAME -> "Study now to eliminate Shame!"
                                MascotState.NIGHT_OWL -> "Night Owl Focus Ready"
                                else -> "Tap Mascot for Motivation!"
                            },
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = Color.White
                        )
                        Text(
                            text = "+2 Fame/min in active sessions",
                            fontSize = 12.sp,
                            color = FameGold,
                            fontWeight = FontWeight.SemiBold
                        )
                        Button(
                            onClick = { onNavigateToRoute("pomodoro") },
                            colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                            shape = RoundedCornerShape(18.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(42.dp)
                        ) {
                            Icon(
                                imageVector = StudyIcons.Play,
                                contentDescription = null,
                                tint = if (isNightMode) PrimaryNightMaroon else PrimaryCoral,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Start Pomodoro",
                                color = if (isNightMode) PrimaryNightMaroon else PrimaryCoral,
                                fontWeight = FontWeight.Black,
                                fontSize = 14.sp
                            )
                        }
                    }
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                QuickStatsWidget(
                    fameEarnedThisWeek = totalFame,
                    shameIncurredThisWeek = totalShame,
                    globalRank = 1,
                    streakDays = streakDays,
                    onClick = { onNavigateToRoute("analytics") }
                )
                ChallengeWidget(
                    challengeTitle = "Focus Milestone: Log 4 Pomodoro Sessions",
                    currentProgress = totalPomodoros,
                    targetGoal = 4,
                    rewardFame = 200,
                    deadlineText = "Daily Goal",
                    onClick = { onNavigateToRoute("pomodoro") }
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "STUDYOS APPLICATIONS",
                    color = Color.White.copy(alpha = 0.9f),
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    letterSpacing = 1.2.sp
                )
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceAround) {
                    appGridItems.take(4).forEach { item ->
                        AppIcon(
                            title = item.title,
                            icon = item.icon,
                            accentColor = item.color,
                            badgeText = item.badge,
                            badgeColor = item.badgeColor,
                            onClick = { onNavigateToRoute(item.route) }
                        )
                    }
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceAround) {
                    appGridItems.drop(4).take(4).forEach { item ->
                        AppIcon(
                            title = item.title,
                            icon = item.icon,
                            accentColor = item.color,
                            badgeText = item.badge,
                            badgeColor = item.badgeColor,
                            onClick = { onNavigateToRoute(item.route) }
                        )
                    }
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceAround) {
                    appGridItems.drop(8).take(4).forEach { item ->
                        AppIcon(
                            title = item.title,
                            icon = item.icon,
                            accentColor = item.color,
                            badgeText = item.badge,
                            badgeColor = item.badgeColor,
                            onClick = { onNavigateToRoute(item.route) }
                        )
                    }
                }
            }
        }
    }
}
