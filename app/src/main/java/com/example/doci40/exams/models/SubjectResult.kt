package com.example.doci40.exams.models

data class SubjectResult(
    val name: String = "",
    val score: Int = 0,
    val grade: String = ""
)

data class TermResult(
    val termId: Int = 0,
    val overallGrade: Int = 0,
    val behaviour: Int = 0,
    val attendance: Int = 0,
    val work: Int = 0,
    val subjects: List<SubjectResult> = listOf()
)