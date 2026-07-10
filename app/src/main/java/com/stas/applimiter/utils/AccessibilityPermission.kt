package com.stas.applimiter.utils

import android.accessibilityservice.AccessibilityServiceInfo
import android.content.ComponentName
import android.content.Context
import android.provider.Settings
import android.text.TextUtils
import android.view.accessibility.AccessibilityManager
import com.stas.applimiter.service.AppBlockAccessibilityService

fun hasAppBlockAccessibilityPermission(context: Context): Boolean {
    val expected = ComponentName(context, AppBlockAccessibilityService::class.java)

    val manager = context
        .getSystemService(Context.ACCESSIBILITY_SERVICE) as? AccessibilityManager

    val enabledServices = manager
        ?.getEnabledAccessibilityServiceList(AccessibilityServiceInfo.FEEDBACK_ALL_MASK)
        .orEmpty()

    val foundViaManager = enabledServices.any { info ->
        val serviceInfo = info.resolveInfo.serviceInfo
        serviceInfo.packageName == expected.packageName &&
            serviceInfo.name == expected.className
    }

    if (foundViaManager) return true

    val enabledSetting = Settings.Secure.getString(
        context.contentResolver,
        Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
    ) ?: return false

    val flattened = expected.flattenToString()

    val splitter = TextUtils.SimpleStringSplitter(':')
    splitter.setString(enabledSetting)

    while (splitter.hasNext()) {
        if (splitter.next().equals(flattened, ignoreCase = true)) {
            return true
        }
    }

    return false
}
