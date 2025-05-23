package com.example.doci40.news.model

import com.google.firebase.Timestamp

data class Comment(
    var id: String = "",
    val newsId: String = "",
    val userId: String = "",
    val userName: String = "",
    val userImageUrl: String = "",
    val text: String = "",
    val timestamp: Timestamp = Timestamp.now()
) 