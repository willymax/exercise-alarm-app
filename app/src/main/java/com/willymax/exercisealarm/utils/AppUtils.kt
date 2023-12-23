package com.willymax.exercisealarm.utils

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.DialogInterface
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AlertDialog


object AppUtils {
    fun showMessageDialog(
        activity: Activity,
        title: String?,
        msg: String?,
        positiveBtn: String?,
        negativeBtn: String?,
        positiveListener: DialogInterface.OnClickListener?,
        negativeListener: DialogInterface.OnClickListener?
    ) {
        val builder = AlertDialog.Builder(activity)
        builder.setTitle(title)
        builder.setMessage(msg)
        builder.setPositiveButton(positiveBtn) { dialogInterface, i ->
            positiveListener?.onClick(dialogInterface, i)
        }
        builder.setNegativeButton(negativeBtn) { dialog: DialogInterface?, which: Int ->
            negativeListener?.onClick(dialog, which)
        }
        builder.show()
    }
    // redirect users to playstore
    fun redirectUserToPlayStore(context: Activity, appPackageName: String, referrer: String) {
        try {
            val uri: Uri = Uri.parse("market://details")
                .buildUpon()
                .appendQueryParameter("id", appPackageName)
                .appendQueryParameter("referrer", referrer)
                .build()
            context.startActivity(Intent(Intent.ACTION_VIEW, uri))
        } catch (ignored: ActivityNotFoundException) {
            val uri: Uri = Uri.parse("https://play.google.com/store/apps/details")
                .buildUpon()
                .appendQueryParameter("id", appPackageName)
                .appendQueryParameter("referrer", referrer)
                .build()
            context.startActivity(Intent(Intent.ACTION_VIEW, uri))
        }
    }
}