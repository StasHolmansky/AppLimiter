package com.stas.applimiter.utils

import android.content.Context
import com.stas.applimiter.R

fun formatMinutes(context: Context, minutes: Long): String {
    val hours = minutes / 60
    val mins = (minutes % 60).toInt()
    return if (hours > 0) {
        context.getString(R.string.format_hours_minutes, hours.toInt(), mins)
    } else {
        context.getString(R.string.format_minutes_only, mins)
    }
}
