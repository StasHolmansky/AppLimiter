package com.stas.applimiter.ui.screens

import android.Manifest
import android.content.Intent
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.navigation.NavController
import com.stas.applimiter.data.local.DatabaseProvider
import com.stas.applimiter.data.local.entity.AppScheduleEntity
import com.stas.applimiter.data.model.InstalledApp
import com.stas.applimiter.data.repository.UsageStatsRepository
import com.stas.applimiter.navigation.encodeNavArg
import com.stas.applimiter.service.UsageMonitorService
import com.stas.applimiter.ui.components.AppCard
import com.stas.applimiter.ui.theme.AppTheme
import com.stas.applimiter.ui.utils.toBitmap
import com.stas.applimiter.utils.formatMinutes
import com.stas.applimiter.utils.formatWindow
import com.stas.applimiter.utils.hasAppBlockAccessibilityPermission
import com.stas.applimiter.utils.hasUsageStatsPermission
import com.stas.applimiter.utils.isAllowedNow
import com.stas.applimiter.utils.launchablePackageNames

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    navController: NavController,
    bankSafeMode: Boolean = false,
    onBankSafeModeChange: (Boolean) -> Unit = {},
) {
    val context = LocalContext.current
    val colors = AppTheme.colors

    val isMonitoring by UsageMonitorService.isRunning.collectAsState()

    var hasUsageAccess by remember {
        mutableStateOf(hasUsageStatsPermission(context))
    }

    var hasAccessibilityAccess by remember {
        mutableStateOf(hasAppBlockAccessibilityPermission(context))
    }

    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
    ) { }

    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    val db = remember { DatabaseProvider.getDatabase(context) }
    val dao = remember { db.appLimitDao() }
    val scheduleDao = remember { db.appScheduleDao() }

    val savedLimits = dao.getAll().collectAsState(initial = emptyList()).value
    val savedSchedules = scheduleDao.getAll().collectAsState(initial = emptyList()).value

    val lifecycleOwner = LocalLifecycleOwner.current
    var usageRefreshKey by remember { mutableStateOf(0) }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                hasUsageAccess = hasUsageStatsPermission(context)
                hasAccessibilityAccess = hasAppBlockAccessibilityPermission(context)
                usageRefreshKey++
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    val packageManager = context.packageManager
    val usageRepository = remember { UsageStatsRepository(context) }

    val apps = remember {
        packageManager
            .launchablePackageNames()
            .filter { it != context.packageName }
            .mapNotNull { packageName ->
                try {
                    val info = packageManager.getApplicationInfo(packageName, 0)
                    InstalledApp(
                        appName = packageManager.getApplicationLabel(info).toString(),
                        packageName = packageName,
                        icon = packageManager.getApplicationIcon(info),
                    )
                } catch (_: Exception) {
                    null
                }
            }
            .sortedBy { it.appName.lowercase() }
    }

    val usageMap = remember(hasUsageAccess, usageRefreshKey) {
        if (hasUsageAccess) usageRepository.getTodayUsage() else emptyMap()
    }

    var searchText by remember { mutableStateOf("") }

    val filteredApps = apps.filter {
        it.appName.contains(searchText, ignoreCase = true) ||
            it.packageName.contains(searchText, ignoreCase = true)
    }

    val scheduledPackages = savedSchedules.map { it.packageName }.toSet()

    val sortedApps = filteredApps.sortedWith(
        compareByDescending<InstalledApp> { it.packageName in scheduledPackages || savedLimits.any { l -> l.packageName == it.packageName } }
            .thenByDescending { usageMap[it.packageName] ?: 0L }
    )

    val canMonitor = hasUsageAccess && hasAccessibilityAccess && !bankSafeMode

    Scaffold(
        containerColor = colors.background,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "AppLimiter",
                        color = colors.textPrimary,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                },
                actions = {
                    IconButton(onClick = { navController.navigate("settings") }) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Настройки",
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
                .background(colors.background),
        ) {
            BankSafeCard(
                enabled = bankSafeMode,
                onToggle = { on ->
                    if (on && isMonitoring) UsageMonitorService.stop(context)
                    onBankSafeModeChange(on)
                },
                onOpenAccessibility = {
                    context.startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
            )

            if (!bankSafeMode) {
                MonitoringToggle(
                    isMonitoring = isMonitoring,
                    enabled = canMonitor,
                    onToggle = { enabled ->
                        if (enabled) {
                            hasUsageAccess = hasUsageStatsPermission(context)
                            hasAccessibilityAccess = hasAppBlockAccessibilityPermission(context)
                            if (hasUsageAccess && hasAccessibilityAccess) {
                                UsageMonitorService.start(context)
                            }
                        } else {
                            UsageMonitorService.stop(context)
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp),
                )

                if (!hasUsageAccess) {
                    PermissionCard(
                        text = "Для работы мониторинга необходим доступ к статистике использования.",
                        buttonText = "Предоставить доступ",
                        onClick = {
                            context.startActivity(Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS))
                        },
                    )
                }

                if (!hasAccessibilityAccess) {
                    PermissionCard(
                        text = "Чтобы закрывать приложения после исчерпания лимита, " +
                            "включите AppLimiter в специальных возможностях.",
                        buttonText = "Включить блокировку",
                        onClick = {
                            context.startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
                        },
                    )
                }
            }

            OutlinedTextField(
                value = searchText,
                onValueChange = { searchText = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                label = { Text("Поиск приложения") },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = colors.inputBg,
                    unfocusedContainerColor = colors.inputBg,
                    focusedBorderColor = colors.accent,
                    unfocusedBorderColor = colors.border,
                    focusedTextColor = colors.textPrimary,
                    unfocusedTextColor = colors.textPrimary,
                    focusedLabelColor = colors.textSecondary,
                    unfocusedLabelColor = colors.textMuted,
                    cursorColor = colors.accent,
                ),
            )

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                items(sortedApps) { app ->
                    AppRow(
                        app = app,
                        usageMillis = usageMap[app.packageName] ?: 0L,
                        limitMinutes = savedLimits
                            .find { it.packageName == app.packageName }
                            ?.limitMinutes,
                        schedule = savedSchedules.find { it.packageName == app.packageName },
                        onClick = {
                            navController.navigate(
                                "appLimit/${app.packageName}/${encodeNavArg(app.appName)}",
                            )
                        },
                    )
                }
            }
        }
    }
}

@Composable
private fun BankSafeCard(
    enabled: Boolean,
    onToggle: (Boolean) -> Unit,
    onOpenAccessibility: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = AppTheme.colors
    AppCard(modifier = modifier) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Icon(
                    imageVector = Icons.Default.AccountBalance,
                    contentDescription = null,
                    tint = if (enabled) colors.accent else colors.textMuted,
                    modifier = Modifier.size(22.dp),
                )
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Банковский режим",
                        color = colors.textPrimary,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                    )
                    Text(
                        text = if (enabled)
                            "Мониторинг приостановлен — банковские приложения работают в штатном режиме"
                        else
                            "Включите перед запуском банковского приложения",
                        color = colors.textSecondary,
                        fontSize = 13.sp,
                        lineHeight = 18.sp,
                    )
                }
                Switch(
                    checked = enabled,
                    onCheckedChange = onToggle,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = colors.onAccent,
                        checkedTrackColor = colors.accent,
                        uncheckedThumbColor = colors.card,
                        uncheckedTrackColor = colors.chipInactiveBg,
                    ),
                )
            }
            if (enabled) {
                Button(
                    onClick = onOpenAccessibility,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = colors.accent,
                        contentColor = colors.onAccent,
                    ),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text("Открыть Специальные возможности", fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}

@Composable
private fun MonitoringToggle(
    isMonitoring: Boolean,
    enabled: Boolean,
    onToggle: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = AppTheme.colors
    AppCard(modifier = modifier) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = if (isMonitoring) "Мониторинг активен" else "Мониторинг остановлен",
                    color = colors.textPrimary,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                )
                if (!enabled) {
                    Text(
                        text = "Предоставьте необходимые разрешения ниже",
                        color = colors.textSecondary,
                        fontSize = 13.sp,
                    )
                }
            }
            Switch(
                checked = isMonitoring,
                onCheckedChange = onToggle,
                enabled = enabled || isMonitoring,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = colors.onAccent,
                    checkedTrackColor = colors.accent,
                    uncheckedThumbColor = colors.card,
                    uncheckedTrackColor = colors.chipInactiveBg,
                ),
            )
        }
    }
}

@Composable
private fun PermissionCard(
    text: String,
    buttonText: String,
    onClick: () -> Unit,
) {
    val colors = AppTheme.colors
    AppCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(
                text = text,
                color = colors.textPrimary,
                fontSize = 15.sp,
                lineHeight = 20.sp,
            )
            Button(
                onClick = onClick,
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = colors.accent,
                    contentColor = colors.onAccent,
                ),
            ) {
                Text(buttonText, fontWeight = FontWeight.SemiBold)
            }
        }
    }
}

@Composable
private fun AppRow(
    app: InstalledApp,
    usageMillis: Long,
    limitMinutes: Long?,
    schedule: AppScheduleEntity?,
    onClick: () -> Unit,
) {
    val colors = AppTheme.colors
    AppCard(onClick = onClick) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Image(
                bitmap = app.icon.toBitmap().asImageBitmap(),
                contentDescription = app.appName,
                modifier = Modifier.size(48.dp),
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = app.appName,
                    color = colors.textPrimary,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = app.packageName,
                    color = colors.textMuted,
                    fontSize = 12.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                val minutes = usageMillis / 1000 / 60
                Text(
                    text = "Сегодня: ${formatMinutes(minutes)}",
                    color = colors.textSecondary,
                    fontSize = 13.sp,
                )
                when {
                    schedule != null -> {
                        val allowed = schedule.isAllowedNow()
                        Text(
                            text = "Расписание: ${schedule.formatWindow()}",
                            color = if (allowed) colors.accent else colors.danger,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                        )
                        if (!allowed) {
                            Text(
                                text = "Сейчас недоступно",
                                color = colors.danger,
                                fontSize = 12.sp,
                            )
                        }
                    }
                    limitMinutes != null -> {
                        Text(
                            text = "Лимит: ${formatMinutes(limitMinutes)}",
                            color = colors.accent,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                        )
                    }
                }
            }
        }
    }
}
