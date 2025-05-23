package com.example.doci40.notifivation.model

data class NotificationModel(
    val title: String = "",
    val message: String = "",
    val time: String = "",
    val type: String = "",
    val actionButtonText: String = "",
    val timestamp: Long = 0
)