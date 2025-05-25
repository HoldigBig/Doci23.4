package com.example.doci40.activities

import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.doci40.adapters.CalendarPagerAdapter
import com.example.doci40.R
import com.example.doci40.databinding.ActivityCalendarBinding
import com.google.android.material.tabs.TabLayoutMediator

class CalendarActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCalendarBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCalendarBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Настройка статус бара
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
            window.statusBarColor = Color.TRANSPARENT
        }

        // Настройка отступов
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(android.R.id.content)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        setupViews()
        setupViewPager()
    }

    private fun setupViews() {
        binding.backButton.setOnClickListener { finish() }
    }

    private fun setupViewPager() {
        val adapter = CalendarPagerAdapter(this)
        binding.viewPager.adapter = adapter

        TabLayoutMediator(binding.tabLayout, binding.viewPager) { tab, position ->
            tab.text = when (position) {
                0 -> "Посещаемость"
                1 -> "Расписание"
                2 -> "События"
                else -> null
            }
        }.attach()

        // Set TabLayout background color
        binding.tabLayout.setBackgroundResource(R.color.background)
    }
} 