package com.bale.fingerprint

import android.content.Context
import android.content.pm.PackageManager
import android.support.annotation.IntegerRes
import android.support.annotation.StringRes
import android.support.design.widget.Snackbar
import android.support.v4.content.ContextCompat
import android.view.View
import android.content.Context.ACCESSIBILITY_SERVICE
import android.view.accessibility.AccessibilityManager



/**
 * Created by Prashant on 03-12-2017.
 */

fun Context.isAccessibilityEnabled(): Boolean {
  val am = getSystemService(ACCESSIBILITY_SERVICE) as AccessibilityManager
  return am.isEnabled
}

fun Context.isPermissionGranted(persmission: String) =
    (ContextCompat.checkSelfPermission(this, persmission) == PackageManager.PERMISSION_GRANTED)


inline fun View.snack(@StringRes messageRes: Int, length: Int = Snackbar.LENGTH_LONG, f: Snackbar.() -> Unit) {
  snack(resources.getString(messageRes), length, f)
}

inline fun View.snack(message: String, length: Int = Snackbar.LENGTH_LONG, f: Snackbar.() -> Unit) : Snackbar {
  val snack = Snackbar.make(this, message, length)
  snack.f()
  snack.show()
  return snack
}

fun Snackbar.action(@StringRes actionRes: Int, color: Int? = null, listener: (View) -> Unit) {
  action(view.resources.getString(actionRes), color, listener)
}

fun Snackbar.action(action: String, color: Int? = null, listener: (View) -> Unit) {
  setAction(action, listener)
  color?.let { setActionTextColor(color) }
}