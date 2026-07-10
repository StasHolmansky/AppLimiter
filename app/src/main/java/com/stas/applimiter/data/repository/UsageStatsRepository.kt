package com.stas.applimiter.data.repository

import android.app.usage.UsageStatsManager
import android.content.Context
import com.stas.applimiter.utils.launchablePackageNames
import java.util.Calendar

class UsageStatsRepository(
    private val context: Context
) {

    fun getTodayUsage(): Map<String, Long> {

        val usageStatsManager =
            context.getSystemService(Context.USAGE_STATS_SERVICE)
                    as UsageStatsManager

        val endTime = System.currentTimeMillis()

        val startTime =
            Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }.timeInMillis

        val launchablePackages =
            context.packageManager.launchablePackageNames()

        return usageStatsManager
            .queryAndAggregateUsageStats(
                startTime,
                endTime
            )
            .filter { (packageName, _) ->
                packageName in launchablePackages
            }
            .mapValues {
                it.value.totalTimeInForeground
            }

    }

}