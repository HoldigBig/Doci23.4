package com.example.doci40.homework.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.doci40.R
import com.example.doci40.homework.models.DayItem

class DayAdapter(private var days: List<DayItem>) : RecyclerView.Adapter<DayAdapter.DayViewHolder>() {
    private var selectedDay: DayItem? = null
    private var listener: DayClickListener? = null

    interface DayClickListener {
        fun onDayClick(dayItem: DayItem)
    }

    fun setOnDayClickListener(listener: DayClickListener) {
        this.listener = listener
    }

    fun setSelectedDay(day: DayItem) {
        val oldSelectedPosition = days.indexOfFirst { it.isSelected }
        if (oldSelectedPosition != -1) {
            days[oldSelectedPosition].isSelected = false
            notifyItemChanged(oldSelectedPosition)
        }

        val newSelectedPosition = days.indexOf(day)
        if (newSelectedPosition != -1) {
            days[newSelectedPosition].isSelected = true
            selectedDay = days[newSelectedPosition]
            notifyItemChanged(newSelectedPosition)
        }
    }

    fun getSelectedDay(): DayItem? = selectedDay

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DayViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_day, parent, false)
        return DayViewHolder(view)
    }

    override fun onBindViewHolder(holder: DayViewHolder, position: Int) {
        holder.bind(days[position])
    }

    override fun getItemCount(): Int = days.size

    inner class DayViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val dayOfWeek: TextView = itemView.findViewById(R.id.dayOfWeek)
        private val dayNumber: TextView = itemView.findViewById(R.id.dayNumber)

        init {
            itemView.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    val day = days[position]
                    setSelectedDay(day)
                    listener?.onDayClick(day)
                }
            }
        }

        fun bind(day: DayItem) {
            dayOfWeek.text = day.dayOfWeek
            dayNumber.text = day.dayNumber
            dayNumber.isSelected = day.isSelected
        }
    }
} 