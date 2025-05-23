package com.example.doci40.exams.dialogs

import android.app.Dialog
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import com.example.doci40.exams.models.ExamModel
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class ExamFilterDialog : DialogFragment() {

    private var onFilterSelected: ((String?) -> Unit)? = null

    fun setOnFilterSelectedListener(listener: (String?) -> Unit) {
        onFilterSelected = listener
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val subjects = ExamModel.SUBJECTS.toMutableList()
        subjects.add(0, "Все предметы")

        return MaterialAlertDialogBuilder(requireContext())
            .setTitle("Фильтр по предмету")
            .setItems(subjects.toTypedArray()) { _, which ->
                val selectedSubject = if (which == 0) null else subjects[which]
                onFilterSelected?.invoke(selectedSubject)
            }
            .setNegativeButton("Отмена", null)
            .create()
    }

    companion object {
        const val TAG = "ExamFilterDialog"
    }
}