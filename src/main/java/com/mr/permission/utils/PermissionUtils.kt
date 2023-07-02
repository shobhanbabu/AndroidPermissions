package com.mr.permission.utils

import android.content.Context
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import com.mr.permission.utils.launchMyAppDetails
import com.mr.permission.PermissionHelper
import com.mr.permission.R
import com.mr.permission.enums.PermissionCategory
import com.mr.permission.fragments.PermissionFragment
import com.mr.permission.mappers.CategoryPermissionsMapper
import com.mr.permission.model.PermissionResult
import com.mr.permission.model.PermissionStringRes
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

internal object PermissionUtils {

    internal fun hasManagePermission(context: Context): Boolean {
        return hasAllPermissions(
            context,
            CategoryPermissionsMapper.getPermissions(PermissionCategory.MANAGE_STORAGE)
        )
    }

    internal fun hasAllPermissions(context: Context, permissions: Array<String>): Boolean {
        permissions.forEach { permission ->
            if (!PermissionFragment.hasPermission(context, permission)) {
                return false
            }
        }
        return true
    }

    internal fun proceedWithPermissionCheck(
        fragment: Fragment,
        categories: List<PermissionCategory>,
        uiText: PermissionStringRes,
        action: (Boolean, List<PermissionCategory>) -> Unit = { _, _ -> },
    ) {
        CoroutineScope(Dispatchers.Main).launch {
            if (PermissionHelper.hasPermissions(fragment.requireContext(), categories)) {
                action(true, emptyList())
                return@launch
            }
            val containsBoth = categories.contains(PermissionCategory.MANAGE_STORAGE)
                    && containMediaPermissions(categories)

            var permissions = arrayOf<String>()
            categories.forEach { category ->
                if (
                    containsBoth &&
                    (isMediaPermission(category) || category == PermissionCategory.MANAGE_STORAGE)
                ) {
                    if (category == PermissionCategory.MANAGE_STORAGE)
                        permissions =
                            permissions.plus(CategoryPermissionsMapper.getPermissions(category))
                } else {
                    permissions =
                        permissions.plus(CategoryPermissionsMapper.getPermissions(category))
                }
            }
            withContext(Dispatchers.Main) {
                val result = PermissionFragment.requestPermissions(
                    fragment,
                    1, *permissions
                )
                if (result is PermissionResult.ShowRational) {
                    val alertDialog = AlertDialog.Builder(fragment.requireContext())
                        .setTitle(uiText.rationalTitle)
                        .setMessage(uiText.rationalDesc)
                        .setPositiveButton(R.string.allow) { dialog, _ ->
                            dialog.dismiss()
                            proceedWithPermissionCheck(fragment, categories, uiText, action)
                        }
                        .setNegativeButton(R.string.not_now) { dialog, _ ->
                            dialog.dismiss()
                        }.create()
                    alertDialog.show()
                } else if (result is PermissionResult.PermissionDeniedPermanently) {
                    val alertDialog = AlertDialog.Builder(fragment.requireContext())
                        .setMessage(uiText.gotoSettingsDesc)
                        .setTitle("")
                        .setNegativeButton(R.string.open_settings) { dialog, _ ->
                            dialog.dismiss()
                            fragment.activity?.launchMyAppDetails()
                        }.create()
                    alertDialog.show()
                }
                fragment.activity?.let { context ->
                    action.invoke(
                        result is PermissionResult.PermissionGranted,
                        getDeniedCategories(context, permissions)
                    )
                }
            }
        }
    }

    private fun containMediaPermissions(categories: List<PermissionCategory>): Boolean {
        return categories.contains(PermissionCategory.MEDIA) ||
                categories.contains(PermissionCategory.PHOTOS) ||
                categories.contains(PermissionCategory.VIDEOS) ||
                categories.contains(PermissionCategory.MUSIC)
    }

    internal fun isMediaPermission(category: PermissionCategory): Boolean {
        return category == PermissionCategory.MEDIA ||
                category == PermissionCategory.PHOTOS ||
                category == PermissionCategory.VIDEOS ||
                category == PermissionCategory.MUSIC
    }

    private fun getDeniedCategories(
        context: Context, permissions: Array<String>
    ): List<PermissionCategory> {
        return permissions.filter {
            !PermissionFragment.hasPermission(context, it)
        }.map { CategoryPermissionsMapper.getCategory(it) }.distinct()
    }
}