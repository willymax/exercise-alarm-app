package com.willymax.exercisealarm

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.switchmaterial.SwitchMaterial
import com.willymax.exercisealarm.alarm.AlarmItem
import com.willymax.exercisealarm.databinding.FragmentItemBinding

/**
 * [RecyclerView.Adapter] that can display a [AlarmItem].
 * TODO: Replace the implementation with code for your data type.
 */
class MyAlarmRecyclerViewAdapter(
    private val values: List<AlarmItem>,
) : RecyclerView.Adapter<MyAlarmRecyclerViewAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {

        return ViewHolder(
            FragmentItemBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )

    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = values[position]
        Log.d("MyAlarmRecyclerViewAdapter", "item: $item")
        holder.alarmTime.text = item.getAlarmTimeStr()
        holder.alarmDayOfWeek.text = item.daysOfWeek.joinToString(", ") { dayOfWeek ->
            dayOfWeek?.name?.substring(0, 3) ?: ""
        }
        holder.alarmEvent.text = item.event
        holder.switchAlarm.isChecked = true
    }

    override fun getItemCount(): Int = values.size

    inner class ViewHolder(binding: FragmentItemBinding) : RecyclerView.ViewHolder(binding.root) {
        val alarmTime: TextView = binding.alarmTime
        val alarmDayOfWeek: TextView = binding.alarmDayOfWeek
        val alarmEvent: TextView = binding.alarmEvent
        val switchAlarm: SwitchMaterial = binding.switchAlarm

        override fun toString(): String {
            return super.toString() + " '" + alarmTime.text + "'"
        }
    }

}