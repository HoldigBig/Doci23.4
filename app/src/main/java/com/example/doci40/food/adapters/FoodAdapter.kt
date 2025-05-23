package com.example.doci40.food.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.doci40.R
import com.example.doci40.food.models.FoodItem

class FoodAdapter(
    private val onAddToCart: (FoodItem) -> Unit,
    private val onItemClick: (FoodItem) -> Unit
) : RecyclerView.Adapter<FoodAdapter.FoodViewHolder>() {

    private var items = listOf<FoodItem>()

    fun updateItems(newItems: List<FoodItem>) {
        items = newItems
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FoodViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_food, parent, false)
        return FoodViewHolder(view)
    }

    override fun onBindViewHolder(holder: FoodViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount() = items.size

    inner class FoodViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val foodImage: ImageView = itemView.findViewById(R.id.foodImage)
        private val foodName: TextView = itemView.findViewById(R.id.foodName)
        private val foodDescription: TextView = itemView.findViewById(R.id.foodDescription)
        private val foodPrice: TextView = itemView.findViewById(R.id.foodPrice)
        private val addToCartButton: ImageButton = itemView.findViewById(R.id.addToCartButton)
        private val timeText: TextView = itemView.findViewById(R.id.timeText)

        fun bind(item: FoodItem) {
            foodName.text = item.name
            foodDescription.text = item.description
            foodPrice.text = "${item.price} ₽"
            timeText.text = "${item.preparationTime} мин"

            Glide.with(itemView.context)
                .load(item.img)
                .placeholder(R.drawable.ic_home)
                .error(R.drawable.ic_error)
                .into(foodImage)

            addToCartButton.setOnClickListener {
                onAddToCart(item)
            }

            itemView.setOnClickListener {
                onItemClick(item)
            }
        }
    }
} 