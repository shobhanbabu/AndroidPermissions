package com.mr.permission.fragments

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.os.Environment
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.mr.permission.model.PermissionResult
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Permission manager which handles checking permission is granted or not and if not then will request permission.
 * A headless fragment which wraps the boilerplate code for checking and requesting permission
 * and suspends the coroutines until result is available.
 * A simple [Fragment] subclass.
 */
internal class PermissionFragment : BasePermissionFragment() {

    companion object {

        private const val TAG = "PermissionManager"

        /**
         * A static method to request permission from activity.
         *
         * @param activity an instance of [AppCompatActivity]
         * @param requestId Request ID for permission request
         * @param permissions Permission(s) to request
         *
         * @return [PermissionResult]
         *
         * Suspends the coroutines until result is available.
         */
        suspend fun requestPermissions(
            activity: AppCompatActivity,
            requestId: Int,
            vararg permissions: String
        ): PermissionResult {
            return withContext(Dispatchers.Main) {
                return@withContext _requestPermissions(
                    activity,
                    requestId,
                    *permissions
                )
            }
        }

        /**
         * A static method to request permission from fragment.
         *
         * @param fragment an instance of [Fragment]
         * @param requestId Request ID for permission request
         * @param permissions Permission(s) to request
         *
         * @return [PermissionResult]
         *
         * Suspends the coroutines until result is available.
         */
        suspend fun requestPermissions(
            fragment: Fragment,
            requestId: Int,
            vararg permissions: String
        ): PermissionResult {
            return withContext(Dispatchers.Main) {
                return@withContext _requestPermissions(
                    fragment,
                    requestId,
                    *permissions
                )
            }
        }

        private suspend fun _requestPermissions(
            activityOrFragment: Any,
            requestId: Int,
            vararg permissions: String
        ): PermissionResult {
            val fragmentManager = if (activityOrFragment is AppCompatActivity) {
                activityOrFragment.supportFragmentManager
            } else {
                (activityOrFragment as Fragment).childFragmentManager
            }
            return if (fragmentManager.findFragmentByTag(TAG) != null) {
                val permissionFragment =
                    fragmentManager.findFragmentByTag(TAG) as PermissionFragment
                permissionFragment.completableDeferred = CompletableDeferred()
                permissionFragment.requestPermissions(requestId, *permissions)
                permissionFragment.completableDeferred.await()
            } else {
                val permissionFragment = PermissionFragment().apply {
                    completableDeferred = CompletableDeferred()
                }
                fragmentManager.beginTransaction().add(
                    permissionFragment,
                    TAG
                ).commitNow()
                permissionFragment.requestPermissions(requestId, *permissions)
                permissionFragment.completableDeferred.await()
            }
        }

        fun hasPermission(context: Context, permission: String): Boolean {
            return if (isSpecialPermission(permission)) {
                hasSpecialPermission(permission)
            } else {
                hasNormalPermission(context, permission)
            }
        }

        private fun isSpecialPermission(permission: String) = when (permission) {
            Manifest.permission.MANAGE_EXTERNAL_STORAGE -> true
            else -> false
        }

        private fun hasNormalPermission(context: Context, permission: String): Boolean {
            return ContextCompat.checkSelfPermission(
                context, permission
            ) == PackageManager.PERMISSION_GRANTED
        }

        @SuppressLint("NewApi")
        private fun hasSpecialPermission(permission: String): Boolean {
            return when (permission) {
                Manifest.permission.MANAGE_EXTERNAL_STORAGE -> Environment.isExternalStorageManager()
                else -> false
            }
        }
    }

    private lateinit var completableDeferred: CompletableDeferred<PermissionResult>

    override fun onPermissionResult(permissionResult: PermissionResult) {
        // When fragment gets recreated due to memory constraints by OS completableDeferred would be
        // uninitialized and hence check
        if (::completableDeferred.isInitialized) {
            completableDeferred.complete(permissionResult)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (::completableDeferred.isInitialized && completableDeferred.isActive) {
            completableDeferred.cancel()
        }
    }
}