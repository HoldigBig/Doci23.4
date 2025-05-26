package com.example.doci40.notifivation.model

data class NotificationModel(
    val id: String = "",
    val title: String = "",
    val message: String = "",
    val type: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    val isRead: Boolean = false
) {
    // Конструктор для Firestore
    constructor() : this("", "", "", "", System.currentTimeMillis(), false)
}