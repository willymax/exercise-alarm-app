package com.willymax.exercisealarm.alarm

import android.util.Log
import com.willymax.exercisealarm.AddAlarmFragment
import com.willymax.exercisealarm.utils.AlarmActivities
import java.time.Clock
import java.time.DayOfWeek
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit

// extended parcelable
data class AlarmItem(
    val id: String,
    val hour: Int,// time in 24 hour format
    val minute: Int,// time in 24 hour format
    val daysOfWeek: MutableList<DayOfWeek?> = arrayListOf(DayOfWeek.SUNDAY),// list of days of week
    val event: String,
    val activity: AlarmActivities,
    val repeats: Boolean,
    val isOn: Boolean,
    val selectedRingtone: String?,
    val selectedRingtoneFrom: AddAlarmFragment.Companion.RingtoneFrom
) {
    // calculate alarm time and return it
    fun getTheAlarmTimes(): List<LocalDateTime> {
        // create a list of LocalDateTime objects using the time and daysOfWeek
        val alarmTimes = mutableListOf<LocalDateTime>()
        // for each day of week
        for (currentDayOfWeek in daysOfWeek) {
            // create a LocalDateTime object for that day of week
            var alarmTime = LocalDateTime.now(Clock.systemDefaultZone())
            // set the day of week
            alarmTime = alarmTime.plusDays(currentDayOfWeek?.let {
                calculateDaysRemaining(
                    it,
                    hour,
                    minute
                )
            } ?: 0)
            // set the hour and minute
            alarmTime = alarmTime.withHour(hour)
            alarmTime = alarmTime.withMinute(minute)
            alarmTime = alarmTime.withSecond(0)
            // add the alarm time to the list
            // log the alarm time
            Log.d(AlarmItem::class.java.name, "Alarm Time: $alarmTime")
            alarmTimes.add(alarmTime)
        }
        return alarmTimes
    }

    private fun calculateDaysRemaining(day: DayOfWeek, hour: Int, minute: Int): Long {
        val now = LocalDateTime.now()
        var then = now.with(DayOfWeek.from(day)).withHour(hour).withMinute(minute).withSecond(0)
            .withNano(0)

        if (then.isBefore(now) || then.isEqual(now)) {
            then = then.plusWeeks(1)
        }

        return ChronoUnit.DAYS.between(now, then)
    }

    override fun toString(): String =
        "${hour}:${minute}:${daysOfWeek}:${event}:${activity}:${repeats}:${isOn}"
}
