package com.willymax.exercisealarm.utils

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.material.snackbar.Snackbar
import com.willymax.exercisealarm.R
import com.willymax.exercisealarm.data.SharedPref

object PermissionUtil {
    /* Permission required for application */
    private val ALL_APPLICATION_PERMISSIONS = arrayOf(
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            Manifest.permission.ACTIVITY_RECOGNITION
        } else {
            "com.google.android.gms.permission.ACTIVITY_RECOGNITION"
        }
    )

    fun goToPermissionSettingScreen(activity: Activity) {
        val intent = Intent(
            Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
            Uri.fromParts("package", activity.packageName, null)
        )
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        activity.startActivity(intent)
    }

    fun isAllPermissionGranted(activity: Activity?): Boolean {
        val permission = ALL_APPLICATION_PERMISSIONS
        if (permission.isEmpty()) return false
        for (s in permission) {
            if (ActivityCompat.checkSelfPermission(
                    activity!!,
                    s
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                return false
            }
        }
        return true
    }

    fun getDeniedPermission(act: Activity): Array<String> {
        val permissions: MutableList<String> = ArrayList()
        for (i in ALL_APPLICATION_PERMISSIONS.indices) {
            val status = act.checkSelfPermission(ALL_APPLICATION_PERMISSIONS[i])
            if (status != PackageManager.PERMISSION_GRANTED) {
                permissions.add(ALL_APPLICATION_PERMISSIONS[i])
            }
        }
        return permissions.toTypedArray()
    }

    fun isGranted(ctx: Context, permission: String?): Boolean {
        return ctx.checkSelfPermission(permission!!) == PackageManager.PERMISSION_GRANTED
    }

    fun checkAndRequestNotification(act: Activity) {
        val sharedPref = SharedPref(act)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    act,
                    Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_DENIED
                && !sharedPref.getNeverAskAgain(Manifest.permission.POST_NOTIFICATIONS)
            ) {
                val permissions = arrayOf(Manifest.permission.POST_NOTIFICATIONS)
                act.requestPermissions(permissions, 200)
                sharedPref.setNeverAskAgain(Manifest.permission.POST_NOTIFICATIONS, true)
            }
        }
    }

    fun showSystemDialogPermission(act: Activity, perm: String) {
        act.requestPermissions(arrayOf(perm), 200)
    }

    fun showSystemDialogPermission(act: Activity, perm: String, code: Int) {
        act.requestPermissions(arrayOf(perm), code)
    }

    fun showConfirmDialog(
        activity: Activity,
        title: String?,
        msg: String?,
        permissions: Array<String?>?,
        requestCode: Int
    ) {
        val builder = AlertDialog.Builder(activity)
        builder.setTitle(title)
        builder.setMessage(msg)
        builder.setPositiveButton(R.string.ALLOW) { dialogInterface, i ->
            ActivityCompat.requestPermissions(
                activity,
                permissions!!,
                requestCode
            )
        }
        builder.setNegativeButton(R.string.DENY) { dialog: DialogInterface?, which: Int ->
            Snackbar.make(
                activity.findViewById(android.R.id.content),
                R.string.permission_denied,
                Snackbar.LENGTH_SHORT
            ).show()
        }
        builder.show()
    }
}