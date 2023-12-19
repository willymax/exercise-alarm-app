package com.willymax.exercisealarm.alarm

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.PowerManager
import android.provider.Settings
import android.util.Log
import com.willymax.exercisealarm.AddAlarmFragment
import com.willymax.exercisealarm.receivers.AlarmReceiver
import com.willymax.exercisealarm.utils.AppConstants
import java.time.ZoneOffset

class AlarmSchedulerImpl(private val context: Context) : AlarmScheduler {
    // create alarm manager
    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    // create power manager
    private val powerManager = context.getSystemService(Context.POWER_SERVICE) as PowerManager

    // create package name
    private val packageName = context.packageName

    override fun scheduleAlarm(alarmItem: AlarmItem) {
        val intent = Intent(context, AlarmReceiver::class.java).apply {
            putExtra(AppConstants.ALARM_ACTIVITY, alarmItem.activity.name)
            putExtra(AppConstants.ALARM_EVENT, alarmItem.event)
            action = AppConstants.ACTION_START_ALARM
        }
        // for each alarm time
        for (alarmTime in alarmItem.getTheAlarmTimes()) {
            // create pending intent with AlarmReceiver
            val pendingIntent = PendingIntent.getBroadcast(
                context,
                alarmItem.hashCode().and(alarmTime.dayOfMonth),
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            // set alarm manager
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (powerManager.isIgnoringBatteryOptimizations(packageName) && alarmManager.canScheduleExactAlarms()) {
                    alarmManager.setExactAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        alarmTime.toEpochSecond(ZoneOffset.UTC) * 1000,
                        pendingIntent
                    )
                } else {
                    intent.action = Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS
                    intent.data = Uri.parse("package:$packageName")
                    context.sendBroadcast(intent)
                }
            } else {
                alarmManager.setExact(
                    AlarmManager.RTC_WAKEUP,
                    alarmTime.toEpochSecond(ZoneOffset.UTC) * 1000,
                    pendingIntent
                )
            }
            Log.d(AddAlarmFragment::class.java.name, "Alarm for set on: $alarmTime")
        }
    }

    override fun cancelAlarm(alarmItem: AlarmItem) {
        for (alarmTime in alarmItem.getTheAlarmTimes()) {
            val intent = Intent(context, AlarmReceiver::class.java)
            val pendingIntent = PendingIntent.getBroadcast(
                context,
                alarmItem.hashCode().and(alarmTime.dayOfMonth),
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            alarmManager.cancel(pendingIntent)
        }
    }
}