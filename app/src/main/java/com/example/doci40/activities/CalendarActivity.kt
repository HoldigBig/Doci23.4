package com.example.doci40.activities

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.doci40.CalendarPagerAdapter
import com.example.doci40.R
import com.example.doci40.databinding.ActivityCalendarBinding
import com.google.android.material.tabs.TabLayoutMediator

class CalendarActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCalendarBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCalendarBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Set up the ViewPager and TabLayout
        val adapter = CalendarPagerAdapter(this)
        binding.viewPagerCalendar.adapter = adapter

        TabLayoutMediator(binding.tabLayoutCalendar, binding.viewPagerCalendar) {
            tab, position ->
            tab.text = when(position) {
                0 -> "Посещаемость"
                1 -> "Расписание"
                2 -> "События"
                else -> ""
            }
        }.attach()

        // Set TabLayout background color
        binding.tabLayoutCalendar.setBackgroundResource(R.color.background)
    }
} 