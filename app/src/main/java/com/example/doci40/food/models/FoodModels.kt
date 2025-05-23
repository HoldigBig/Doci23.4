package com.example.doci40.food.models

data class FoodItem(
    val id: String = "",
    val name: String = "",
    val description: String = "",
    val price: Double = 0.0,
    val img: String = "",
    val category: String = "",
    val isPopular: Boolean = false,
    val preparationTime: Int = 0 // в минутах
)

data class Category(
    val id: String = "",
    val name: String = "",
    val iconUrl: String = ""
)

data class CartItem(
    val foodItem: FoodItem,
    var quantity: Int = 1
) {
    val totalPrice: Double
        get() = foodItem.price * quantity
} 