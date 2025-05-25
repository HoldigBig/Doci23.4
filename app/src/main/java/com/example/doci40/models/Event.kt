package com.example.doci40.models

import java.util.*

data class Event(
    val id: String = "",
    val userId: String = "",
    val title: String = "",
    val description: String = "",
    val date: String = "", // Формат "dd.MM.yyyy"
    val startTime: String = "", // Формат "HH:mm"
    val endTime: String = "", // Формат "HH:mm"
    val location: String = "",
    val type: String = "", // Тип события (Встреча, Дедлайн, Мероприятие и т.д.)
    val priority: String = "" // Высокий, Средний, Низкий
) {
    // Пустой конструктор для Firestore
    constructor() : this("", "", "", "", "", "", "", "", "", "")
} 