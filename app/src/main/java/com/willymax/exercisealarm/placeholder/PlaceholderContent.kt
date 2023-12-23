package com.willymax.exercisealarm.placeholder

import com.willymax.exercisealarm.AddAlarmFragment
import com.willymax.exercisealarm.alarm.AlarmItem
import com.willymax.exercisealarm.utils.AlarmActivities
import java.time.DayOfWeek

/**
 * Helper class for providing sample content for user interfaces created by
 * Android template wizards.
 *
 * TODO: Replace all uses of this class before publishing your app.
 */
object PlaceholderContent {

    /**
     * An array of sample (placeholder) items.
     */
    val ITEMS: MutableList<AlarmItem> = ArrayList()
    val daysOfWeek: MutableList<DayOfWeek> = arrayListOf(
        DayOfWeek.SUNDAY,
        DayOfWeek.MONDAY,
        DayOfWeek.TUESDAY,
        DayOfWeek.WEDNESDAY,
        DayOfWeek.THURSDAY,
        DayOfWeek.FRIDAY,
        DayOfWeek.SATURDAY
    )

    /**
     * A map of sample (placeholder) items, by ID.
     */
    val ITEM_MAP: MutableMap<String, AlarmItem> = HashMap()

    private val COUNT = 1

    init {
        // Add some sample items.
        for (i in 1..COUNT) {
            addItem(createPlaceholderItem(i))
        }
    }

    private fun addItem(item: AlarmItem) {
        ITEMS.add(item)
        ITEM_MAP[item.id] = item
    }

    private fun createPlaceholderItem(position: Int): AlarmItem {
        return AlarmItem(
            position.toString(),
            12,
            30,
            arrayListOf(daysOfWeek[0], daysOfWeek[2]),
            "Meditate",
            AlarmActivities.WALKING,
            repeats = true,
            isOn = true,
            selectedRingtone = "",
            selectedRingtoneFrom = AddAlarmFragment.Companion.RingtoneFrom.DEFAULT
        )
    }

    private fun makeDetails(position: Int): String {
        val builder = StringBuilder()
        builder.append("Details about Item: ").append(position)
        for (i in 0..<position) {
            builder.append("\nMore details information here.")
        }
        return builder.toString()
    }
}