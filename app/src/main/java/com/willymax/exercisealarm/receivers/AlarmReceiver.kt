package com.willymax.exercisealarm.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
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
        Log.d("AlarmReceiver", "onReceive")
        val action = intent.action ?: return
        Log.d("AlarmReceiver", "action: $action")
        // if the action is not ACTION_START_ALARM, return
        if (action == AppConstants.ACTION_START_ALARM) {
            val alarmActivity = intent.getStringExtra(AppConstants.ALARM_ACTIVITY) ?: return
            if (alarmActivity == AlarmActivities.WALKING.name) {
                val selectedRingtone = intent.getStringExtra(AppConstants.SELECTED_RINGTONE) ?: ""
                val selectedRingtoneFrom = intent.getStringExtra(AppConstants.SELECTED_RINGTONE_FROM) ?: ""
                RightOnPlayAndVibrateService.startActionPlayRingtone(context, selectedRingtone, selectedRingtoneFrom)
                val extraStepCount = intent.getIntExtra(
                    WalkingActivity.EXTRA_STEP_COUNT,
                    AppConstants.DEFAULT_NUMBER_OF_STEPS
                )
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
    }
}