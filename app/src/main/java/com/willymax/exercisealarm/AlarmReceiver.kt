package com.willymax.exercisealarm

import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.willymax.exercisealarm.utils.AlarmActivities
import com.willymax.exercisealarm.utils.AppConstants

// if the user has walked 100 steps, stop the alarm
// else, keep ringing the alarm
// write a pseudo code to ring the alarm
// write a pseudo code to trigger step counter and check if the user has walked 100 steps
class AlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val alarmActivity = intent.getStringExtra(AppConstants.ALARM_ACTIVITY) ?: return
        if (alarmActivity == AlarmActivities.WALKING.name) {
            // start the activity
            val activityIntent = Intent(context, WalkingActivity::class.java)
            activityIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(activityIntent)
        } else if (alarmActivity == AlarmActivities.CYCLING.name) {
            // start the activity
        }

        val channelId = AppConstants.ALARM_CHANNEL_ID
        context.let { ctx ->
            val notificationManager =
                ctx.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            val builder = NotificationCompat.Builder(ctx, channelId)
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentTitle("Exercise Alarm")
                .setContentText("It is time to $alarmActivity")
                .setPriority(NotificationCompat.PRIORITY_HIGH)
            notificationManager.notify(1, builder.build())
        }

    }
}