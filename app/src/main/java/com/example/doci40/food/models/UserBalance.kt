package com.example.doci40.food.models

data class UserBalance(
    var balance: Double = 0.0,
    var lastUpdated: Long = System.currentTimeMillis()
) 