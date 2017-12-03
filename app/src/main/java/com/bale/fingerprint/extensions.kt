package com.bale.fingerprint

import android.content.Context
import android.content.pm.PackageManager
import android.support.v4.content.ContextCompat

/**
 * Created by Prashant on 03-12-2017.
 */

fun Context.isPermissionGranted(persmission: String) =
    (ContextCompat.checkSelfPermission(this, persmission) == PackageManager.PERMISSION_GRANTED)

