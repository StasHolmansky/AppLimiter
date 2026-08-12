package com.stas.applimiter.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Stores an allowed time window for an app.
 * [allowFromMinutes] and [allowUntilMinutes] are minutes from midnight (0..1439).
 * Supports overnight windows, e.g. 21:00 (1260) – 01:00 (60).
 */
@Entity(tableName = "app_schedules")
data class AppScheduleEntity(

    @PrimaryKey
    val packageName: String,

    val appName: String,

    /** Minutes from midnight when the app becomes accessible. */
    val allowFromMinutes: Int,

    /** Minutes from midnight when the app becomes inaccessible. */
    val allowUntilMinutes: Int,
)
