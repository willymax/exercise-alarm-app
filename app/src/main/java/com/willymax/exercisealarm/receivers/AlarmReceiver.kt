package com.willymax.exercisealarm.receivers

import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.willymax.exercisealarm.R
import com.willymax.exercisealarm.alarm.RightOnPlayAndVibrateService
import com.willymax.exercisealarm.utils.AlarmActivities
import com.willymax.exercisealarm.utils.AppConstants
import com.willymax.exercisealarm.walking.WalkingActivity

// if the user has walked 100 steps, stop the alarm
// else, keep ringing the alarm
// write a pseudo code to ring the alarm
// write a pseudo code to trigger step counter and check if the user has walked 100 steps
class AlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        // get the action from the intent
        val action = intent.action ?: return
        // if the action is not ACTION_START_ALARM, return
        if (action == AppConstants.ACTION_START_ALARM) {
            val alarmActivity = intent.getStringExtra(AppConstants.ALARM_ACTIVITY) ?: return
            if (alarmActivity == AlarmActivities.WALKING.name) {
                RightOnPlayAndVibrateService.startActionPlayRingtone(context, "param1", "param2")
                val extraStepCount = intent.getIntExtra(WalkingActivity.EXTRA_STEP_COUNT, AppConstants.DEFAULT_NUMBER_OF_STEPS)
                val activityIntent = Intent(context, WalkingActivity::class.java).apply {
                    putExtra(WalkingActivity.EXTRA_STEP_COUNT, extraStepCount)
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(activityIntent)
            }
        } else if (action == AppConstants.ACTION_STOP_ALARM) {
            // create intent to destroy the service
            val intent = Intent(context, RightOnPlayAndVibrateService::class.java)
            context.stopService(intent)
        }

//        val channelId = AppConstants.ALARM_CHANNEL_ID
//        context.let { ctx ->
//            val notificationManager =
//                ctx.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
//            val builder = NotificationCompat.Builder(ctx, channelId)
//                .setSmallIcon(R.drawable.ic_launcher_foreground)
//                .setContentTitle("Exercise Alarm")
//                .setContentText("It is time to $alarmActivity")
//                .setPriority(NotificationCompat.PRIORITY_HIGH)
//            notificationManager.notify(1, builder.build())
//        }
    }
}