package com.example.doci40

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.doci40.calendar.fragments.AttendanceFragment
import com.example.doci40.calendar.fragments.EventsFragment
import com.example.doci40.calendar.fragments.TimetableFragment

class CalendarPagerAdapter(activity: FragmentActivity) : FragmentStateAdapter(activity) {
    override fun getItemCount(): Int = 3

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> AttendanceFragment()
            1 -> TimetableFragment()
            2 -> EventsFragment()
            else -> throw IllegalArgumentException("Invalid position $position")
        }
    }
} 