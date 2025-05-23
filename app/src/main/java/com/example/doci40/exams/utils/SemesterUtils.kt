package com.example.doci40.exams.utils
import java.util.*

object SemesterUtils {
    fun getCurrentSemester(): Int {
        val calendar = Calendar.getInstance()
        return when (calendar.get(Calendar.MONTH)) {
            in 8..11 -> 1  // Сентябрь - Декабрь: 1 семестр
            in 0..5 -> 2   // Январь - Июнь: 2 семестр
            else -> 3      // Летние месяцы: 3 семестр (если есть)
        }
    }

    fun getSemesterName(semester: Int): String {
        return when (semester) {
            1 -> "1 семестр"
            2 -> "2 семестр"
            3 -> "3 семестр"
            else -> "Неизвестный семестр"
        }
    }

    fun getSemesterStartDate(semester: Int): Calendar {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        
        return Calendar.getInstance().apply {
            when (semester) {
                1 -> {
                    set(year, Calendar.SEPTEMBER, 1)
                }
                2 -> {
                    set(year, Calendar.FEBRUARY, 1)
                }
                3 -> {
                    set(year, Calendar.JUNE, 1)
                }
                else -> {
                    set(year, Calendar.JANUARY, 1)
                }
            }
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
    }

    fun getSemesterEndDate(semester: Int): Calendar {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        
        return Calendar.getInstance().apply {
            when (semester) {
                1 -> {
                    set(year, Calendar.DECEMBER, 31)
                }
                2 -> {
                    set(year, Calendar.MAY, 31)
                }
                3 -> {
                    set(year, Calendar.AUGUST, 31)
                }
                else -> {
                    set(year, Calendar.DECEMBER, 31)
                }
            }
            set(Calendar.HOUR_OF_DAY, 23)
            set(Calendar.MINUTE, 59)
            set(Calendar.SECOND, 59)
            set(Calendar.MILLISECOND, 999)
        }
    }

    fun isDateInSemester(date: Calendar, semester: Int): Boolean {
        val start = getSemesterStartDate(semester)
        val end = getSemesterEndDate(semester)
        return !date.before(start) && !date.after(end)
    }
} 