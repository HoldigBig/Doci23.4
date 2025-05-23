package com.example.doci40.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.doci40.databinding.ItemScheduleBinding
import com.example.doci40.models.ScheduleModel

class ScheduleAdapter(
    private val onItemClick: (ScheduleModel) -> Unit
) : ListAdapter<ScheduleModel, ScheduleAdapter.ScheduleViewHolder>(ScheduleDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ScheduleViewHolder {
        val binding = ItemScheduleBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ScheduleViewHolder(binding, this)
    }

    override fun onBindViewHolder(holder: ScheduleViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class ScheduleViewHolder(
        private val binding: ItemScheduleBinding,
        private val adapter: ScheduleAdapter
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(schedule: ScheduleModel) {
            with(binding) {
                timeText.text = "${schedule.startTime} - ${schedule.endTime}"
                typeText.text = schedule.type
                titleText.text = schedule.title
                teacherText.text = schedule.teacher
                roomText.text = schedule.room
                weekTypeText.text = schedule.weekType

                root.setOnClickListener {
                    val position = adapterPosition
                    if (position != RecyclerView.NO_POSITION) {
                        adapter.onItemClick(adapter.getItem(position))
                    }
                }
            }
        }
    }

    private class ScheduleDiffCallback : DiffUtil.ItemCallback<ScheduleModel>() {
        override fun areItemsTheSame(oldItem: ScheduleModel, newItem: ScheduleModel): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: ScheduleModel, newItem: ScheduleModel): Boolean {
            return oldItem == newItem
        }
    }
} 