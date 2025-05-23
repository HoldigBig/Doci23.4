package com.example.doci40.exams.models

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

data class ExamModel(
    val examId: String = "",
    val subject: String = "",
    val date: String = "",
    val startTime: String = "",
    val endTime: String = "",
    val location: String = "",
    val examiner: String = "",
    val type: String = "",
    val duration: Int = 0,
    val isActive: Boolean = true,
    val semester: Int = 1
) {
    // Получение дня недели
    val dayOfWeek: String
        get() {
            return try {
                val parsedDate = dateFormatter.parse(date)
                val calendar = Calendar.getInstance().apply {
                    time = parsedDate ?: return "???"
                }
                val days = arrayOf("???", "Вс", "Пн", "Вт", "Ср", "Чт", "Пт", "Сб")
                days[calendar.get(Calendar.DAY_OF_WEEK)]
            } catch (e: Exception) {
                "???"
            }
        }

    // Получение числа месяца
    val dayNumber: String
        get() {
            return try {
                val parsedDate = dateFormatter.parse(date)
                val calendar = Calendar.getInstance().apply {
                    time = parsedDate ?: return "??"
                }
                calendar.get(Calendar.DAY_OF_MONTH).toString()
            } catch (e: Exception) {
                "??"
            }
        }

    // Получение времени экзамена
    val time: String
        get() = "$startTime - $endTime"

    fun isValid(): Boolean {
        return examId.isNotBlank() &&
                subject.isNotBlank() &&
                date.isNotBlank() &&
                startTime.isNotBlank() &&
                endTime.isNotBlank() &&
                semester > 0
    }

    companion object {
        // Предметы
        val SUBJECTS = listOf(
            "Математика",
            "Физика",
            "Информатика",
            "История",
            "Литература",
            "Английский язык",
            "Химия",
            "Биология",
            "География",
            "Обществознание"
        )

        // Типы экзаменов
        val EXAM_TYPES = listOf(
            "Экзамен",
            "Зачет",
            "Дифференцированный зачет",
            "Курсовая работа",
            "Контрольная работа"
        )

        private val dateFormatter = SimpleDateFormat("dd.MM.yyyy", Locale("ru"))
        private val timeFormatter = SimpleDateFormat("HH:mm", Locale("ru"))
        private val fullDateFormatter = SimpleDateFormat("dd MMMM yyyy", Locale("ru"))

        fun formatDate(date: String): String {
            return try {
                val parsedDate = dateFormatter.parse(date)
                fullDateFormatter.format(parsedDate ?: return date)
            } catch (e: Exception) {
                date
            }
        }

        fun formatTime(time: String): String {
            return try {
                val parsedTime = timeFormatter.parse(time)
                timeFormatter.format(parsedTime ?: return time)
            } catch (e: Exception) {
                time
            }
        }

        fun isValidDate(date: String): Boolean {
            return try {
                dateFormatter.parse(date) != null
            } catch (e: Exception) {
                false
            }
        }

        fun isValidTime(time: String): Boolean {
            return try {
                timeFormatter.parse(time) != null
            } catch (e: Exception) {
                false
            }
        }

        fun getCurrentSemester(): Int {
            val calendar = Calendar.getInstance()
            return when (calendar.get(Calendar.MONTH)) {
                in 8..11 -> 1  // Сентябрь - Декабрь: 1 семестр
                in 0..5 -> 2   // Январь - Июнь: 2 семестр
                else -> 3      // Летние месяцы: 3 семестр (если есть)
            }
        }
    }

    // Методы экземпляра
    fun getFormattedDate(): String = formatDate(date)

    fun getFormattedStartTime(): String = formatTime(startTime)

    fun getFormattedEndTime(): String = formatTime(endTime)

    fun getFullFormattedTime(): String = "${getFormattedStartTime()} - ${getFormattedEndTime()}"

    fun isUpcoming(): Boolean {
        try {
            val examDate = dateFormatter.parse(date) ?: return false
            val now = Calendar.getInstance().time
            return examDate.after(now)
        } catch (e: Exception) {
            return false
        }
    }
}