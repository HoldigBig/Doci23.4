package com.example.doci40.models

import java.util.Date

data class EventModel(
    val id: String = "",
    val title: String = "",
    val description: String = "",
    val date: Date = Date(),
    val startTime: Date = Date(),
    val endTime: Date = Date(),
    val location: String = "",
    val type: String = "",
    val userId: String = ""
) {
    // Пустой конструктор для Firestore
    constructor() : this(
        id = "",
        title = "",
        description = "",
        date = Date(),
        startTime = Date(),
        endTime = Date(),
        location = "",
        type = "",
        userId = ""
    )
} 