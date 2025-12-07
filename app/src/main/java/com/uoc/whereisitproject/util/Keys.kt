package com.uoc.whereisitproject.util

import android.content.Context
import android.content.pm.PackageManager

fun readMapsApiKey(context: Context): String {
    val ai = context.packageManager.getApplicationInfo(
        context.packageName,
        PackageManager.GET_META_DATA
    )
    return ai.metaData.getString("com.google.android.geo.API_KEY", "")
}