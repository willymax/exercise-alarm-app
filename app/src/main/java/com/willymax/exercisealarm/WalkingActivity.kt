package com.willymax.exercisealarm

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.willymax.exercisealarm.databinding.ActivityMainBinding
import com.willymax.exercisealarm.databinding.ActivityWalkingBinding

class WalkingActivity : AppCompatActivity() {
    private lateinit var binding: ActivityWalkingBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityWalkingBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)
    }
}