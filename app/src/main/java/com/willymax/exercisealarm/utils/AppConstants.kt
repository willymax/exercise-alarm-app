package com.willymax.exercisealarm.utils

class AppConstants {
    companion object {
        const val ACTION_START_ALARM: String = "com.willymax.exercisealarm.alarm.action.START_ALARM"
        const val ACTION_STOP_ALARM: String = "com.willymax.exercisealarm.alarm.action.STOP_ALARM"
        const val ALARM_CHANNEL_ID: String = "alarm_channel_id"
        const val ALARM_EVENT: String = "alarm_event"
        const val ALARM_ACTIVITY: String = "alarm_activity"
        const val EXTRA_STEP_COUNT: String = "extra_step_count"
        const val STEPS_COUNT_PREFS: String = "steps_count_prefs"
        const val DEFAULT_NUMBER_OF_STEPS: Int = 100
    }
}