package com.example.doci40

import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.example.doci40.databinding.ActivityCalendarBinding
import com.google.android.material.tabs.TabLayoutMediator

class CalendarActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCalendarBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCalendarBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Устанавливаем цвет иконок статус бара на черный
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
            window.statusBarColor = Color.TRANSPARENT // Опционально, если нужен прозрачный статус бар
        }

        setupToolbar()
        setupViewPagerAndTabs()
    }

    private fun setupToolbar() {
        binding.toolbarCalendar.setNavigationOnClickListener { finish() }
    }

    private fun setupViewPagerAndTabs() {
        val adapter = CalendarPagerAdapter(this)
        binding.viewPagerCalendar.adapter = adapter

        TabLayoutMediator(binding.tabLayoutCalendar, binding.viewPagerCalendar) { tab, position ->
            tab.text = when (position) {
                0 -> "Посещаемость"
                1 -> "Расписание"
                2 -> "События"
                else -> null
            }
        }.attach()
    }
} 