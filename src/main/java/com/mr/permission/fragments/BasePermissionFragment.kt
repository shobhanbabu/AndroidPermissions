package com.mr.permission.fragments

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.os.PowerManager
import android.provider.Settings
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.mr.permission.utils.Version
import com.mr.permission.model.PermissionResult

/**
 * A simple abstract [Fragment] subclass.
 *
 */
internal abstract class BasePermissionFragment : Fragment() {

    private val rationalRequest = mutableMapOf<Int, Boolean>()
    private val pendingSpecialPermissions = mutableListOf<String>()
    private val pendingNormalPermissions = mutableListOf<String>()
    private val specialDeniedPermissions = mutableListOf<String>()
    private var specialPermissionReqCode: Int = -1
    private var requestedSpecialPermission: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        retainInstance = true
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if (grantResults.isNotEmpty() &&
            grantResults.all { it == PackageManager.PERMISSION_GRANTED }
        ) {
            if (specialDeniedPermissions.isEmpty()) {
                _onPermissionResult(PermissionResult.PermissionGranted(requestCode))
            } else {
                _onPermissionResult(
                    PermissionResult.PermissionDenied(requestCode, specialDeniedPermissions)
                )
            }
        } else if (permissions.any { shouldShowRequestPermissionRationale(it) }) {
            _onPermissionResult(
                PermissionResult.PermissionDenied(
                    requestCode,
                    permissions.filterIndexed { index, _ ->
                        grantResults[index] == PackageManager.PERMISSION_DENIED
                    } + specialDeniedPermissions
                )
            )
        } else {
            _onPermissionResult(
                PermissionResult.PermissionDeniedPermanently(requestCode,
                    permissions.filterIndexed { index, _ ->
                        grantResults[index] == PackageManager.PERMISSION_DENIED
                    } + specialDeniedPermissions
                )
            )
        }
    }

    protected fun requestPermissions(requestId: Int, vararg permissions: String) {
        rationalRequest[requestId]?.let {
            rationalRequest.remove(requestId)
            categorisePermissions(permissions)
            if (pendingSpecialPermissions.isNotEmpty()) {
                requestNextSpecialPermission(requestId)
            } else {
                requestPermissions(pendingNormalPermissions.toTypedArray(), requestId)
            }
            return
        }

        val notGranted = categorisePermissions(permissions)

        when {
            notGranted.isEmpty() -> {
                _onPermissionResult(PermissionResult.PermissionGranted(requestId))
            }

            notGranted.any {
                !isSpecialPermission(it) && shouldShowRequestPermissionRationale(it)
            } -> {
                rationalRequest[requestId] = false
                _onPermissionResult(PermissionResult.ShowRational(requestId))
            }

            pendingSpecialPermissions.isNotEmpty() -> {
                requestNextSpecialPermission(requestId)
            }

            else -> {
                requestPermissions(pendingNormalPermissions.toTypedArray(), requestId)
            }
        }
    }

    private fun categorisePermissions(permissions: Array<out String>): MutableList<String> {
        val notGranted = mutableListOf<String>()
        permissions.forEach {
            if (isSpecialPermission(it)) {
                if (!hasSpecialPermission(it)) {
                    pendingSpecialPermissions.add(it)
                    notGranted.add(it)
                }
            } else if (!hasNormalPermission(it)) {
                pendingNormalPermissions.add(it)
                notGranted.add(it)
            }
        }
        return notGranted
    }

    private fun containSpecialPermission(permission: Array<out String>) = permission.any {
        isSpecialPermission(it)
    }

    private fun requestNextSpecialPermission(requestId: Int) {
        if (pendingSpecialPermissions.size > 0) {
            specialPermissionReqCode = requestId
            requestedSpecialPermission = pendingSpecialPermissions.removeAt(0)
            requestSpecialPermission(requestId, requestedSpecialPermission!!)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (specialPermissionReqCode == -1 || specialPermissionReqCode != requestCode) {
            super.onActivityResult(requestCode, resultCode, data)
            return
        }

        requestedSpecialPermission?.let { permission ->
            requestedSpecialPermission = null
            if (!hasSpecialPermission(permission)) specialDeniedPermissions.add(permission)
        }

        when {
            pendingSpecialPermissions.isNotEmpty() -> {
                requestNextSpecialPermission(requestCode)
            }
            pendingNormalPermissions.isNotEmpty() -> {
                requestPermissions(pendingNormalPermissions.toTypedArray(), requestCode)
            }
            else -> {
                if (specialDeniedPermissions.isNotEmpty()) {
                    _onPermissionResult(
                        PermissionResult.PermissionDenied(requestCode, specialDeniedPermissions)
                    )
                } else {
                    _onPermissionResult(PermissionResult.PermissionGranted(requestCode))
                }
            }
        }
    }

    protected fun hasPermission(permission: String): Boolean {
        return if (isSpecialPermission(permission)) {
            hasSpecialPermission(permission)
        } else {
            hasNormalPermission(permission)
        }
    }


    @SuppressLint("NewApi")
    protected fun hasNormalPermission(permission: String): Boolean {
        return ContextCompat.checkSelfPermission(
            requireContext(), permission
        ) == PackageManager.PERMISSION_GRANTED
    }

    @SuppressLint("NewApi")
    protected fun hasSpecialPermission(permission: String): Boolean {
        return when (permission) {
            Manifest.permission.MANAGE_EXTERNAL_STORAGE -> Environment.isExternalStorageManager()
            Manifest.permission.REQUEST_IGNORE_BATTERY_OPTIMIZATIONS -> isIgnoringBatteryOptimizations()
            else -> false
        }
    }

    //https://developer.android.com/reference/android/provider/Settings#ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS
    private fun isIgnoringBatteryOptimizations(): Boolean {
        val context = requireContext()
        val powerManager = context.getSystemService(Context.POWER_SERVICE) as PowerManager
        return !Version.isMarshmallowPlus() ||
                powerManager.isIgnoringBatteryOptimizations(context.packageName)
    }

    private fun isSpecialPermission(permission: String) = when (permission) {
        Manifest.permission.MANAGE_EXTERNAL_STORAGE -> true
        Manifest.permission.REQUEST_IGNORE_BATTERY_OPTIMIZATIONS -> true
        else -> false
    }

    @SuppressLint("InlinedApi")
    private fun requestSpecialPermission(requestId: Int, permission: String) {
        when (permission) {
            Manifest.permission.MANAGE_EXTERNAL_STORAGE -> activity?.let {
                try {
                    val intent = Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION)
                    intent.addCategory(Intent.CATEGORY_DEFAULT)
                    intent.data = Uri.parse(String.format("package:%s", it.packageName))
                    startActivityForResult(intent, requestId)
                } catch (e: Exception) {
                    val intent = Intent()
                    intent.action = Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION
                    intent.data = Uri.parse(String.format("package:%s", it.packageName))
                    startActivityForResult(intent, requestId)
                }
            }
            Manifest.permission.REQUEST_IGNORE_BATTERY_OPTIMIZATIONS -> activity?.let {
                try {
                    val intent = Intent()
                    intent.action = Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS
                    intent.data = Uri.parse(String.format("package:%s", it.packageName))
                    startActivityForResult(intent, requestId)
                } catch (_: Exception) {
                }
            }
            else -> throw RuntimeException("Special permission ($permission) request intent is not handled")
        }
    }

    private fun _onPermissionResult(permissionResult: PermissionResult) {
        clearPrevState()
        onPermissionResult(permissionResult)
    }

    private fun clearPrevState() {
        pendingSpecialPermissions.clear()
        pendingNormalPermissions.clear()
        specialDeniedPermissions.clear()
        specialPermissionReqCode = -1
        requestedSpecialPermission = null
    }

    protected abstract fun onPermissionResult(permissionResult: PermissionResult)
}