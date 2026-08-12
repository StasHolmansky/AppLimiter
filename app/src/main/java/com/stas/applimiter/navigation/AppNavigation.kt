package com.stas.applimiter.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.stas.applimiter.ui.screens.AppLimitScreen
import com.stas.applimiter.ui.screens.FeedbackScreen
import com.stas.applimiter.ui.screens.HomeScreen
import com.stas.applimiter.ui.screens.SettingsScreen
import com.stas.applimiter.ui.theme.ThemePreference
import java.net.URLDecoder
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

@Composable
fun AppNavigation(
    preference: ThemePreference,
    onPreferenceChange: (ThemePreference) -> Unit,
    bankSafeMode: Boolean = false,
    onBankSafeModeChange: (Boolean) -> Unit = {},
) {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = "home",
    ) {
        composable("home") {
            HomeScreen(
                navController = navController,
                bankSafeMode = bankSafeMode,
                onBankSafeModeChange = onBankSafeModeChange,
            )
        }

        composable("settings") {
            SettingsScreen(
                navController = navController,
                preference = preference,
                onPreferenceChange = onPreferenceChange,
                bankSafeMode = bankSafeMode,
                onBankSafeModeChange = onBankSafeModeChange,
            )
        }

        composable("feedback") {
            FeedbackScreen(navController = navController)
        }

        composable(
            route = "appLimit/{packageName}/{appName}",
            arguments = listOf(
                navArgument("packageName") { type = NavType.StringType },
                navArgument("appName") { type = NavType.StringType },
            ),
        ) { backStackEntry ->
            val packageName = backStackEntry.arguments?.getString("packageName").orEmpty()
            val appNameEncoded = backStackEntry.arguments?.getString("appName").orEmpty()
            val appName = URLDecoder.decode(appNameEncoded, StandardCharsets.UTF_8.name())

            AppLimitScreen(
                navController = navController,
                packageName = packageName,
                appName = appName,
            )
        }
    }
}

fun encodeNavArg(value: String): String =
    URLEncoder.encode(value, StandardCharsets.UTF_8.name())
