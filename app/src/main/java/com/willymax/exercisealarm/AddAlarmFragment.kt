package com.willymax.exercisealarm

import android.app.AlarmManager
import android.content.Context
import android.hardware.SensorManager
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.willymax.exercisealarm.alarm.AlarmItem
import com.willymax.exercisealarm.alarm.AlarmSchedulerImpl
import com.willymax.exercisealarm.databinding.FragmentAddFragmentBinding
import com.willymax.exercisealarm.utils.AlarmActivities
import com.willymax.exercisealarm.utils.SharedPreferencesHelper
import com.willymax.exercisealarm.utils.TimeUtils
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.util.Calendar
import java.util.Date


/**
 * A simple [Fragment] subclass as the second destination in the navigation.
 */
class AddAlarmFragment : Fragment() {

    private var _binding: FragmentAddFragmentBinding? = null
    private var sensorManager: SensorManager? = null
    private var running = false
    private var totalSteps = 0f
    private var previousTotalSteps = 0f
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
        updateRemainingTime()
        return binding.root

    }

    private fun updateRemainingTime() {
        val hourOfDay = binding.alarmTimePicker.hour
        val minute = binding.alarmTimePicker.minute
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.HOUR_OF_DAY, hourOfDay)
        calendar.set(Calendar.MINUTE, minute)
        Log.d(
            AddAlarmFragment::class.java.name,
            "Alarm Time: ${SimpleDateFormat("HH:mm").format(calendar.time)} Millis: ${calendar.timeInMillis}"
        )
        val remainingTime =
            TimeUtils.getDaysHoursMinutes(TimeUtils.getTimeRemainingInMilliseconds(calendar.timeInMillis))
        if (remainingTime.first > 0) {
            binding.alarmAfter.text =
                "Alarm Will Ring In ${remainingTime.first} Days"
        } else if (remainingTime.second > 0) {
            binding.alarmAfter.text =
                "Alarm Will Ring In ${remainingTime.second} Hours ${remainingTime.third} Minutes"
        } else {
            binding.alarmAfter.text =
                "Alarm Will Ring In ${remainingTime.third} Minutes"
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