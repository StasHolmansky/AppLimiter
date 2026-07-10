package com.stas.applimiter.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.net.Uri
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import com.stas.applimiter.MainActivity
import com.stas.applimiter.R
import com.stas.applimiter.data.local.DatabaseProvider
import com.stas.applimiter.data.local.dao.AppLimitDao
import com.stas.applimiter.data.local.dao.LimitExtensionDao
import com.stas.applimiter.data.local.entity.AppLimitEntity
import com.stas.applimiter.data.local.entity.LimitExtensionEntity
import com.stas.applimiter.data.repository.UsageStatsRepository
import com.stas.applimiter.utils.formatMinutes
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.concurrent.ConcurrentHashMap

class UsageMonitorService : Service() {

    private lateinit var usageRepository: UsageStatsRepository

    private lateinit var appLimitDao: AppLimitDao

    private lateinit var extensionDao: LimitExtensionDao

    private lateinit var scope: CoroutineScope

    private var monitorJob: Job? = null

    private val notifiedApps = ConcurrentHashMap.newKeySet<String>()

    private var currentDayKey: Int = -1

    override fun onCreate() {
        super.onCreate()

        usageRepository = UsageStatsRepository(this)

        val database = DatabaseProvider.getDatabase(applicationContext)
        appLimitDao = database.appLimitDao()
        extensionDao = database.limitExtensionDao()

        scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

        createNotificationChannels()

        Log.d(TAG, "Service created")
    }

    override fun onStartCommand(
        intent: Intent?,
        flags: Int,
        startId: Int
    ): Int {

        if (intent?.action == ACTION_STOP) {
            Log.d(TAG, "Stop requested")
            setMonitoringEnabled(this, false)
            stopSelf()
            return START_NOT_STICKY
        }

        if (intent?.action != ACTION_EXTEND && !isMonitoringEnabled(this)) {
            stopSelf()
            return START_NOT_STICKY
        }

        startAsForeground()

        _isRunning.value = isMonitoringEnabled(this)

        if (intent?.action == ACTION_EXTEND) {
            val packageName = intent.getStringExtra(EXTRA_PACKAGE_NAME)
            val appName = intent.getStringExtra(EXTRA_APP_NAME)
            val minutes = intent.getLongExtra(EXTRA_MINUTES, 0L)

            scope.launch {
                if (packageName != null && appName != null) {
                    grantExtension(packageName, appName, minutes)
                }
                if (isMonitoringEnabled(applicationContext)) {
                    startMonitoring()
                } else {
                    stopSelf()
                }
            }

            return START_STICKY
        }

        startMonitoring()

        return START_STICKY
    }

    @Synchronized
    private fun startMonitoring() {
        if (monitorJob?.isActive == true) return

        monitorJob = scope.launch {
            while (isActive) {
                resetIfNewDay()
                checkLimits()
                delay(CHECK_INTERVAL_MS)
            }
        }
    }

    private suspend fun checkLimits() {

        val usageMap = usageRepository.getTodayUsage()

        val limits = appLimitDao.getAllOnce()

        val extensions = extensionDao
            .getAllForDay(todayDayKey())
            .associateBy { it.packageName }

        limits.forEach { limit ->

            val usedMillis = usageMap[limit.packageName] ?: 0L

            val usedMinutes = usedMillis / 1000 / 60

            val bonusMinutes = extensions[limit.packageName]?.bonusMinutes ?: 0L

            val effectiveLimitMinutes = limit.limitMinutes + bonusMinutes

            if (usedMinutes >= effectiveLimitMinutes) {

                if (notifiedApps.add(limit.packageName)) {

                    sendLimitNotification(
                        limit = limit,
                        usedMinutes = usedMinutes,
                        effectiveLimitMinutes = effectiveLimitMinutes,
                        canExtend = bonusMinutes == 0L
                    )

                    Log.d(
                        TAG,
                        "${limit.appName}: LIMIT EXCEEDED $usedMinutes/$effectiveLimitMinutes"
                    )
                }

            } else {

                notifiedApps.remove(limit.packageName)

                Log.d(
                    TAG,
                    "${limit.appName}: $usedMinutes/$effectiveLimitMinutes"
                )
            }
        }
    }

    private suspend fun resetIfNewDay() {

        val today = todayDayKey()

        if (today != currentDayKey) {
            currentDayKey = today
            notifiedApps.clear()
            extensionDao.deleteExceptDay(today)
        }
    }

    private suspend fun grantExtension(
        packageName: String,
        appName: String,
        minutes: Long
    ) {
        if (minutes !in ALLOWED_EXTENSION_MINUTES) return

        val inserted = extensionDao.insert(
            LimitExtensionEntity(
                packageName = packageName,
                dayKey = todayDayKey(),
                bonusMinutes = minutes
            )
        )

        if (inserted == -1L) return

        notifiedApps.remove(packageName)
        showExtensionGrantedNotification(packageName, appName, minutes)

        Log.d(TAG, "$appName: limit extended once by $minutes minutes")
    }

    private fun startAsForeground() {

        val notification = buildForegroundNotification()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            startForeground(
                FOREGROUND_NOTIFICATION_ID,
                notification,
                ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE
            )
        } else {
            startForeground(FOREGROUND_NOTIFICATION_ID, notification)
        }
    }

    private fun createNotificationChannels() {

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return

        val manager = getSystemService(NotificationManager::class.java)

        val serviceChannel = NotificationChannel(
            CHANNEL_SERVICE_ID,
            "Мониторинг использования",
            NotificationManager.IMPORTANCE_LOW
        ).apply {
            description = "Постоянное уведомление о работе мониторинга"
        }

        val alertChannel = NotificationChannel(
            CHANNEL_ALERT_ID,
            "Превышение лимита",
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "Оповещения о превышении дневного лимита приложения"
        }

        manager.createNotificationChannel(serviceChannel)
        manager.createNotificationChannel(alertChannel)
    }

    private fun contentIntent(): PendingIntent {

        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }

        return PendingIntent.getActivity(
            this,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE
        )
    }

    private fun buildForegroundNotification(): Notification {

        return NotificationCompat.Builder(this, CHANNEL_SERVICE_ID)
            .setContentTitle("AppLimiter")
            .setContentText("Мониторинг использования приложений включён")
            .setSmallIcon(R.drawable.ic_notification)
            .setOngoing(true)
            .setContentIntent(contentIntent())
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
    }

    private fun sendLimitNotification(
        limit: AppLimitEntity,
        usedMinutes: Long,
        effectiveLimitMinutes: Long,
        canExtend: Boolean
    ) {

        val builder = NotificationCompat.Builder(this, CHANNEL_ALERT_ID)
            .setContentTitle("Лимит превышен: ${limit.appName}")
            .setContentText(
                "Использовано ${formatMinutes(usedMinutes)} из " +
                    formatMinutes(effectiveLimitMinutes)
            )
            .setSmallIcon(R.drawable.ic_notification)
            .setAutoCancel(true)
            .setContentIntent(contentIntent())
            .setPriority(NotificationCompat.PRIORITY_HIGH)

        if (canExtend) {
            ALLOWED_EXTENSION_MINUTES.forEach { minutes ->
                builder.addAction(
                    R.drawable.ic_notification,
                    "+$minutes мин",
                    extensionIntent(limit, minutes)
                )
            }
        }

        val notification = builder.build()

        getSystemService(NotificationManager::class.java)
            .notify(limit.packageName.hashCode(), notification)
    }

    private fun extensionIntent(
        limit: AppLimitEntity,
        minutes: Long
    ): PendingIntent {
        val intent = Intent(this, UsageMonitorService::class.java).apply {
            action = ACTION_EXTEND
            data = Uri.Builder()
                .scheme("applimiter")
                .authority("extend")
                .appendPath(limit.packageName)
                .appendPath(minutes.toString())
                .build()
            putExtra(EXTRA_PACKAGE_NAME, limit.packageName)
            putExtra(EXTRA_APP_NAME, limit.appName)
            putExtra(EXTRA_MINUTES, minutes)
        }

        return PendingIntent.getService(
            this,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    private fun showExtensionGrantedNotification(
        packageName: String,
        appName: String,
        minutes: Long
    ) {
        val notification = NotificationCompat.Builder(this, CHANNEL_ALERT_ID)
            .setContentTitle("Лимит продлён: $appName")
            .setContentText(
                "Добавлено ${formatMinutes(minutes)}. Повторное продление сегодня недоступно"
            )
            .setSmallIcon(R.drawable.ic_notification)
            .setAutoCancel(true)
            .setOnlyAlertOnce(true)
            .setContentIntent(contentIntent())
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .build()

        getSystemService(NotificationManager::class.java)
            .notify(packageName.hashCode(), notification)
    }

    override fun onDestroy() {

        scope.cancel()

        _isRunning.value = false

        Log.d(TAG, "Service destroyed")

        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    companion object {

        private const val TAG = "UsageMonitor"

        private const val CHANNEL_SERVICE_ID = "usage_monitor_service"
        private const val CHANNEL_ALERT_ID = "usage_limit_alerts"

        private const val FOREGROUND_NOTIFICATION_ID = 1

        private const val CHECK_INTERVAL_MS = 60_000L

        const val ACTION_STOP = "com.stas.applimiter.action.STOP"
        private const val ACTION_EXTEND = "com.stas.applimiter.action.EXTEND"

        private const val EXTRA_PACKAGE_NAME = "package_name"
        private const val EXTRA_APP_NAME = "app_name"
        private const val EXTRA_MINUTES = "minutes"

        private const val PREFERENCES_NAME = "monitoring_state"
        private const val KEY_MONITORING_ENABLED = "monitoring_enabled"

        private val ALLOWED_EXTENSION_MINUTES = listOf(1L, 3L, 5L)

        private val _isRunning = MutableStateFlow(false)
        val isRunning: StateFlow<Boolean> = _isRunning.asStateFlow()

        fun start(context: Context) {
            setMonitoringEnabled(context, true)
            val intent = Intent(context, UsageMonitorService::class.java)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }

        fun stop(context: Context) {
            setMonitoringEnabled(context, false)
            val intent = Intent(context, UsageMonitorService::class.java).apply {
                action = ACTION_STOP
            }
            context.startService(intent)
        }

        fun isMonitoringEnabled(context: Context): Boolean {
            return context
                .getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE)
                .getBoolean(KEY_MONITORING_ENABLED, false)
        }

        private fun setMonitoringEnabled(context: Context, enabled: Boolean) {
            context
                .getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE)
                .edit()
                .putBoolean(KEY_MONITORING_ENABLED, enabled)
                .apply()
        }

        private fun todayDayKey(): Int {
            val calendar = Calendar.getInstance()
            return calendar.get(Calendar.YEAR) * 1000 +
                calendar.get(Calendar.DAY_OF_YEAR)
        }
    }
}
