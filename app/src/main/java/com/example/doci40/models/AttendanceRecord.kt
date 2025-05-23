package com.example.doci40.models

import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.ServerTimestamp
import java.util.Date

data class AttendanceRecord(
    @DocumentId val id: String = "",
    val userId: String = "",
    @ServerTimestamp val timestamp: Date? = null,
    val status: String = "" // e.g., "present", "absent"
) 