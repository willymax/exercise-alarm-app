package com.willymax.exercisealarm

import android.content.Context
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
import com.willymax.exercisealarm.databinding.FragmentStepCounterBinding
import com.willymax.exercisealarm.utils.AppConstants

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val NUMBER_OF_STEPS_NEEDED = "numberOfStepsNeeded"

/**
 * A simple [Fragment] subclass.
 * Use the [StepCounterFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class StepCounterFragment : Fragment(), SensorEventListener {
    private var _binding: FragmentStepCounterBinding? = null
    private var sensorManager: SensorManager? = null
    private var numberOfStepsNeeded: Int? = null
    private var running = false
    private var totalSteps = 0f
    private var previousTotalSteps = 0f

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

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param numberOfSteps Parameter 1.
         * @return A new instance of fragment StepCounterFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(numberOfSteps: String) =
            StepCounterFragment().apply {
                arguments = Bundle().apply {
                    putString(NUMBER_OF_STEPS_NEEDED, numberOfSteps)
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
        val tvStepsTaken = binding.tvStepsTaken
        if (running && event != null && event.sensor.type == Sensor.TYPE_STEP_COUNTER) {
            totalSteps = event.values[0]
            val currentSteps = totalSteps.toInt() - previousTotalSteps.toInt()
            tvStepsTaken.text = ("$currentSteps")
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
            requireActivity().getSharedPreferences(AppConstants.STEPS_COUNT_PREFS, Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putFloat("previousTotalSteps", previousTotalSteps)
        editor.apply()
    }

    private fun loadData() {
        val sharedPreferences =
            requireActivity().getSharedPreferences(AppConstants.STEPS_COUNT_PREFS, Context.MODE_PRIVATE)
        val savedNumber = sharedPreferences.getFloat("previousTotalSteps", 0f)
        Log.d(StepCounterFragment::class.java.name, "$savedNumber")
        previousTotalSteps = savedNumber
    }
}