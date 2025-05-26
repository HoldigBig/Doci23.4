package com.example.doci40.homework.models

data class HomeworkModel(
    var id: String = "",
    val subject: String = "",
    val title: String = "",
    val teacher: String = "",
    val description: String = "",
    val dueDate: String = "",
    val groupId: String = "",
    val assignmentDate: String = "",
    val editDate: String? = null
) {
    companion object {
        // Список предметов
        val SUBJECTS = listOf(
            "Математика",
            "Физика",
            "Химия",
            "Информатика",
            "История",
            "Литература",
            "Английский язык",
            "Биология",
            "География",
            "Обществознание"
        )
    }
} 