package com.example.doci40.exams.dialogs

import android.app.DatePickerDialog
import android.app.Dialog
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import com.example.doci40.exams.models.ExamModel
import java.util.Calendar

class ExamDateDialog(private val exam: ExamModel) : DialogFragment() {

    private var onDateSelected: ((String) -> Unit)? = null

    fun setOnDateSelectedListener(listener: (String) -> Unit) {
        onDateSelected = listener
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val calendar = Calendar.getInstance()
        
        // Парсим текущую дату экзамена
        try {
            val parts = exam.date.split(".")
            calendar.set(parts[2].toInt(), parts[1].toInt() - 1, parts[0].toInt())
        } catch (e: Exception) {
            // Если не удалось распарсить дату, используем текущую
        }

        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        return DatePickerDialog(
            requireContext(),
            { _, selectedYear, selectedMonth, selectedDay ->
                val formattedDate = String.format(
                    "%02d.%02d.%04d",
                    selectedDay,
                    selectedMonth + 1,
                    selectedYear
                )
                onDateSelected?.invoke(formattedDate)
            },
            year,
            month,
            day
        )
    }

    companion object {
        const val TAG = "ExamDateDialog"
    }
} 