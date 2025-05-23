package com.example.doci40.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.doci40.R
import com.example.doci40.databinding.ItemMonthlyAttendanceBinding
import com.example.doci40.models.AttendanceSummary

class MonthlyAttendanceAdapter(private val onItemClick: (AttendanceSummary) -> Unit) :
    ListAdapter<AttendanceSummary, MonthlyAttendanceAdapter.MonthlyAttendanceViewHolder>(AttendanceDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int):
            MonthlyAttendanceViewHolder {
        val binding = ItemMonthlyAttendanceBinding.inflate(
            LayoutInflater.from(parent.context),
            parent, false
        )
        return MonthlyAttendanceViewHolder(binding, onItemClick)
    }

    override fun onBindViewHolder(holder: MonthlyAttendanceViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class MonthlyAttendanceViewHolder(
        private val binding: ItemMonthlyAttendanceBinding,
        private val onItemClick: (AttendanceSummary) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(attendanceSummary: AttendanceSummary) {
            binding.tvMonth.text = attendanceSummary.month
            binding.tvPresentPercentage.text = binding.root.context.getString(
                R.string.attendance_present_percentage,
                attendanceSummary.presentPercentage
            )
            binding.tvAbsentPercentage.text = binding.root.context.getString(
                R.string.attendance_absent_percentage,
                attendanceSummary.absentPercentage
            )
            binding.tvViewDetails.setOnClickListener { onItemClick(attendanceSummary) }
        }
    }

    private class AttendanceDiffCallback :
        DiffUtil.ItemCallback<AttendanceSummary>() {
        override fun areItemsTheSame(
            oldItem: AttendanceSummary,
            newItem: AttendanceSummary
        ): Boolean {
            return oldItem.month == newItem.month // Assuming month is unique identifier
        }

        override fun areContentsTheSame(
            oldItem: AttendanceSummary,
            newItem: AttendanceSummary
        ): Boolean {
            return oldItem == newItem
        }
    }
} 