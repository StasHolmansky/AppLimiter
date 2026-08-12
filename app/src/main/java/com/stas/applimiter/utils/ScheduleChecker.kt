package com.stas.applimiter.utils

import com.stas.applimiter.data.local.entity.AppScheduleEntity
import java.util.Calendar

/** Returns true when the current time falls inside the schedule's allowed window. */
fun AppScheduleEntity.isAllowedNow(): Boolean {
    val now = Calendar.getInstance()
    val nowMinutes = now.get(Calendar.HOUR_OF_DAY) * 60 + now.get(Calendar.MINUTE)
    return isAllowedAt(nowMinutes)
}

fun AppScheduleEntity.isAllowedAt(minutesFromMidnight: Int): Boolean {
    return if (allowFromMinutes <= allowUntilMinutes) {
        // Normal window: e.g. 09:00–17:00
        minutesFromMidnight in allowFromMinutes until allowUntilMinutes
    } else {
        // Overnight window: e.g. 21:00–01:00
        minutesFromMidnight >= allowFromMinutes || minutesFromMidnight < allowUntilMinutes
    }
}

/** Human-readable label, e.g. "21:00 – 01:00". */
fun AppScheduleEntity.formatWindow(): String {
    return "${formatMinutesOfDay(allowFromMinutes)} – ${formatMinutesOfDay(allowUntilMinutes)}"
}

fun formatMinutesOfDay(minutes: Int): String {
    val h = minutes / 60
    val m = minutes % 60
    return "%02d:%02d".format(h, m)
}
