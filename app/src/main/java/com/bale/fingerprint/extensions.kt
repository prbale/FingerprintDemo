package com.bale.fingerprint

import android.content.Context
import android.content.Context.ACCESSIBILITY_SERVICE
import android.content.pm.PackageManager
import android.support.v4.content.ContextCompat
import android.view.accessibility.AccessibilityManager

fun Context.isAccessibilityEnabled(): Boolean = (getSystemService(ACCESSIBILITY_SERVICE) as AccessibilityManager).isEnabled

fun Context.isPermissionGranted(persmission: String) =
    (ContextCompat.checkSelfPermission(this, persmission) == PackageManager.PERMISSION_GRANTED)