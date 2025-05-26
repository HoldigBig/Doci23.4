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
    var likesCount: String = "0",
    var commentsCount: String = "0",
    var isLiked: Boolean = false
) {
    fun getLikesCountInt(): Int = likesCount.toIntOrNull() ?: 0

    fun getCommentsCountInt(): Int = commentsCount.toIntOrNull() ?: 0

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as NewsItem

        if (id != other.id) return false
        if (name != other.name) return false
        if (desc != other.desc) return false
        if (img != other.img) return false
        if (fullImageUrl != other.fullImageUrl) return false
        if (schoolName != other.schoolName) return false
        if (schoolLogo != other.schoolLogo) return false
        if (timestamp != other.timestamp) return false
        if (likesCount != other.likesCount) return false
        if (commentsCount != other.commentsCount) return false
        if (isLiked != other.isLiked) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + name.hashCode()
        result = 31 * result + desc.hashCode()
        result = 31 * result + img.hashCode()
        result = 31 * result + fullImageUrl.hashCode()
        result = 31 * result + schoolName.hashCode()
        result = 31 * result + schoolLogo.hashCode()
        result = 31 * result + timestamp.hashCode()
        result = 31 * result + likesCount.hashCode()
        result = 31 * result + commentsCount.hashCode()
        result = 31 * result + isLiked.hashCode()
        return result
    }
}