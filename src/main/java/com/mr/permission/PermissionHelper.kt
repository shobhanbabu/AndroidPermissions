package com.mr.permission

import android.content.Context
import android.content.Intent
import android.provider.Settings
import com.mr.common.utils.Version
import com.mr.permission.enums.PermissionCategory
import com.mr.permission.mappers.CategoryPermissionsMapper
import com.mr.permission.utils.PermissionUtils


object PermissionHelper {

    fun hasPermissions(context: Context, categories: List<PermissionCategory>): Boolean {
        categories.forEach { category ->
            //If permission category is MEDIA, check for MANAGE_STORAGE.
            val isMediaPermission = PermissionUtils.isMediaPermission(category)

            val haveAllPermissions =
                (isMediaPermission && PermissionUtils.hasManagePermission(context)) ||
                        PermissionUtils.hasAllPermissions(
                            context,
                            CategoryPermissionsMapper.getPermissions(category)
                        )

            if (!haveAllPermissions) return false
        }
        return true
    }

    fun openPowerSettings(context: Context) {
        if (Version.isMarshmallowPlus()) {
            val intent = Intent()
            intent.action = Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS
            context.startActivity(intent)
        }
    }
}