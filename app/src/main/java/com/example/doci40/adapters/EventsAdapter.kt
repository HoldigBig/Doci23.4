package com.example.doci40.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.doci40.databinding.ItemEventBinding
import com.example.doci40.models.EventModel
import java.text.SimpleDateFormat
import java.util.*

class EventsAdapter(
    private val onEventClick: (EventModel) -> Unit
) : ListAdapter<EventModel, EventsAdapter.EventViewHolder>(EventDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EventViewHolder {
        val binding = ItemEventBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return EventViewHolder(binding, onEventClick, this)
    }

    override fun onBindViewHolder(holder: EventViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class EventViewHolder(
        private val binding: ItemEventBinding,
        private val onEventClick: (EventModel) -> Unit,
        private val adapter: EventsAdapter
    ) : RecyclerView.ViewHolder(binding.root) {

        private val timeFormat = SimpleDateFormat("HH:mm", Locale("ru"))

        init {
            binding.root.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onEventClick(adapter.getItem(position))
                }
            }
        }

        fun bind(event: EventModel) {
            binding.apply {
                eventTypeText.text = event.type
                eventTimeText.text = "${timeFormat.format(event.startTime)} - ${timeFormat.format(event.endTime)}"
                eventTitleText.text = event.title
                eventDescriptionText.text = event.description
                eventLocationText.text = event.location
            }
        }
    }

    private class EventDiffCallback : DiffUtil.ItemCallback<EventModel>() {
        override fun areItemsTheSame(oldItem: EventModel, newItem: EventModel): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: EventModel, newItem: EventModel): Boolean {
            return oldItem == newItem
        }
    }
} 