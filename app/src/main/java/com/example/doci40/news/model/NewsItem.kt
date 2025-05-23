package com.example.doci40.news.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentReference

data class NewsItem(
    var id: String = "",
    val name: String = "",
    val desc: String = "",
    val img: String = "",
    val fullImageUrl: String = "",
    val schoolName: String = "",
    val schoolLogo: String = "",
    val timestamp: Timestamp = Timestamp.now(),
    val likesCount: String = "0",
    val commentsCount: String = "0",
    var isLiked: Boolean = false
) {
    fun getLikesCountInt(): Int = likesCount.toIntOrNull() ?: 0

    fun getCommentsCountInt(): Int = commentsCount.toIntOrNull() ?: 0
}