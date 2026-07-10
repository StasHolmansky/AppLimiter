package com.stas.applimiter.utils

import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager

fun ApplicationInfo.isUserApp(): Boolean {

    return (flags and ApplicationInfo.FLAG_SYSTEM) == 0 &&
            (flags and ApplicationInfo.FLAG_UPDATED_SYSTEM_APP) == 0

}

fun PackageManager.launchablePackageNames(): Set<String> {

    val launcherIntent = Intent(Intent.ACTION_MAIN).apply {
        addCategory(Intent.CATEGORY_LAUNCHER)
    }

    return queryIntentActivities(launcherIntent, 0)
        .map { it.activityInfo.packageName }
        .toSet()
}
