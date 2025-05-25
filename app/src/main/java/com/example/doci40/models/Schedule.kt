package com.example.doci40.models

data class Schedule(
    val id: String = "",
    val userId: String = "",
    val groupId: String = "",
    val subject: String = "",
    val teacher: String = "",
    val room: String = "",
    val dayOfWeek: Int = 1, // 1 = Понедельник, 7 = Воскресенье
    val startTime: String = "", // Формат "HH:mm"
    val endTime: String = "", // Формат "HH:mm"
    val type: String = "", // Лекция, Практика, Семинар и т.д.
    val weekType: String = "", // Четная, Нечетная, Каждая
    val group: String = ""
) {
    // Пустой конструктор для Firestore
    constructor() : this("", "", "", "", "", "", 1, "", "", "", "", "")
} 