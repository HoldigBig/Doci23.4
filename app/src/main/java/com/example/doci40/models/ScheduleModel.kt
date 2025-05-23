package com.example.doci40.models

data class ScheduleModel(
    val id: String = "",
    val title: String = "",
    val teacher: String = "",
    val room: String = "",
    val dayOfWeek: Int = 0, // 1-7 (понедельник-воскресенье)
    val startTime: String = "",
    val endTime: String = "",
    val type: String = "", // Лекция, Практика, Лабораторная работа и т.д.
    val group: String = "",
    val weekType: String = "", // Четная/Нечетная/Каждая
    val userId: String = ""
) {
    // Пустой конструктор для Firestore
    constructor() : this("", "", "", "", 0, "", "", "", "", "", "")
} 