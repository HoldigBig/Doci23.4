package com.example.doci40

data class DayItem(
    val dayOfWeek: String,
    val dayNumber: String,
    val date: Long // Добавляем поле для хранения полной даты (timestamp)
) 