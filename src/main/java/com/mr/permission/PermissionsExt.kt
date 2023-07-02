package com.mr.permission

import android.app.Activity
import androidx.fragment.app.Fragment
import com.mr.permission.enums.PermissionCategory
import com.mr.permission.model.PermissionStringRes
import com.mr.permission.utils.PermissionUtils

fun Fragment.hasPermissions(categories: List<PermissionCategory>): Boolean {
    activity?.let {
        return PermissionHelper.hasPermissions(it, categories)
    }
    return false
}

fun Activity.hasPermissions(categories: List<PermissionCategory>): Boolean {
    return PermissionHelper.hasPermissions(this, categories)
}

fun Fragment.proceedWithPermissionCheck(
    categories: List<PermissionCategory>,
    uiText: PermissionStringRes,
    action: (Boolean, List<PermissionCategory>) -> Unit = { _, _ -> },
) {
    PermissionUtils.proceedWithPermissionCheck(this, categories, uiText, action)
}