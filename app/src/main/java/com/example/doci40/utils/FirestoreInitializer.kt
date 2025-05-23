package com.example.doci40.utils

import android.util.Log
import com.example.doci40.exams.models.SubjectResult
import com.example.doci40.exams.models.TermResult
import com.google.firebase.firestore.FirebaseFirestore

object FirestoreInitializer {

    private const val TAG = "FirestoreInitializer"

    fun initializeExamResults(userId: String) {
        Log.d(TAG, "Starting initialization for user: $userId")
        val db = FirebaseFirestore.getInstance()

        // Данные для первого семестра
        val term1Results = TermResult(
            termId = 1,
            overallGrade = 75,
            behaviour = 4,
            attendance = 4,
            work = 4,
            subjects = listOf(
                SubjectResult("Арабский", 90, "A"),
                SubjectResult("Наука", 60, "B"),
                SubjectResult("Английский", 60, "A"),
                SubjectResult("Математика", 55, "B"),
                SubjectResult("История", 42, "C"),
                SubjectResult("Музыка", 30, "D")
            )
        )

        // Данные для второго семестра
        val term2Results = TermResult(
            termId = 2,
            overallGrade = 82,
            behaviour = 5,
            attendance = 4,
            work = 5,
            subjects = listOf(
                SubjectResult("Арабский", 95, "A"),
                SubjectResult("Наука", 75, "B"),
                SubjectResult("Английский", 85, "A"),
                SubjectResult("Математика", 70, "B"),
                SubjectResult("История", 80, "B"),
                SubjectResult("Музыка", 88, "A")
            )
        )

        // Данные для третьего семестра
        val term3Results = TermResult(
            termId = 3,
            overallGrade = 88,
            behaviour = 5,
            attendance = 5,
            work = 5,
            subjects = listOf(
                SubjectResult("Арабский", 98, "A"),
                SubjectResult("Наука", 85, "A"),
                SubjectResult("Английский", 90, "A"),
                SubjectResult("Математика", 82, "A"),
                SubjectResult("История", 88, "A"),
                SubjectResult("Музыка", 85, "A")
            )
        )

        Log.d(TAG, "Created term results objects")

        // Сохраняем данные в Firestore
        val userResultsRef = db.collection("users").document(userId).collection("results")

        userResultsRef.document("term_1").set(term1Results)
            .addOnSuccessListener {
                Log.d(TAG, "Данные 1-го семестра успешно сохранены")
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Ошибка при сохранении данных 1-го семестра: $e")
            }

        userResultsRef.document("term_2").set(term2Results)
            .addOnSuccessListener {
                Log.d(TAG, "Данные 2-го семестра успешно сохранены")
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Ошибка при сохранении данных 2-го семестра: $e")
            }

        userResultsRef.document("term_3").set(term3Results)
            .addOnSuccessListener {
                Log.d(TAG, "Данные 3-го семестра успешно сохранены")
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Ошибка при сохранении данных 3-го семестра: $e")
            }
    }
}