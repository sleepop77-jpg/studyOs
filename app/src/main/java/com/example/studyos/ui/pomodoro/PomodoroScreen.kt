package com.example.studyos.ui.pomodoro

import android.content.Context
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.studyos.core.EconomyManager
import com.example.studyos.core.MascotState
import com.example.studyos.core.NotificationHelper
import com.example.studyos.core.TimeBasedThemeManager
import com.example.studyos.core.FocusTimerManager
import com.example.studyos.core.TimerMode
import com.example.studyos.data.local.entities.Subject
import com.example.studyos.data.repository.StudyRepository
import com.example.studyos.ui.common.StudyIcons
import com.example.studyos.ui.common.ConfettiRain
import com.example.studyos.ui.launcher.InteractiveMascot
import com.example.studyos.ui.theme.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PomodoroScreen(
    repository: StudyRepository,
    economyManager: EconomyManager,
    timerManager: FocusTimerManager,
    themeManager: TimeBasedThemeManager,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val coroutineScope = rememberCoroutineScope()
    val totalSessions by repository.totalPomodoros.collectAsState(initial = 0)
    val todaySessions by remember {
        val startOfDay = java.util.Calendar.getInstance().apply {
            set(java.util.Calendar.HOUR_OF_DAY, 0)
            set(java.util.Calendar.MINUTE, 0)
            set(java.util.Calendar.SECOND, 0)
        }.timeInMillis
        repository.getTodayPomodoros(startOfDay)
    }.collectAsState(initial = 0)
    val subjects by repository.allSubjects.collectAsState(initial = emptyList())
    val selectedSubject by timerManager.selectedSubject.collectAsState()
    var showSubjectDropdown by remember { mutableStateOf(false) }
    var showAddSubjectDialog by remember { mutableStateOf(false) }
    val timerMode by timerManager.timerMode.collectAsState()
    var showExamLockedDialog by remember { mutableStateOf(false) }
    var isFullscreen by remember { mutableStateOf(false) }
    var showCustomDurationDialog by remember { mutableStateOf(false) }
    val celebrationMessage by timerManager.celebrationMessage.collectAsState()
    val customDurationMinutes by timerManager.customDurationMinutes.collectAsState()
    val currentTotalDurationSeconds by timerManager.currentTotalDurationSeconds.collectAsState()
    val secondsRemaining by timerManager.secondsRemaining.collectAsState()
    val isRunning by timerManager.isRunning.collectAsState()
    val currentRound by timerManager.currentRound.collectAsState()
    val totalRounds = timerManager.totalRounds
    val goalSessions = timerManager.goalSessions
    val continuousStudySeconds by economyManager.continuousStudySeconds.collectAsState()
    val isBurning = continuousStudySeconds >= 10800
    val isNightMode = themeManager.isDarkThemeActive()

    LaunchedEffect(subjects) {
        if (subjects.isNotEmpty() && subjects.none { it.name.equals(selectedSubject, ignoreCase = true) }) {
            timerManager.setSubject(subjects.first().name)
        }
    }

    val progressArc = if (currentTotalDurationSeconds > 0) {
        (secondsRemaining.toFloat() / currentTotalDurationSeconds.toFloat()).coerceIn(0f, 1f)
    } else 1f

    val minutesPart = secondsRemaining / 60
    val secondsPart = secondsRemaining % 60
    val formattedTime = String.format("%02d:%02d", minutesPart, secondsPart)

    val backgroundBrush = remember(isNightMode, isBurning) {
        when {
            isBurning -> Brush.verticalGradient(listOf(Color(0xFF8B1E0F), Color(0xFF4A0E07), Color(0xFF240703)))
            isNightMode -> Brush.verticalGradient(listOf(Color(0xFF4A2C2C), Color(0xFF241515)))
            else -> Brush.verticalGradient(listOf(Color(0xFFD9534F), Color(0xFFC94440)))
        }
    }

    if (showAddSubjectDialog) {
        AddSubjectDialog(
            onDismiss = { showAddSubjectDialog = false },
            onAddSubject = { name, colorHex ->
                coroutineScope.launch {
                    val newSubject = Subject(
                        id = name.lowercase().replace(" ", "_"),
                        name = name,
                        masteryPercent = 0,
                        studyHoursTotal = 0f,
                        cardsReviewed = 0,
                        cardsCorrect = 0,
                        colorHex = colorHex
                    )
                    repository.insertSubject(newSubject)
                    timerManager.setSubject(name)
                    showAddSubjectDialog = false
                }
            }
        )
    }

    if (showExamLockedDialog) {
        AlertDialog(
            onDismissRequest = { showExamLockedDialog = false },
            icon = {
                Icon(
                    imageVector = StudyIcons.Lock,
                    contentDescription = "Locked",
                    tint = FameGold,
                    modifier = Modifier.size(36.dp)
                )
            },
            title = {
                Text(
                    text = "Exam Mode Locked",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = OnSurfaceDark
                )
            },
            text = {
                Text(
                    text = "Exam Mode is locked. It unlocks exclusively during Midterm, Finals, and Campus Study Tournaments to deliver high-stakes double fame multipliers!",
                    color = OnSurfaceMuted,
                    fontSize = 14.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = { showExamLockedDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryCoral)
                ) {
                    Text("Got It")
                }
            },
            containerColor = SurfaceCream,
            shape = RoundedCornerShape(20.dp)
        )
    }

    if (showCustomDurationDialog) {
        var inputMinutesText by remember { mutableStateOf(customDurationMinutes.toString()) }
        AlertDialog(
            onDismissRequest = { showCustomDurationDialog = false },
            title = {
                Text(
                    text = "Set Custom Timer Duration",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = OnSurfaceDark
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "Enter any study duration in minutes (1 - 480 mins):",
                        fontSize = 13.sp,
                        color = OnSurfaceMuted
                    )
                    OutlinedTextField(
                        value = inputMinutesText,
                        onValueChange = { inputMinutesText = it.filter { ch -> ch.isDigit() }.take(3) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    )
                    if (inputMinutesText.toIntOrNull() != null && inputMinutesText.toInt() >= 60) {
                        Text(
                            text = "⚡ 1h+ Timers qualify for +2.5 Fame/min in Loop Mode!",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = FameGold
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val mins = inputMinutesText.toIntOrNull() ?: 25
                        timerManager.setDurationMinutes(mins)
                        showCustomDurationDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryCoral)
                ) {
                    Text("Set Timer")
                }
            },
            dismissButton = {
                TextButton(onClick = { showCustomDurationDialog = false }) {
                    Text("Cancel", color = OnSurfaceMuted)
                }
            },
            containerColor = SurfaceCream,
            shape = RoundedCornerShape(20.dp)
        )
    }

    if (isFullscreen) {
        Box(
            modifier = modifier
                .fillMaxSize()
                .background(backgroundBrush)
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = Color.White.copy(alpha = 0.2f),
                        modifier = Modifier.padding(4.dp)
                    ) {
                        Text(
                            text = "ZEN FOCUS · $selectedSubject",
                            color = Color.White,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                        )
                    }
                    IconButton(
                        onClick = { isFullscreen = false },
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.2f))
                    ) {
                        Icon(
                            imageVector = StudyIcons.FullscreenExit,
                            contentDescription = "Exit Fullscreen",
                            tint = Color.White
                        )
                    }
                }
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.size(260.dp)
                ) {
                    InteractiveMascot(
                        state = if (isBurning) MascotState.BURNING else if (isRunning) MascotState.STUDYING else MascotState.IDLE,
                        size = 250.dp,
                        showArc = true,
                        progressArc = progressArc
                    )
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = formattedTime,
                        color = Color.White,
                        fontSize = 64.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 3.sp
                    )
                    if (isBurning) {
                        Text(
                            text = "🔥 3+ HOURS CONTINUOUS OVERDRIVE (+100 FAME CLAIMED)",
                            color = FameGold,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Black
                        )
                    }
                }
                Row(
                    horizontalArrangement = Arrangement.spacedBy(20.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = { timerManager.adjustMinutes(-5) },
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.2f))
                    ) {
                        Text("-5m", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                    IconButton(
                        onClick = { timerManager.toggle() },
                        modifier = Modifier
                            .size(72.dp)
                            .clip(CircleShape)
                            .background(Color.White)
                    ) {
                        Icon(
                            imageVector = if (isRunning) StudyIcons.Pause else StudyIcons.Play,
                            contentDescription = if (isRunning) "Pause" else "Play",
                            tint = if (isBurning) Color(0xFF8B1E0F) else if (isNightMode) PrimaryNightMaroon else PrimaryCoral,
                            modifier = Modifier.size(40.dp)
                        )
                    }
                    IconButton(
                        onClick = { timerManager.adjustMinutes(+5) },
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.2f))
                    ) {
                        Text("+5m", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                }
            }
        }
        return
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
                .padding(horizontal = 20.dp, vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onNavigateBack,
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.2f))
                ) {
                    Icon(
                        imageVector = StudyIcons.Back,
                        contentDescription = "Back",
                        tint = Color.White
                    )
                }
                Box {
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = Color.White.copy(alpha = 0.22f),
                        modifier = Modifier.clickable { showSubjectDropdown = true }
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 7.dp)
                        ) {
                            Text(
                                text = selectedSubject,
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                            Icon(
                                imageVector = StudyIcons.ExpandMore,
                                contentDescription = "Select Subject",
                                tint = Color.White,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                    DropdownMenu(
                        expanded = showSubjectDropdown,
                        onDismissRequest = { showSubjectDropdown = false },
                        modifier = Modifier.background(SurfaceCream)
                    ) {
                        val availableSubjects = if (subjects.isNotEmpty()) subjects.map { it.name } else listOf("Mathematics", "Computer Science", "Spanish Language", "Quantum Physics", "World History")
                        availableSubjects.forEach { sub ->
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        text = sub,
                                        fontWeight = if (sub == selectedSubject) FontWeight.Bold else FontWeight.Normal,
                                        color = OnSurfaceDark
                                    )
                                },
                                onClick = {
                                    timerManager.setSubject(sub)
                                    showSubjectDropdown = false
                                }
                            )
                        }
                        HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                        DropdownMenuItem(
                            text = {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(
                                        imageVector = StudyIcons.Add,
                                        contentDescription = null,
                                        tint = PrimaryCoral,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Text(
                                        text = "+ Add New Subject",
                                        fontWeight = FontWeight.Bold,
                                        color = PrimaryCoral
                                    )
                                }
                            },
                            onClick = {
                                showSubjectDropdown = false
                                showAddSubjectDialog = true
                            }
                        )
                    }
                }
                Surface(
                    onClick = { showExamLockedDialog = true },
                    shape = RoundedCornerShape(20.dp),
                    color = Color.White.copy(alpha = 0.15f),
                    modifier = Modifier.height(38.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        modifier = Modifier.padding(horizontal = 10.dp)
                    ) {
                        Icon(
                            imageVector = StudyIcons.Lock,
                            contentDescription = "Locked Exam Mode",
                            tint = FameGold,
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = "Exam Mode",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White.copy(alpha = 0.9f)
                        )
                    }
                }
            }

            AnimatedVisibility(visible = celebrationMessage != null) {
                celebrationMessage?.let { msg ->
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = FameGold),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 14.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = msg,
                                color = PrimaryNightMaroon,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                modifier = Modifier.weight(1f)
                            )
                            IconButton(
                                onClick = { timerManager.clearCelebration() },
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(
                                    imageVector = StudyIcons.Close,
                                    contentDescription = "Dismiss",
                                    tint = PrimaryNightMaroon,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }
            }

            if (celebrationMessage != null) {
                ConfettiRain(
                    colors = listOf(0xFFFFD700, 0xFFD9534F, 0xFF4CAF50, 0xFF20B2AA, 0xFFFFF9C4),
                    active = true
                )
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                StatColumn(label = "Rounds", value = "$currentRound/$totalRounds", sub = "Sessions")
                StatColumn(label = "Goals", value = "$todaySessions/$goalSessions", sub = "Sessions")
                StatColumn(label = "Today", value = "$todaySessions", sub = "Sessions")
                StatColumn(label = "Lifetime", value = "$totalSessions", sub = "Sessions")
            }

            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(230.dp)
                    .padding(4.dp)
            ) {
                InteractiveMascot(
                    state = if (isBurning) MascotState.BURNING else if (isRunning) MascotState.STUDYING else MascotState.IDLE,
                    size = 220.dp,
                    showArc = true,
                    progressArc = progressArc
                )
            }

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.clickable { showCustomDurationDialog = true }
            ) {
                Text(
                    text = formattedTime,
                    color = Color.White,
                    fontSize = 48.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 2.sp,
                    textAlign = TextAlign.Center
                )
                Text(
                    text = "Tap to set custom duration (${customDurationMinutes}m)",
                    color = Color.White.copy(alpha = 0.8f),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            if (isBurning) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFF3E120A),
                    modifier = Modifier.padding(top = 2.dp)
                ) {
                    Text(
                        text = "🔥 3-Hour Burning Mode Active: +100 Overdrive Fame Claimed!",
                        color = FameGold,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Black,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                    )
                }
            } else if (timerMode == TimerMode.LOOP && customDurationMinutes >= 60) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = FameGold.copy(alpha = 0.25f),
                    modifier = Modifier.padding(top = 2.dp)
                ) {
                    Text(
                        text = "⚡ 1h+ Loop Active: +2.5 Fame/min on cycle completion!",
                        color = FameGold,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
            }

            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                TimeAdjustChip(label = "-5m") { timerManager.adjustMinutes(-5) }
                TimeAdjustChip(label = "-1m") { timerManager.adjustMinutes(-1) }
                TimeAdjustChip(label = "+1m") { timerManager.adjustMinutes(+1) }
                TimeAdjustChip(label = "+5m") { timerManager.adjustMinutes(+5) }
            }

            Row(
                horizontalArrangement = Arrangement.spacedBy(28.dp),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(vertical = 4.dp)
            ) {
                IconButton(
                    onClick = { isFullscreen = true },
                    modifier = Modifier.size(46.dp)
                ) {
                    Icon(
                        imageVector = StudyIcons.Fullscreen,
                        contentDescription = "Fullscreen Zen Mode",
                        tint = Color.White.copy(alpha = 0.9f),
                        modifier = Modifier.size(28.dp)
                    )
                }
                IconButton(
                    onClick = { timerManager.toggle() },
                    modifier = Modifier
                        .size(64.dp)
                        .clip(CircleShape)
                        .background(Color.White)
                ) {
                    Icon(
                        imageVector = if (isRunning) StudyIcons.Pause else StudyIcons.Play,
                        contentDescription = if (isRunning) "Pause" else "Play",
                        tint = if (isBurning) Color(0xFF8B1E0F) else if (isNightMode) PrimaryNightMaroon else PrimaryCoral,
                        modifier = Modifier.size(36.dp)
                    )
                }
                IconButton(
                    onClick = { timerManager.reset() },
                    modifier = Modifier.size(46.dp)
                ) {
                    Icon(
                        imageVector = StudyIcons.Reset,
                        contentDescription = "Reset Timer",
                        tint = Color.White.copy(alpha = 0.9f),
                        modifier = Modifier.size(28.dp)
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                listOf(15, 25, 45, 60, 90, 180).forEach { mins ->
                    val isSelected = customDurationMinutes == mins
                    Surface(
                        onClick = { timerManager.setDurationMinutes(mins) },
                        shape = RoundedCornerShape(14.dp),
                        color = if (isSelected) Color.White else Color.White.copy(alpha = 0.18f),
                        modifier = Modifier.height(34.dp)
                    ) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier.padding(horizontal = 10.dp)
                        ) {
                            Text(
                                text = "${mins}m",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isSelected) PrimaryCoralDark else Color.White
                            )
                        }
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                TimerModeCard(
                    title = "Standard Timer",
                    subtitle = "One session ($customDurationMinutes mins)",
                    selected = timerMode == TimerMode.STANDARD,
                    modifier = Modifier.weight(1f),
                    onClick = {
                        timerManager.setTimerMode(TimerMode.STANDARD)
                    }
                )
                TimerModeCard(
                    title = "Loop Mode",
                    subtitle = if (customDurationMinutes >= 60) "Continuous (2.5 Fame/min ⚡)" else "Continuous loops",
                    selected = timerMode == TimerMode.LOOP,
                    modifier = Modifier.weight(1f),
                    onClick = {
                        timerManager.setTimerMode(TimerMode.LOOP)
                    }
                )
            }
        }
    }
}

@Composable
private fun TimeAdjustChip(label: String, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(12.dp),
        color = Color.White.copy(alpha = 0.2f),
        modifier = Modifier.height(32.dp)
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.padding(horizontal = 12.dp)
        ) {
            Text(
                text = label,
                color = Color.White,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun StatColumn(label: String, value: String, sub: String) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        Text(
            text = label,
            fontSize = 11.sp,
            color = Color.White.copy(alpha = 0.85f),
            fontWeight = FontWeight.Medium
        )
        Text(
            text = value,
            fontSize = 15.sp,
            color = Color.White,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = sub,
            fontSize = 9.sp,
            color = Color.White.copy(alpha = 0.7f)
        )
    }
}

@Composable
private fun TimerModeCard(
    title: String,
    subtitle: String,
    selected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(20.dp),
        color = if (selected) Color.White else Color.White.copy(alpha = 0.15f),
        modifier = modifier.height(64.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = if (selected) PrimaryCoralDark else Color.White
                )
                Text(
                    text = subtitle,
                    fontSize = 11.sp,
                    color = if (selected) OnSurfaceMuted else Color.White.copy(alpha = 0.75f)
                )
            }
            if (selected) {
                Surface(
                    shape = CircleShape,
                    color = PrimaryCoral,
                    modifier = Modifier.size(20.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = StudyIcons.Check,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(12.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun AddSubjectDialog(
    onDismiss: () -> Unit,
    onAddSubject: (name: String, colorHex: String) -> Unit
) {
    var subjectName by remember { mutableStateOf("") }
    val colorOptions = listOf("#D9534F", "#00BCD4", "#4CAF50", "#9C27B0", "#F5A623", "#20B2AA")
    var selectedColor by remember { mutableStateOf(colorOptions[0]) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Add Custom Subject",
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = OnSurfaceDark
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                Text(
                    text = "Create a new course or subject to track your study sessions and mastery:",
                    fontSize = 13.sp,
                    color = OnSurfaceMuted
                )
                OutlinedTextField(
                    value = subjectName,
                    onValueChange = { subjectName = it },
                    label = { Text("Subject Name") },
                    placeholder = { Text("e.g. Organic Chemistry, Macroeconomics") },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )
                Text(
                    text = "Select Accent Color:",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = OnSurfaceDark
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    colorOptions.forEach { hex ->
                        val color = Color(android.graphics.Color.parseColor(hex))
                        val isSelected = selectedColor == hex
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(CircleShape)
                                .background(color)
                                .clickable { selectedColor = hex },
                            contentAlignment = Alignment.Center
                        ) {
                            if (isSelected) {
                                Icon(
                                    imageVector = StudyIcons.Check,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (subjectName.isNotBlank()) {
                        onAddSubject(subjectName.trim(), selectedColor)
                    }
                },
                enabled = subjectName.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryCoral)
            ) {
                Text("Save Subject")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = OnSurfaceMuted)
            }
        },
        containerColor = SurfaceCream,
        shape = RoundedCornerShape(20.dp)
    )
}
