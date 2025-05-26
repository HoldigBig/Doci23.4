package com.example.doci40.exams.models

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.Date

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
    @JvmField
    val dayOfWeek: String = try {
        val parsedDate = dateFormatter.parse(date)
        val calendar = Calendar.getInstance().apply {
            time = parsedDate ?: return@apply
        }
        val days = arrayOf("???", "Вс", "Пн", "Вт", "Ср", "Чт", "Пт", "Сб")
        days[calendar.get(Calendar.DAY_OF_WEEK)]
    } catch (e: Exception) {
        "???"
    }

    @JvmField
    val dayNumber: String = try {
        val parsedDate = dateFormatter.parse(date)
        val calendar = Calendar.getInstance().apply {
            time = parsedDate ?: return@apply
        }
        calendar.get(Calendar.DAY_OF_MONTH).toString()
    } catch (e: Exception) {
        "??"
    }

    @JvmField
    val time: String = "$startTime - $endTime"

    @JvmField
    val formattedDate: String = try {
        val parsedDate = SimpleDateFormat("dd.MM.yyyy", Locale("ru")).parse(date)
        SimpleDateFormat("d MMMM yyyy", Locale("ru")).format(parsedDate ?: Date())
    } catch (e: Exception) {
        date
    }

    @JvmField
    val formattedStartTime: String = try {
        val parsedTime = timeFormatter.parse(startTime)
        if (parsedTime == null) {
            startTime
        } else {
            timeFormatter.format(parsedTime)
        }
    } catch (e: Exception) {
        startTime
    }

    @JvmField
    val formattedEndTime: String = try {
        val parsedTime = timeFormatter.parse(endTime)
        if (parsedTime == null) {
            endTime
        } else {
            timeFormatter.format(parsedTime)
        }
    } catch (e: Exception) {
        endTime
    }

    @JvmField
    val fullFormattedTime: String = "$startTime - $endTime"

    @JvmField
    val upcoming: Boolean = try {
        val examDate = dateFormatter.parse(date)
        val now = Date()
        examDate?.after(now) ?: false
    } catch (e: Exception) {
        false
    }

    @JvmField
    val valid: Boolean = subject.isNotBlank() && date.isNotBlank() && 
            startTime.isNotBlank() && endTime.isNotBlank()

    @JvmField
    val active: Boolean = isActive

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
        private val fullDateFormatter = SimpleDateFormat("d MMMM yyyy", Locale("ru"))

        fun formatDate(date: String): String {
            return try {
                val parsedDate = dateFormatter.parse(date)
                if (parsedDate == null) {
                    date
                } else {
                    fullDateFormatter.format(parsedDate)
                }
            } catch (e: Exception) {
                date
            }
        }

        fun formatTime(time: String): String {
            return try {
                val parsedTime = timeFormatter.parse(time)
                if (parsedTime == null) {
                    time
                } else {
                    timeFormatter.format(parsedTime)
                }
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
}