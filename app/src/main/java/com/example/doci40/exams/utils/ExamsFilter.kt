package com.example.doci40.exams.utils

import com.example.doci40.exams.models.ExamModel

object ExamsFilter {
    fun filterBySubject(exams: List<ExamModel>, subject: String?): List<ExamModel> {
        return if (subject.isNullOrEmpty()) {
            exams
        } else {
            exams.filter { it.subject == subject }
        }
    }

    fun sortByDate(exams: List<ExamModel>, ascending: Boolean = true): List<ExamModel> {
        return if (ascending) {
            exams.sortedBy { it.date }
        } else {
            exams.sortedByDescending { it.date }
        }
    }

    fun sortBySubject(exams: List<ExamModel>, ascending: Boolean = true): List<ExamModel> {
        return if (ascending) {
            exams.sortedBy { it.subject }
        } else {
            exams.sortedByDescending { it.subject }
        }
    }
} 