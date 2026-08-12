package com.stas.applimiter.service

import android.accessibilityservice.AccessibilityService
import android.content.Intent
import android.view.accessibility.AccessibilityEvent
import com.stas.applimiter.data.local.DatabaseProvider
import com.stas.applimiter.data.local.dao.AppLimitDao
import com.stas.applimiter.data.local.dao.AppScheduleDao
import com.stas.applimiter.data.local.dao.LimitExtensionDao
import com.stas.applimiter.data.repository.UsageStatsRepository
import com.stas.applimiter.utils.isAllowedNow
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Calendar

class AppBlockAccessibilityService : AccessibilityService() {

    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    private lateinit var appLimitDao: AppLimitDao
    private lateinit var extensionDao: LimitExtensionDao
    private lateinit var appScheduleDao: AppScheduleDao
    private lateinit var usageRepository: UsageStatsRepository

    private var currentPackageName: String? = null
    private var checkJob: Job? = null
    private var homePackages: Set<String> = emptySet()

    override fun onServiceConnected() {
        super.onServiceConnected()

        val database = DatabaseProvider.getDatabase(applicationContext)
        appLimitDao = database.appLimitDao()
        extensionDao = database.limitExtensionDao()
        appScheduleDao = database.appScheduleDao()
        usageRepository = UsageStatsRepository(applicationContext)
        homePackages = findHomePackages()
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event?.eventType != AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) return

        val packageName = event.packageName?.toString() ?: return

        if (packageName == currentPackageName && checkJob?.isActive == true) return

        currentPackageName = packageName
        checkJob?.cancel()

        if (packageName == applicationContext.packageName ||
            packageName == SYSTEM_UI_PACKAGE ||
            packageName in homePackages ||
            !UsageMonitorService.isMonitoringEnabled(applicationContext)
        ) {
            return
        }

        checkJob = scope.launch {
            monitorCurrentApp(packageName)
        }
    }

    private suspend fun monitorCurrentApp(packageName: String) {
        if (!UsageMonitorService.isMonitoringEnabled(applicationContext)) return

        // Schedule check — kick out immediately if outside allowed window
        val schedule = appScheduleDao.getByPackageName(packageName)
        if (schedule != null) {
            if (!schedule.isAllowedNow()) {
                withContext(Dispatchers.Main) { performGlobalAction(GLOBAL_ACTION_HOME) }
                return
            }
            // Keep watching while app is open — kick out the moment the window ends
            while (currentPackageName == packageName &&
                UsageMonitorService.isMonitoringEnabled(applicationContext)
            ) {
                if (!schedule.isAllowedNow()) {
                    withContext(Dispatchers.Main) { performGlobalAction(GLOBAL_ACTION_HOME) }
                    return
                }
                delay(CHECK_INTERVAL_MS)
            }
            return
        }

        // Duration-based check
        val limit = appLimitDao.getByPackageName(packageName) ?: return

        while (currentPackageName == packageName &&
            UsageMonitorService.isMonitoringEnabled(applicationContext)
        ) {
            val usedMillis = usageRepository
                .getTodayUsage()[packageName] ?: 0L

            val bonusMinutes = extensionDao
                .getForPackageAndDay(packageName, todayDayKey())
                ?.bonusMinutes ?: 0L

            val effectiveLimitMillis =
                (limit.limitMinutes + bonusMinutes) * MILLIS_PER_MINUTE

            if (usedMillis >= effectiveLimitMillis) {
                withContext(Dispatchers.Main) {
                    performGlobalAction(GLOBAL_ACTION_HOME)
                }
                return
            }

            delay(CHECK_INTERVAL_MS)
        }
    }

    private fun findHomePackages(): Set<String> {
        val homeIntent = Intent(Intent.ACTION_MAIN).apply {
            addCategory(Intent.CATEGORY_HOME)
        }

        return packageManager
            .queryIntentActivities(homeIntent, 0)
            .mapTo(mutableSetOf()) { it.activityInfo.packageName }
    }

    override fun onInterrupt() {
        checkJob?.cancel()
    }

    override fun onDestroy() {
        scope.cancel()
        super.onDestroy()
    }

    private fun todayDayKey(): Int {
        val calendar = Calendar.getInstance()
        return calendar.get(Calendar.YEAR) * 1000 +
            calendar.get(Calendar.DAY_OF_YEAR)
    }

    companion object {
        private const val SYSTEM_UI_PACKAGE = "com.android.systemui"
        private const val CHECK_INTERVAL_MS = 1_000L
        private const val MILLIS_PER_MINUTE = 60_000L
    }
}
