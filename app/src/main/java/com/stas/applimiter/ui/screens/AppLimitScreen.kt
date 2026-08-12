package com.stas.applimiter.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TimePicker
import androidx.compose.material3.TimePickerDefaults
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.stas.applimiter.data.local.DatabaseProvider
import com.stas.applimiter.data.local.entity.AppLimitEntity
import com.stas.applimiter.data.local.entity.AppScheduleEntity
import com.stas.applimiter.ui.components.AppCard
import com.stas.applimiter.ui.components.SectionLabel
import com.stas.applimiter.ui.theme.AppTheme
import kotlinx.coroutines.launch

private enum class LimitType { Duration, Schedule }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppLimitScreen(
    navController: NavController,
    packageName: String,
    appName: String,
) {
    val context = LocalContext.current
    val colors = AppTheme.colors

    val db = remember { DatabaseProvider.getDatabase(context) }
    val dao = remember { db.appLimitDao() }
    val scheduleDao = remember { db.appScheduleDao() }
    val scope = rememberCoroutineScope()

    val limits = dao.getAll().collectAsState(initial = emptyList()).value
    val schedules = scheduleDao.getAll().collectAsState(initial = emptyList()).value

    val currentLimit = limits.find { it.packageName == packageName }
    val currentSchedule = schedules.find { it.packageName == packageName }

    var limitType by remember {
        mutableStateOf(if (currentSchedule != null) LimitType.Schedule else LimitType.Duration)
    }

    // Duration state
    var hours by remember { mutableStateOf("") }
    var minutes by remember { mutableStateOf("") }

    LaunchedEffect(currentLimit) {
        if (currentLimit != null) {
            hours = (currentLimit.limitMinutes / 60).toString()
            minutes = (currentLimit.limitMinutes % 60).toString()
        } else if (currentSchedule == null) {
            hours = ""; minutes = ""
        }
    }

    // Schedule state
    val fromState = rememberTimePickerState(
        initialHour = currentSchedule?.let { it.allowFromMinutes / 60 } ?: 9,
        initialMinute = currentSchedule?.let { it.allowFromMinutes % 60 } ?: 0,
        is24Hour = true,
    )
    val untilState = rememberTimePickerState(
        initialHour = currentSchedule?.let { it.allowUntilMinutes / 60 } ?: 23,
        initialMinute = currentSchedule?.let { it.allowUntilMinutes % 60 } ?: 0,
        is24Hour = true,
    )

    val fieldColors = OutlinedTextFieldDefaults.colors(
        focusedContainerColor = colors.inputBg,
        unfocusedContainerColor = colors.inputBg,
        focusedBorderColor = colors.accent,
        unfocusedBorderColor = colors.border,
        focusedTextColor = colors.textPrimary,
        unfocusedTextColor = colors.textPrimary,
        focusedLabelColor = colors.textSecondary,
        unfocusedLabelColor = colors.textMuted,
        cursorColor = colors.accent,
    )

    val timePickerColors = TimePickerDefaults.colors(
        clockDialColor = colors.inputBg,
        clockDialSelectedContentColor = colors.onAccent,
        clockDialUnselectedContentColor = colors.textPrimary,
        selectorColor = colors.accent,
        containerColor = colors.card,
        timeSelectorSelectedContainerColor = colors.accent,
        timeSelectorUnselectedContainerColor = colors.inputBg,
        timeSelectorSelectedContentColor = colors.onAccent,
        timeSelectorUnselectedContentColor = colors.textPrimary,
        periodSelectorSelectedContainerColor = colors.accent,
        periodSelectorUnselectedContainerColor = colors.inputBg,
        periodSelectorSelectedContentColor = colors.onAccent,
        periodSelectorUnselectedContentColor = colors.textPrimary,
    )

    Scaffold(
        containerColor = colors.background,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Ограничения",
                        color = colors.textPrimary,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Назад",
                            tint = colors.textPrimary,
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = colors.headerBg,
                ),
            )
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(colors.background)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
        ) {
            AppCard {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = appName,
                        color = colors.textPrimary,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Text(
                        text = packageName,
                        color = colors.textMuted,
                        fontSize = 13.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            SectionLabel(text = "Тип ограничения")

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                FilterChip(
                    selected = limitType == LimitType.Duration,
                    onClick = { limitType = LimitType.Duration },
                    label = { Text("Лимит времени") },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = colors.accent,
                        selectedLabelColor = colors.onAccent,
                        containerColor = colors.chipInactiveBg,
                        labelColor = colors.textSecondary,
                    ),
                    border = FilterChipDefaults.filterChipBorder(
                        enabled = true,
                        selected = limitType == LimitType.Duration,
                        borderColor = colors.border,
                        selectedBorderColor = colors.accent,
                    ),
                )
                FilterChip(
                    selected = limitType == LimitType.Schedule,
                    onClick = { limitType = LimitType.Schedule },
                    label = { Text("Расписание") },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = colors.accent,
                        selectedLabelColor = colors.onAccent,
                        containerColor = colors.chipInactiveBg,
                        labelColor = colors.textSecondary,
                    ),
                    border = FilterChipDefaults.filterChipBorder(
                        enabled = true,
                        selected = limitType == LimitType.Schedule,
                        borderColor = colors.border,
                        selectedBorderColor = colors.accent,
                    ),
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            when (limitType) {
                LimitType.Duration -> {
                    SectionLabel(text = "Максимальное время в день")
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        OutlinedTextField(
                            value = hours,
                            onValueChange = { hours = it.filter { ch -> ch.isDigit() } },
                            label = { Text("Часы") },
                            modifier = Modifier.weight(1f),
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            colors = fieldColors,
                        )
                        OutlinedTextField(
                            value = minutes,
                            onValueChange = { minutes = it.filter { ch -> ch.isDigit() } },
                            label = { Text("Минуты") },
                            modifier = Modifier.weight(1f),
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            colors = fieldColors,
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Приложение закроется, как только суммарное время за день достигнет лимита.",
                        color = colors.textSecondary,
                        fontSize = 13.sp,
                        lineHeight = 18.sp,
                    )
                }

                LimitType.Schedule -> {
                    SectionLabel(text = "Доступно с")
                    TimePicker(
                        state = fromState,
                        colors = timePickerColors,
                        modifier = Modifier.fillMaxWidth(),
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    SectionLabel(text = "Доступно до")
                    TimePicker(
                        state = untilState,
                        colors = timePickerColors,
                        modifier = Modifier.fillMaxWidth(),
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Приложение закроется при открытии вне указанного окна. Поддерживается ночной диапазон (например, 21:00 – 01:00).",
                        color = colors.textSecondary,
                        fontSize = 13.sp,
                        lineHeight = 18.sp,
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Button(
                onClick = {
                    scope.launch {
                        when (limitType) {
                            LimitType.Duration -> {
                                val norm = (minutes.toLongOrNull() ?: 0L).coerceIn(0, 59)
                                val total = (hours.toLongOrNull() ?: 0L) * 60 + norm
                                dao.insert(
                                    AppLimitEntity(
                                        packageName = packageName,
                                        appName = appName,
                                        limitMinutes = total,
                                    )
                                )
                                scheduleDao.delete(packageName)
                            }
                            LimitType.Schedule -> {
                                scheduleDao.insert(
                                    AppScheduleEntity(
                                        packageName = packageName,
                                        appName = appName,
                                        allowFromMinutes = fromState.hour * 60 + fromState.minute,
                                        allowUntilMinutes = untilState.hour * 60 + untilState.minute,
                                    )
                                )
                                dao.delete(packageName)
                            }
                        }
                    }
                    navController.popBackStack()
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = colors.accent,
                    contentColor = colors.onAccent,
                ),
            ) {
                Text("Сохранить", fontWeight = FontWeight.SemiBold)
            }

            Spacer(modifier = Modifier.height(10.dp))

            OutlinedButton(
                onClick = {
                    scope.launch {
                        dao.delete(packageName)
                        scheduleDao.delete(packageName)
                    }
                    navController.popBackStack()
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = colors.danger,
                ),
                border = androidx.compose.foundation.BorderStroke(1.dp, colors.danger),
            ) {
                Text("Снять все ограничения", fontWeight = FontWeight.SemiBold)
            }
        }
    }
}
