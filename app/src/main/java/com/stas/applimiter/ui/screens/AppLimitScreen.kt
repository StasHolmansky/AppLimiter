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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
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
import com.stas.applimiter.ui.components.AppCard
import com.stas.applimiter.ui.theme.AppTheme
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppLimitScreen(
    navController: NavController,
    packageName: String,
    appName: String,
) {
    val context = LocalContext.current
    val colors = AppTheme.colors

    val dao = remember {
        DatabaseProvider.getDatabase(context).appLimitDao()
    }
    val scope = rememberCoroutineScope()

    val limits = dao.getAll().collectAsState(initial = emptyList()).value
    val currentLimit = limits.find { it.packageName == packageName }

    var hours by remember { mutableStateOf("") }
    var minutes by remember { mutableStateOf("") }

    LaunchedEffect(currentLimit) {
        if (currentLimit != null) {
            val totalMinutes = currentLimit.limitMinutes
            hours = (totalMinutes / 60).toString()
            minutes = (totalMinutes % 60).toString()
        } else {
            hours = ""
            minutes = ""
        }
    }

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

    Scaffold(
        containerColor = colors.background,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Лимит приложения",
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
                .padding(16.dp),
        ) {
            AppCard {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = appName,
                        color = colors.textPrimary,
                        fontSize = 22.sp,
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

            Spacer(modifier = Modifier.height(20.dp))

            Button(
                onClick = {
                    val normalizedMinutes = (minutes.toLongOrNull() ?: 0L).coerceIn(0, 59)
                    val totalMinutes = (hours.toLongOrNull() ?: 0L) * 60 + normalizedMinutes
                    scope.launch {
                        dao.insert(
                            AppLimitEntity(
                                packageName = packageName,
                                appName = appName,
                                limitMinutes = totalMinutes,
                            ),
                        )
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
                    scope.launch { dao.delete(packageName) }
                    navController.popBackStack()
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = colors.danger,
                ),
                border = androidx.compose.foundation.BorderStroke(1.dp, colors.danger),
            ) {
                Text("Сбросить лимит", fontWeight = FontWeight.SemiBold)
            }
        }
    }
}
