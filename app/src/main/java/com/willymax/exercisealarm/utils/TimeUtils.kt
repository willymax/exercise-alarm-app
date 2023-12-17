package com.willymax.exercisealarm.utils

class TimeUtils {
    companion object {
        // get days, hour and minute given time in milliseconds
        fun getDaysHoursMinutes(time: Long): Triple<Long, Long, Long> {
            val days = time / (1000 * 60 * 60 * 24)
            val hours = time / (1000 * 60 * 60) % 24
            val minutes = time / (1000 * 60) % 60
            return Triple(days, hours, minutes)
        }

        // get time remaining in milliseconds given LocalDateTime object
        fun getTimeRemainingInMilliseconds(time: Long): Long {
            val currentTime = System.currentTimeMillis()
            return time - currentTime
        }
    }
}