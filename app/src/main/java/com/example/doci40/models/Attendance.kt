package com.example.doci40.models

import java.util.Date

data class Attendance(
    val id: String = "",
    val userId: String = "",
    val date: Date = Date(),
    val isPresent: Boolean = false,
    val isHoliday: Boolean = false
) 