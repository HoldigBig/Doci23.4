package com.example.doci40.exams.utils

import java.text.SimpleDateFormat
import java.util.*

object DateUtils {
    fun parseTime(timeString: String): Int {
        val parts = timeString.split(":")
        return if (parts.size == 2) {
            val hours = parts[0].toIntOrNull() ?: 0
            val minutes = parts[1].toIntOrNull() ?: 0
            hours * 60 + minutes
        } else {
            0
        }
    }

    fun formatDate(date: Date): String {
        val dateFormat = SimpleDateFormat("dd.MM.yyyy", Locale("ru"))
        return dateFormat.format(date)
    }

    fun formatTime(date: Date): String {
        val timeFormat = SimpleDateFormat("HH:mm", Locale("ru"))
        return timeFormat.format(date)
    }
} 