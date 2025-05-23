package com.example.doci40.exams.adapters

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.doci40.exams.fragments.ExamsFragment

class ExamsPagerAdapter(activity: FragmentActivity) : FragmentStateAdapter(activity) {

    override fun getItemCount(): Int = 3

    override fun createFragment(position: Int): Fragment {
        return com.example.doci40.exams.fragments.ExamsFragment.newInstance(position + 1)
    }
}