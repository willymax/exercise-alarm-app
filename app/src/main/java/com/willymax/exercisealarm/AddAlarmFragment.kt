package com.willymax.exercisealarm

import android.app.AlarmManager
import android.content.Context
import android.hardware.SensorManager
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.willymax.exercisealarm.alarm.AlarmItem
import com.willymax.exercisealarm.alarm.AlarmSchedulerImpl
import com.willymax.exercisealarm.databinding.FragmentAddFragmentBinding
import com.willymax.exercisealarm.utils.AlarmActivities
import com.willymax.exercisealarm.utils.SharedPreferencesHelper
import com.willymax.exercisealarm.utils.TimeUtils
import java.text.SimpleDateFormat
import java.time.DayOfWeek
import java.time.LocalDate
import java.util.Calendar
import java.util.Date
import java.util.Locale


/**
 * A simple [Fragment] subclass as the second destination in the navigation.
 */
class AddAlarmFragment : Fragment() {

    private var _binding: FragmentAddFragmentBinding? = null
    private var sensorManager: SensorManager? = null
    private var alarmManager: AlarmManager? = null


    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        sensorManager = activity?.getSystemService(Context.SENSOR_SERVICE) as SensorManager
        alarmManager = activity?.getSystemService(Context.ALARM_SERVICE) as AlarmManager
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAddFragmentBinding.inflate(inflater, container, false)
        binding.alarmTimePicker.setIs24HourView(true)
        binding.alarmTimePicker.setOnTimeChangedListener { view, hourOfDay, minute ->
            run {
                updateRemainingTime()
            }
        }
        binding.alarmRepeatSpinner.setSelection(0)
        binding.alarmRepeatSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                if ("Custom" == parent?.getItemAtPosition(position)) {
                    binding.customWeekdays.visibility = View.VISIBLE
                } else {
                    binding.customWeekdays.visibility = View.GONE
                }
                updateRemainingTime()
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                TODO("Not yet implemented")
            }
        }
        updateRemainingTime()
        return binding.root
    }

    /**
     * Updates the remaining time text view based on the time picker
     * Note: an alarm can repeat on multiple days depending on the repeat spinner. Calculate the remaining time based on the next day the alarm will ring
     */
    private fun updateRemainingTime() {
        var remainingTime: Triple<Long, Long, Long> = Triple(0, 0, 0)
        val hourOfDay = binding.alarmTimePicker.hour
        val minute = binding.alarmTimePicker.minute
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.HOUR_OF_DAY, hourOfDay)
        calendar.set(Calendar.MINUTE, minute)
        Log.d(
            AddAlarmFragment::class.java.name,
            "Alarm Time: ${
                SimpleDateFormat(
                    "HH:mm",
                    Locale.US
                ).format(calendar.time)
            } Millis: ${calendar.timeInMillis}"
        )
        // determine the next day the alarm will ring given that an alarm can repeat once, daily, or weekly
        val selectedItem = binding.alarmRepeatSpinner.selectedItem
        when (selectedItem) {
            "Once" -> {
                remainingTime =
                    TimeUtils.getDaysHoursMinutes(TimeUtils.getTimeRemainingInMilliseconds(calendar.timeInMillis))
            }
            "Everyday" -> {
                remainingTime =
                    TimeUtils.getDaysHoursMinutes(TimeUtils.getTimeRemainingInMilliseconds(calendar.timeInMillis))
            }
            "Weekdays" -> {
                // if the alarm is set to repeat on weekdays, then calculate the remaining time based on the next weekday
                val currentDayOfWeek = LocalDate.now().dayOfWeek
                val daysUntilNextWeekday = when (currentDayOfWeek) {
                    DayOfWeek.MONDAY -> 1
                    DayOfWeek.TUESDAY -> 1
                    DayOfWeek.WEDNESDAY -> 1
                    DayOfWeek.THURSDAY -> 1
                    DayOfWeek.FRIDAY -> 1
                    DayOfWeek.SATURDAY -> 2
                    DayOfWeek.SUNDAY -> 1
                    else -> 1
                }
                 calendar.add(Calendar.DAY_OF_MONTH, daysUntilNextWeekday)
                remainingTime =
                    TimeUtils.getDaysHoursMinutes(TimeUtils.getTimeRemainingInMilliseconds(calendar.timeInMillis))
            }
            "Weekends" -> {
                // if the alarm is set to repeat on weekends, then calculate the remaining time based on the next weekend
                val currentDayOfWeek = LocalDate.now().dayOfWeek
                val daysUntilNextWeekend = when (currentDayOfWeek) {
                    DayOfWeek.MONDAY -> 5
                    DayOfWeek.TUESDAY -> 4
                    DayOfWeek.WEDNESDAY -> 3
                    DayOfWeek.THURSDAY -> 2
                    DayOfWeek.FRIDAY -> 1
                    DayOfWeek.SATURDAY -> 1
                    DayOfWeek.SUNDAY -> 7
                    else -> 1
                }
                if (currentDayOfWeek == DayOfWeek.SATURDAY || currentDayOfWeek == DayOfWeek.SUNDAY) {
                    calendar.add(Calendar.DAY_OF_MONTH, 0)
                } else {
                    calendar.add(Calendar.DAY_OF_MONTH, daysUntilNextWeekend)
                }

                remainingTime =
                    TimeUtils.getDaysHoursMinutes(TimeUtils.getTimeRemainingInMilliseconds(calendar.timeInMillis))
            }
            "Custom" -> {
                // is there a way to group checkboxes together? if so, then get the selected checkboxes and calculate the remaining time based on the next day the alarm will ring

                val selectedDaysOfWeek = binding.monday.isChecked
                remainingTime =
                    TimeUtils.getDaysHoursMinutes(TimeUtils.getTimeRemainingInMilliseconds(calendar.timeInMillis))
            }
        }

        if (remainingTime.first > 0) {
            binding.alarmAfter.text =
                getString(R.string.alarm_will_ring_in, "${remainingTime.first} Days")
        } else if (remainingTime.second > 0) {
            binding.alarmAfter.text =
                getString(
                    R.string.alarm_will_ring_in,
                    "${remainingTime.second} Hours ${remainingTime.third} Minutes"
                )
        } else {
            binding.alarmAfter.text =
                getString(R.string.alarm_will_ring_in, "${remainingTime.third} Minutes")
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.saveAlarmButton.setOnClickListener {
            saveExactAlarm()
        }
    }

    private fun saveExactAlarm() {
        val newAlarmItem = AlarmItem(
            Date().time.toString(),
            binding.alarmTimePicker.hour,
            binding.alarmTimePicker.minute,
            arrayListOf(LocalDate.now().dayOfWeek),
            binding.alarmLabelText.text.toString(),
            AlarmActivities.WALKING,
            repeats = true,
            isOn = true
        )
        Log.d(
            AddAlarmFragment::class.java.name,
            "Save Alarm Button Clicked newAlarmItem: $newAlarmItem"
        )

        val sharedPreferencesHelper = SharedPreferencesHelper(requireContext(), "AlarmList")

        sharedPreferencesHelper.updateList("AlarmList") { list ->
            list + newAlarmItem
        }
        Log.d(
            AddAlarmFragment::class.java.name,
            "Alarm List: ${sharedPreferencesHelper.retrieveList("AlarmList")}"
        )

        AlarmSchedulerImpl(requireActivity()).scheduleAlarm(newAlarmItem)
        findNavController().popBackStack(R.id.AlarmFragment, false)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}