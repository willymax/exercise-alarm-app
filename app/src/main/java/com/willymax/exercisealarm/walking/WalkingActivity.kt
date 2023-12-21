package com.willymax.exercisealarm.walking

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.willymax.exercisealarm.R
import com.willymax.exercisealarm.databinding.ActivityWalkingBinding

class WalkingActivity : AppCompatActivity() {
    private lateinit var binding: ActivityWalkingBinding

    companion object {
        private const val TAG = "WalkingActivity"
        const val EXTRA_STEP_COUNT = "extra_step_count"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityWalkingBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)
        if (savedInstanceState == null) {
            val stepCount = intent.getIntExtra(EXTRA_STEP_COUNT, 100)
            supportFragmentManager.beginTransaction()
                .replace(
                    R.id.nav_host_fragment_content_main,
                    StepCounterFragment.newInstance(stepCount)
                )
                .commitNow()
        }
        // check if user has allowed location permission

        // if not, request for it
        // if yes, start location updates
        // if location updates are already running, do nothing

    }
}