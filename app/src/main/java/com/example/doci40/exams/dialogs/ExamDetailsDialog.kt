package com.example.doci40.exams.dialogs

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import com.example.doci40.R
import com.example.doci40.exams.models.ExamModel
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class ExamDetailsDialog(private val exam: ExamModel) : DialogFragment() {

    private var onExamUpdated: (() -> Unit)? = null
    private var onExamDeleted: (() -> Unit)? = null

    fun setOnExamUpdatedListener(listener: () -> Unit) {
        onExamUpdated = listener
    }

    fun setOnExamDeletedListener(listener: () -> Unit) {
        onExamDeleted = listener
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val view = LayoutInflater.from(requireContext())
            .inflate(R.layout.dialog_exam_details, null)

        // Заполняем данные
        view.findViewById<TextView>(R.id.examDate).text = ExamModel.formatDate(exam.date)
        view.findViewById<TextView>(R.id.examTime).text = exam.time
        view.findViewById<TextView>(R.id.examLocation).text = exam.location
        view.findViewById<TextView>(R.id.examiner).text = exam.examiner
        view.findViewById<TextView>(R.id.examType).text = exam.type

        return MaterialAlertDialogBuilder(requireContext())
            .setTitle(exam.subject)
            .setView(view)
            .setPositiveButton("Редактировать") { _, _ ->
                // Открываем экран редактирования
                // TODO: Реализовать редактирование
            }
            .setNeutralButton("Удалить") { _, _ ->
                deleteExam()
            }
            .setNegativeButton("Закрыть", null)
            .create()
    }

    private fun deleteExam() {
        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser == null) {
            Toast.makeText(context, "Ошибка авторизации", Toast.LENGTH_SHORT).show()
            return
        }

        FirebaseFirestore.getInstance()
            .collection("users")
            .document(currentUser.uid)
            .collection("exams")
            .document(exam.examId)
            .delete()
            .addOnSuccessListener {
                Toast.makeText(context, "Экзамен удален", Toast.LENGTH_SHORT).show()
                onExamDeleted?.invoke()
            }
            .addOnFailureListener { e ->
                Toast.makeText(context, "Ошибка: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
            }
    }

    companion object {
        const val TAG = "ExamDetailsDialog"
    }
} 