package com.stas.applimiter.ui.screens

import android.content.Intent
import android.content.pm.PackageManager
import android.provider.Settings
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccountBalance
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.stas.applimiter.config.Release
import com.stas.applimiter.ui.components.AppCard
import com.stas.applimiter.ui.components.FeedbackNavRow
import com.stas.applimiter.ui.components.SectionLabel
import com.stas.applimiter.ui.components.ThemeChipRow
import com.stas.applimiter.ui.theme.AppTheme
import com.stas.applimiter.ui.theme.ThemePreference

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    navController: NavController,
    preference: ThemePreference,
    onPreferenceChange: (ThemePreference) -> Unit,
    bankSafeMode: Boolean = false,
    onBankSafeModeChange: (Boolean) -> Unit = {},
) {
    val context = LocalContext.current
    val colors = AppTheme.colors
    val versionLabel = rememberVersionLabel()

    Scaffold(
        containerColor = colors.background,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Настройки",
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
            SectionLabel(text = "Оформление")

            val options = listOf(
                "Системная" to (preference == ThemePreference.System),
                "Светлая" to (preference == ThemePreference.Light),
                "Тёмная" to (preference == ThemePreference.Dark),
            )
            ThemeChipRow(
                options = options,
                onSelect = { index ->
                    onPreferenceChange(
                        when (index) {
                            1 -> ThemePreference.Light
                            2 -> ThemePreference.Dark
                            else -> ThemePreference.System
                        }
                    )
                },
            )

            Text(
                text = "Системная следует режиму Android.",
                color = colors.textSecondary,
                fontSize = 13.sp,
                modifier = Modifier.padding(top = 8.dp),
            )

            Spacer(modifier = Modifier.height(24.dp))

            SectionLabel(text = "Совместимость")
            AppCard(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Icon(
                        imageVector = Icons.Default.AccountBalance,
                        contentDescription = null,
                        tint = if (bankSafeMode) colors.accent else colors.textMuted,
                        modifier = Modifier.size(22.dp),
                    )
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Банковский режим",
                            color = colors.textPrimary,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold,
                        )
                        Text(
                            text = "Отключает мониторинг и специальные возможности — банковские приложения работают корректно",
                            color = colors.textSecondary,
                            fontSize = 13.sp,
                            lineHeight = 18.sp,
                        )
                    }
                    Switch(
                        checked = bankSafeMode,
                        onCheckedChange = { on ->
                            onBankSafeModeChange(on)
                            if (on) {
                                context.startActivity(
                                    Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
                                )
                            }
                        },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = colors.onAccent,
                            checkedTrackColor = colors.accent,
                            uncheckedThumbColor = colors.card,
                            uncheckedTrackColor = colors.chipInactiveBg,
                        ),
                    )
                }
            }
            Text(
                text = "При включении откроются Специальные возможности — отключите там AppLimiter.",
                color = colors.textSecondary,
                fontSize = 13.sp,
                modifier = Modifier.padding(top = 6.dp),
                lineHeight = 18.sp,
            )

            Spacer(modifier = Modifier.height(24.dp))

            SectionLabel(text = "Поддержка")
            FeedbackNavRow(
                title = "Обратная связь и предложения",
                hint = "Идеи, пожелания или проблемы можно отправить разработчику.",
                onClick = { navController.navigate("feedback") },
            )

            Spacer(modifier = Modifier.height(28.dp))

            Text(
                text = "${Release.APP_DISPLAY_NAME} $versionLabel",
                color = colors.textMuted,
                fontSize = 12.sp,
                modifier = Modifier.fillMaxWidth(),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Composable
private fun rememberVersionLabel(): String {
    val context = LocalContext.current
    return try {
        val info = context.packageManager.getPackageInfo(context.packageName, 0)
        val versionName = info.versionName ?: "1.0"
        val versionCode = if (android.os.Build.VERSION.SDK_INT >= 28) {
            info.longVersionCode
        } else {
            @Suppress("DEPRECATION")
            info.versionCode.toLong()
        }
        "v$versionName ($versionCode)"
    } catch (_: PackageManager.NameNotFoundException) {
        "v1.0"
    }
}
