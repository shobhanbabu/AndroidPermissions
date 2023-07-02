package com.mr.permission.utils

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.provider.Settings

fun Activity.launchMyAppDetails() {
    val intent = Intent()
    intent.action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
    intent.addCategory(Intent.CATEGORY_DEFAULT)
    intent.data = Uri.parse("package:$packageName")
    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY)
    intent.addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS)
    startActivity(intent)
}