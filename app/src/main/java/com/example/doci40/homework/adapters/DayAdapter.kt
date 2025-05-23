package com.example.doci40.homework.adapters

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.doci40.R
import com.example.doci40.homework.models.DayItem
import com.example.doci40.databinding.ListItemDayBinding

class DayAdapter(private val days: List<DayItem>) : RecyclerView.Adapter<DayAdapter.DayViewHolder>() {

    interface DayClickListener {
        fun onDayClick(dayItem: DayItem)
    }

    private var clickListener: DayClickListener? = null
    var selectedPosition: Int = -1
        private set

    fun setOnDayClickListener(listener: DayClickListener) {
        clickListener = listener
    }

    fun getSelectedDay(): DayItem? {
        return if (selectedPosition != -1 && selectedPosition < days.size) {
            days[selectedPosition]
        } else {
            null
        }
    }

    inner class DayViewHolder(private val binding: ListItemDayBinding) : RecyclerView.ViewHolder(binding.root) {
        init {
            itemView.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    val previouslySelectedPosition = selectedPosition
                    selectedPosition = position
                    notifyItemChanged(previouslySelectedPosition)
                    notifyItemChanged(selectedPosition)
                    clickListener?.onDayClick(days[position])
                }
            }
        }

        fun bind(dayItem: DayItem, isSelected: Boolean) {
            binding.textViewDayOfWeek.text = dayItem.dayOfWeek
            binding.textViewDayNumber.text = dayItem.dayNumber

            // Скрываем номер дня для элемента "Все"
            binding.textViewDayNumber.visibility = if (dayItem.date == -1L) ViewGroup.GONE else ViewGroup.VISIBLE

            val textColor = if (isSelected) Color.BLACK else Color.GRAY
            binding.textViewDayOfWeek.setTextColor(textColor)
            binding.textViewDayNumber.setTextColor(textColor)

            // Добавляем визуальное выделение выбранного элемента
            itemView.setBackgroundResource(if (isSelected) R.drawable.bg_selected_day else android.R.color.transparent)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DayViewHolder {
        val binding = ListItemDayBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return DayViewHolder(binding)
    }

    override fun onBindViewHolder(holder: DayViewHolder, position: Int) {
        val dayItem = days[position]
        holder.bind(dayItem, position == selectedPosition)
    }

    override fun getItemCount(): Int = days.size

    fun setSelectedDay(dayItem: DayItem) {
        val position = days.indexOf(dayItem)
        if (position != -1) {
            val previouslySelectedPosition = selectedPosition
            selectedPosition = position
            notifyItemChanged(previouslySelectedPosition)
            notifyItemChanged(selectedPosition)
        }
    }
} 