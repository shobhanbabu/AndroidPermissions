package com.mr.permission.mappers

import android.Manifest
import android.os.Build
import androidx.annotation.RequiresApi
import com.mr.permission.utils.Version
import com.mr.permission.enums.PermissionCategory

object CategoryPermissionsMapper {
    internal fun getCategory(permission: String): PermissionCategory {
        return when (permission) {
            Manifest.permission.MANAGE_EXTERNAL_STORAGE -> PermissionCategory.MANAGE_STORAGE

            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE -> PermissionCategory.MEDIA

            Manifest.permission.READ_MEDIA_IMAGES -> PermissionCategory.PHOTOS

            Manifest.permission.READ_MEDIA_VIDEO -> PermissionCategory.VIDEOS

            Manifest.permission.READ_MEDIA_AUDIO -> PermissionCategory.MUSIC

            Manifest.permission.WRITE_CONTACTS,
            Manifest.permission.READ_CONTACTS -> PermissionCategory.CONTACTS

            Manifest.permission.WRITE_CALENDAR,
            Manifest.permission.READ_CALENDAR -> PermissionCategory.CALENDAR

            Manifest.permission.WRITE_CALL_LOG,
            Manifest.permission.READ_CALL_LOG -> PermissionCategory.CALL_LOGS

            Manifest.permission.READ_SMS -> PermissionCategory.SMS

            Manifest.permission.ACCESS_MEDIA_LOCATION -> PermissionCategory.MEDIA_LOCATION

            Manifest.permission.POST_NOTIFICATIONS -> PermissionCategory.NOTIFICATIONS

            Manifest.permission.REQUEST_IGNORE_BATTERY_OPTIMIZATIONS -> PermissionCategory.IGNORE_BATTERY_OPTIMIZATION

            else -> throw RuntimeException("$permission not handled")
        }
    }

    internal fun getPermissions(category: PermissionCategory): Array<String> {
        return when (category) {
            PermissionCategory.MEDIA -> getMediaPermissions()

            PermissionCategory.PHOTOS -> getImagesPermissions()

            PermissionCategory.VIDEOS -> getVideoPermissions()

            PermissionCategory.MUSIC -> getAudioPermissions()

            PermissionCategory.MANAGE_STORAGE -> getStoragePermissions()

            PermissionCategory.CONTACTS -> getContactPermissions()

            PermissionCategory.CALENDAR -> getCalendarPermissions()

            PermissionCategory.CALL_LOGS -> getCallLogPermissions()

            PermissionCategory.SMS -> getSmsPermissions()

            PermissionCategory.SCHEDULE -> getAlarmPermissions()

            PermissionCategory.MEDIA_LOCATION -> getMediaLocationPermissions()

            PermissionCategory.NOTIFICATIONS -> getNotificationPermissions()

            PermissionCategory.IGNORE_BATTERY_OPTIMIZATION -> getIgnoreBatteryOptPermissions()
        }
    }

    @RequiresApi(Build.VERSION_CODES.R)
    private fun getManageStoragePermission() = arrayOf(Manifest.permission.MANAGE_EXTERNAL_STORAGE)

    private fun getStoragePermissions() = if (Version.isRPlus()) getManageStoragePermission()
    else getMediaPermissions()

    private fun getMediaPermissions() = run {
        val permissions = mutableSetOf<String>()

        permissions.addAll(getImagesPermissions())
        permissions.addAll(getVideoPermissions())
        permissions.addAll(getAudioPermissions())

        permissions.toTypedArray()
    }

    private fun getImagesPermissions() = run {
        val permissions = mutableListOf<String>()
        if (Version.isTPlus()) {
            permissions.add(Manifest.permission.READ_MEDIA_IMAGES)
        } else {
            permissions.add(Manifest.permission.READ_EXTERNAL_STORAGE)
        }
        if (!Version.isRPlus()) permissions.add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
        permissions.addAll(getMediaLocationPermissions())

        permissions.toTypedArray()
    }

    private fun getVideoPermissions() = run {
        val permissions = mutableListOf<String>()
        if (Version.isTPlus()) {
            permissions.add(Manifest.permission.READ_MEDIA_VIDEO)
        } else {
            permissions.add(Manifest.permission.READ_EXTERNAL_STORAGE)
        }
        if (!Version.isRPlus()) permissions.add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
        permissions.addAll(getMediaLocationPermissions())

        permissions.toTypedArray()
    }

    private fun getAudioPermissions() = run {
        val permissions = mutableListOf<String>()
        if (Version.isTPlus()) {
            permissions.add(Manifest.permission.READ_MEDIA_AUDIO)
        } else {
            permissions.add(Manifest.permission.READ_EXTERNAL_STORAGE)
        }
        if (!Version.isRPlus()) permissions.add(Manifest.permission.WRITE_EXTERNAL_STORAGE)

        permissions.toTypedArray()
    }

    private fun getMediaLocationPermissions() = run {
        if (Version.isQPlus()) {
            return@run arrayOf(Manifest.permission.ACCESS_MEDIA_LOCATION)
        }
        arrayOf()
    }

    private fun getContactPermissions() = arrayOf(
        Manifest.permission.READ_CONTACTS,
        Manifest.permission.WRITE_CONTACTS,
    )

    private fun getCalendarPermissions() = arrayOf(
        Manifest.permission.READ_CALENDAR,
        Manifest.permission.WRITE_CALENDAR
    )

    private fun getCallLogPermissions() = arrayOf(
        Manifest.permission.READ_CALL_LOG,
        Manifest.permission.WRITE_CALL_LOG
    )

    private fun getSmsPermissions() = arrayOf(
        Manifest.permission.READ_SMS
    )

    private fun getIgnoreBatteryOptPermissions() = run {
        var permissions = arrayOf<String>()
        if (Version.isMarshmallowPlus()) {
            permissions = permissions.plus(Manifest.permission.REQUEST_IGNORE_BATTERY_OPTIMIZATIONS)
        }
        permissions
    }

    private fun getNotificationPermissions() = run {
        var permissions = arrayOf<String>()
        if (Version.isTPlus()) {
            permissions = permissions.plus(Manifest.permission.POST_NOTIFICATIONS)
        }
        permissions
    }

    private fun getAlarmPermissions() = run {
        var permissions = arrayOf<String>()
        if (Version.isSPlus()) {
            permissions = permissions.plus(Manifest.permission.SCHEDULE_EXACT_ALARM)
        }
        permissions
    }
}