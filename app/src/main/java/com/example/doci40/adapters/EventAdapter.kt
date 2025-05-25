package com.example.doci40.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.doci40.R
import com.example.doci40.databinding.ItemEventBinding
import com.example.doci40.models.Event

class EventAdapter(
    private val onItemClick: (Event) -> Unit
) : ListAdapter<Event, EventAdapter.EventViewHolder>(EventDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EventViewHolder {
        val binding = ItemEventBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return EventViewHolder(binding)
    }

    override fun onBindViewHolder(holder: EventViewHolder, position: Int) {
        holder.bind(getItem(position), onItemClick)
    }

    class EventViewHolder(
        private val binding: ItemEventBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(event: Event, onItemClick: (Event) -> Unit) {
            with(binding) {
                titleText.text = event.title
                descriptionText.text = event.description
                dateTimeText.text = "${event.date} ${event.startTime} - ${event.endTime}"
                locationText.text = event.location
                
                // Настройка приоритета
                priorityText.text = event.priority
                priorityText.setBackgroundResource(
                    when (event.priority) {
                        "Высокий" -> R.color.priority_high
                        "Средний" -> R.color.priority_medium
                        else -> R.color.priority_low
                    }
                )

                root.setOnClickListener {
                    onItemClick(event)
                }
            }
        }
    }

    private class EventDiffCallback : DiffUtil.ItemCallback<Event>() {
        override fun areItemsTheSame(oldItem: Event, newItem: Event): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Event, newItem: Event): Boolean {
            return oldItem == newItem
        }
    }
} 