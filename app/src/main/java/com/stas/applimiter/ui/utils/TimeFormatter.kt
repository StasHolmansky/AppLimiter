package com.stas.applimiter.utils

fun formatMinutes(minutes: Long): String {

    val hours = minutes / 60
    val mins = minutes % 60

    return when {

        hours > 0 ->
            "${hours} ч ${mins} мин"

        else ->
            "${mins} мин"

    }

}