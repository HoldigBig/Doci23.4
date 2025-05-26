package com.example.doci40.exams.models

data class SubjectResult(
    val name: String = "",
    val score: Int = 0,
    val grade: String = ""
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as SubjectResult

        if (name != other.name) return false
        if (score != other.score) return false
        if (grade != other.grade) return false

        return true
    }

    override fun hashCode(): Int {
        var result = name.hashCode()
        result = 31 * result + score
        result = 31 * result + grade.hashCode()
        return result
    }
}

data class TermResult(
    val termId: Int = 0,
    val overallGrade: Int = 0,
    val behaviour: Int = 0,
    val attendance: Int = 0,
    val work: Int = 0,
    val subjects: List<SubjectResult> = listOf()
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as TermResult

        if (termId != other.termId) return false
        if (overallGrade != other.overallGrade) return false
        if (behaviour != other.behaviour) return false
        if (attendance != other.attendance) return false
        if (work != other.work) return false
        if (subjects != other.subjects) return false

        return true
    }

    override fun hashCode(): Int {
        var result = termId
        result = 31 * result + overallGrade
        result = 31 * result + behaviour
        result = 31 * result + attendance
        result = 31 * result + work
        result = 31 * result + subjects.hashCode()
        return result
    }
}