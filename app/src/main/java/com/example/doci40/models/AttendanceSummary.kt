package com.example.doci40.models

data class AttendanceSummary(
    val month: String = "",
    val presentDays: Int = 0,
    val absentDays: Int = 0,
    val totalDays: Int = 0
) {
    // Calculate percentage for display
    val presentPercentage: Int
        get() = if (totalDays == 0) 0 else (presentDays * 100) / totalDays
    val absentPercentage: Int
        get() = if (totalDays == 0) 0 else (absentDays * 100) / totalDays
} 