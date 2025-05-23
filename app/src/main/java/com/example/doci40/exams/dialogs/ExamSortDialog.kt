package com.example.doci40.exams.dialogs

import android.app.Dialog
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class ExamSortDialog : DialogFragment() {

    private var onSortSelected: ((SortOption) -> Unit)? = null

    fun setOnSortSelectedListener(listener: (SortOption) -> Unit) {
        onSortSelected = listener
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val options = SortOption.values()
        val optionNames = options.map { it.displayName }.toTypedArray()

        return MaterialAlertDialogBuilder(requireContext())
            .setTitle("Сортировка")
            .setItems(optionNames) { _, which ->
                onSortSelected?.invoke(options[which])
            }
            .setNegativeButton("Отмена", null)
            .create()
    }

    enum class SortOption(val displayName: String) {
        DATE_ASC("По дате (сначала ближайшие)"),
        DATE_DESC("По дате (сначала дальние)"),
        SUBJECT_ASC("По предмету (А-Я)"),
        SUBJECT_DESC("По предмету (Я-А)")
    }

    companion object {
        const val TAG = "ExamSortDialog"
    }
}