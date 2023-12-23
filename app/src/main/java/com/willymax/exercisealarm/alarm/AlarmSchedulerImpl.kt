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
import java.time.ZoneId


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
            putExtra(AppConstants.SELECTED_RINGTONE, alarmItem.selectedRingtone)
            putExtra(AppConstants.SELECTED_RINGTONE_FROM, alarmItem.selectedRingtoneFrom.name)
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
            val systemZone = ZoneId.systemDefault() // my timezone
            val offset = systemZone.rules.getOffset(alarmTime) // offset
            val alarmTimeInMilliSeconds = alarmTime.toEpochSecond(offset) * 1000
            Log.d(AddAlarmFragment::class.java.name, "Alarm for set on S: $alarmTimeInMilliSeconds")
            // set alarm manager
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (powerManager.isIgnoringBatteryOptimizations(packageName) && alarmManager.canScheduleExactAlarms()) {
                    alarmManager.setExactAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        alarmTimeInMilliSeconds,
                        pendingIntent
                    )
                    Log.d(
                        AddAlarmFragment::class.java.name,
                        "Alarm for set on ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS: $alarmTime"
                    )
                } else {
                    val openBatterySettingIntent = Intent()
                    if (powerManager.isIgnoringBatteryOptimizations(context.packageName)) {
                        openBatterySettingIntent.action =
                            Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS
                    } else {
                        // TODO: work on this to comply with Play Store Content Policy
                        openBatterySettingIntent.action =
                            Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS
                        openBatterySettingIntent.data = Uri.parse("package:${context.packageName}")
                    }
                    context.startActivity(openBatterySettingIntent)
                    Log.d(
                        AddAlarmFragment::class.java.name,
                        "Alarm for set on requesting ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS: $alarmTime"
                    )
                }
            } else {
                alarmManager.setExact(
                    AlarmManager.RTC_WAKEUP,
                    alarmTimeInMilliSeconds,
                    pendingIntent
                )
                Log.d(AddAlarmFragment::class.java.name, "Alarm for set on setExact: $alarmTime")
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