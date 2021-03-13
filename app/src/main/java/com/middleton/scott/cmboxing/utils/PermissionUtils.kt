package com.middleton.scott.cmboxing.utils

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.fragment.app.Fragment

fun Fragment.checkAudioPermission(requestCode: Int): Boolean {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        if (!requireContext().isGranted(Manifest.permission.RECORD_AUDIO)) {
            requestPermissions(
                arrayOf(Manifest.permission.RECORD_AUDIO),
                requestCode
            )
            return false
        }
    }
    return true
}

fun Context.isGranted(permissionCode: String) =
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        checkSelfPermission(permissionCode) == PackageManager.PERMISSION_GRANTED
    } else {
        true
    }