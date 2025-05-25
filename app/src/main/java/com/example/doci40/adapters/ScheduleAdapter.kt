package com.example.doci40.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.doci40.databinding.ItemScheduleBinding
import com.example.doci40.models.Schedule

class ScheduleAdapter(
    private val onItemClick: (Schedule) -> Unit
) : ListAdapter<Schedule, ScheduleAdapter.ScheduleViewHolder>(ScheduleDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ScheduleViewHolder {
        val binding = ItemScheduleBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ScheduleViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ScheduleViewHolder, position: Int) {
        holder.bind(getItem(position), onItemClick)
    }

    class ScheduleViewHolder(
        private val binding: ItemScheduleBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(schedule: Schedule, onItemClick: (Schedule) -> Unit) {
            with(binding) {
                timeText.text = "${schedule.startTime} - ${schedule.endTime}"
                typeText.text = schedule.type
                titleText.text = schedule.subject
                teacherText.text = schedule.teacher
                roomText.text = "ауд. ${schedule.room}"
                weekTypeText.text = schedule.weekType

                root.setOnClickListener {
                    onItemClick(schedule)
                }
            }
        }
    }

    private class ScheduleDiffCallback : DiffUtil.ItemCallback<Schedule>() {
        override fun areItemsTheSame(oldItem: Schedule, newItem: Schedule): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Schedule, newItem: Schedule): Boolean {
            return oldItem == newItem
        }
    }
} 