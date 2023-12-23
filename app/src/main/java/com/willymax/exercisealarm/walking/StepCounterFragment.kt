package com.willymax.exercisealarm.walking

import android.content.Context
import android.content.Intent
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.willymax.exercisealarm.alarm.RightOnPlayAndVibrateService
import com.willymax.exercisealarm.databinding.FragmentStepCounterBinding
import com.willymax.exercisealarm.utils.AppConstants

/**
 * A simple [Fragment] subclass.
 * Use the [StepCounterFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
private const val NUMBER_OF_STEPS_NEEDED = "numberOfStepsNeeded"
private const val PREVIOUS_TOTAL_STEPS = "previousTotalSteps"

class StepCounterFragment : Fragment(), SensorEventListener {
    private var _binding: FragmentStepCounterBinding? = null
    private var sensorManager: SensorManager? = null
    private var numberOfStepsNeeded: Int = AppConstants.DEFAULT_NUMBER_OF_STEPS
    private var running = false
    private var totalSteps = 0f
    private var previousTotalSteps = 0f

    private val alpha = 0.8f
    private val gravity = FloatArray(3)

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            numberOfStepsNeeded = it.getInt(NUMBER_OF_STEPS_NEEDED)
        }
        sensorManager = activity?.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentStepCounterBinding.inflate(inflater, container, false)
        loadData()
        resetSteps()
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.tvTargetSteps.text = numberOfStepsNeeded.toString()
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param numberOfSteps Parameter 1.
         * @return A new instance of fragment StepCounterFragment.
         */
        @JvmStatic
        fun newInstance(numberOfSteps: Int) =
            StepCounterFragment().apply {
                arguments = Bundle().apply {
                    putInt(NUMBER_OF_STEPS_NEEDED, numberOfSteps)
                }
            }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        running = false
    }

    override fun onResume() {
        super.onResume()
        running = true
        val stepSensor = sensorManager?.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)
        if (stepSensor == null) {
            Toast.makeText(activity, "No sensor detected on this device", Toast.LENGTH_SHORT).show()
        } else {
            sensorManager?.registerListener(this, stepSensor, SensorManager.SENSOR_DELAY_UI)
        }
    }

    override fun onPause() {
        super.onPause()
        val stepSensor = sensorManager?.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)
        if (stepSensor != null) {
            sensorManager?.unregisterListener(this, stepSensor)
        }
    }

    override fun onSensorChanged(event: SensorEvent?) {
        Log.d(StepCounterFragment::class.java.name, "onSensorChanged")
        val tvStepsTaken = binding.tvStepsTaken
        if (running && event != null && event.sensor.type == Sensor.TYPE_STEP_COUNTER) {
            Log.d(StepCounterFragment::class.java.name, "onSensorChanged: TYPE_STEP_COUNTER")
            totalSteps = event.values[0]
            if (previousTotalSteps == 0f) {
                previousTotalSteps = totalSteps
            }
            val currentSteps = totalSteps.toInt() - previousTotalSteps.toInt()
            tvStepsTaken.text = ("$currentSteps")
            if (currentSteps >= numberOfStepsNeeded) {
                val intent = Intent(activity, RightOnPlayAndVibrateService::class.java)
                activity?.stopService(intent)
                Toast.makeText(activity, "You have reached your goal", Toast.LENGTH_SHORT).show()
                tvStepsTaken.text = 0.toString()
                previousTotalSteps = totalSteps
                saveData()
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {

    }

    private fun resetSteps() {
        val tvStepsTaken = binding.tvStepsTaken
        tvStepsTaken.setOnClickListener {
            Toast.makeText(activity, "Long tap to reset steps", Toast.LENGTH_SHORT).show()
        }
        tvStepsTaken.setOnLongClickListener {
            previousTotalSteps = totalSteps
            tvStepsTaken.text = 0.toString()
            saveData()
            true
        }
    }

    private fun saveData() {
        val sharedPreferences =
            requireActivity().getSharedPreferences(
                AppConstants.STEPS_COUNT_PREFS,
                Context.MODE_PRIVATE
            )
        val editor = sharedPreferences.edit()
        editor.putFloat(PREVIOUS_TOTAL_STEPS, previousTotalSteps)
        editor.apply()
    }

    private fun loadData() {
        val sharedPreferences =
            requireActivity().getSharedPreferences(
                AppConstants.STEPS_COUNT_PREFS,
                Context.MODE_PRIVATE
            )
        val savedNumber = sharedPreferences.getFloat(PREVIOUS_TOTAL_STEPS, 0f)
        Log.d(StepCounterFragment::class.java.name, "$savedNumber")
        previousTotalSteps = savedNumber
    }
}