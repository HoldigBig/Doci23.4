package com.example.doci40.food.models

object FoodCategories {
    val ALL = "Все"
    val HOT = "Горячие блюда"
    val COLD = "Холодные блюда"
    val DRINKS = "Напитки"
    val DESSERTS = "Десерты"

    val categories = listOf(ALL, HOT, COLD, DRINKS, DESSERTS)

    // Маппинг категорий из базы данных
    fun mapCategory(category: String): String {
        return when (category.lowercase()) {
            "hot" -> HOT
            "cold" -> COLD
            "drinks" -> DRINKS
            "desserts" -> DESSERTS
            else -> category
        }
    }
} 