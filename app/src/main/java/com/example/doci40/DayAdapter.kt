package com.example.doci40

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.doci40.databinding.ListItemDayBinding // Импортируем сгенерированный класс привязки для list_item_day.xml
import java.util.Calendar

class DayAdapter(private val onDayClickListener: (DayItem) -> Unit) : RecyclerView.Adapter<DayAdapter.DayViewHolder>() {

    private var dayList = emptyList<DayItem>()
    private var selectedItemPosition = RecyclerView.NO_POSITION
    private var currentDayPosition = RecyclerView.NO_POSITION

    fun setData(newList: List<DayItem>) {
        dayList = newList
        findCurrentDayPosition() // Находим позицию текущего дня
        notifyDataSetChanged()
    }

    private fun findCurrentDayPosition() {
        val todayCalendar = Calendar.getInstance()
        todayCalendar.set(Calendar.HOUR_OF_DAY, 0)
        todayCalendar.set(Calendar.MINUTE, 0)
        todayCalendar.set(Calendar.SECOND, 0)
        todayCalendar.set(Calendar.MILLISECOND, 0)
        val todayMillis = todayCalendar.timeInMillis

        currentDayPosition = dayList.indexOfFirst { dayItem ->
            val itemCalendar = Calendar.getInstance()
            itemCalendar.timeInMillis = dayItem.date
            itemCalendar.set(Calendar.HOUR_OF_DAY, 0)
            itemCalendar.set(Calendar.MINUTE, 0)
            itemCalendar.set(Calendar.SECOND, 0)
            itemCalendar.set(Calendar.MILLISECOND, 0)
            itemCalendar.timeInMillis == todayMillis
        }
        if (currentDayPosition != RecyclerView.NO_POSITION) {
            selectedItemPosition = currentDayPosition // Выделяем текущий день по умолчанию
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DayViewHolder {
        val binding = ListItemDayBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return DayViewHolder(binding, onDayClickListener)
    }

    override fun onBindViewHolder(holder: DayViewHolder, position: Int) {
        val dayItem = dayList[position]
        holder.bind(dayItem, position == selectedItemPosition, position == currentDayPosition)
    }

    override fun getItemCount(): Int = dayList.size

    fun selectItem(position: Int) {
        if (selectedItemPosition != RecyclerView.NO_POSITION) {
            notifyItemChanged(selectedItemPosition)
        }
        selectedItemPosition = position
        notifyItemChanged(selectedItemPosition)
    }

    inner class DayViewHolder(private val binding: ListItemDayBinding, private val onDayClickListener: (DayItem) -> Unit) : RecyclerView.ViewHolder(binding.root) {
        init {
            itemView.setOnClickListener { // Обработчик нажатия на элемент дня
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    selectItem(position)
                    onDayClickListener.invoke(dayList[position])
                }
            }
        }

        fun bind(dayItem: DayItem, isSelected: Boolean, isCurrentDay: Boolean) {
            binding.textViewDayOfWeek.text = dayItem.dayOfWeek
            binding.textViewDayNumber.text = dayItem.dayNumber

            // Логика выделения текущего дня и выбранного дня
            if (isCurrentDay) {
                binding.textViewDayOfWeek.setTextColor(Color.BLUE) // Цвет для текущего дня (пример)
                binding.textViewDayNumber.setTextColor(Color.BLUE) // Цвет для текущего дня (пример)
            } else if (isSelected) {
                binding.textViewDayOfWeek.setTextColor(Color.BLACK) // Цвет для выбранного дня (пример)
                binding.textViewDayNumber.setTextColor(Color.BLACK) // Цвет для выбранного дня (пример)
            } else {
                binding.textViewDayOfWeek.setTextColor(Color.GRAY) // Цвет по умолчанию (пример)
                binding.textViewDayNumber.setTextColor(Color.GRAY) // Цвет по умолчанию (пример)
            }
            // Возможно, потребуется изменить фон или добавить другие визуальные индикаторы
        }
    }
} 