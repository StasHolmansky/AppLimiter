package com.stas.applimiter.data.local.entity

import androidx.room.Entity

@Entity(
    tableName = "limit_extensions",
    primaryKeys = ["packageName", "dayKey"]
)
data class LimitExtensionEntity(
    val packageName: String,
    val dayKey: Int,
    val bonusMinutes: Long
)
