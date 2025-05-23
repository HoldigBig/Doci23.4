package com.example.doci40.food.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.doci40.R
import com.example.doci40.food.models.CartItem
import com.google.android.material.imageview.ShapeableImageView
import java.text.NumberFormat
import java.util.Locale

class CartAdapter(
    private var cartItems: List<CartItem>,
    private val onQuantityChange: (CartItem, Int) -> Unit,
    private val onRemoveItem: (CartItem) -> Unit
) : RecyclerView.Adapter<CartAdapter.CartViewHolder>() {

    fun updateItems(newItems: List<CartItem>) {
        cartItems = newItems.toList()
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CartViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_cart, parent, false)
        return CartViewHolder(view)
    }

    override fun onBindViewHolder(holder: CartViewHolder, position: Int) {
        val item = cartItems[position]
        holder.bind(item)
    }

    override fun getItemCount() = cartItems.size

    inner class CartViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val foodImage: ShapeableImageView = itemView.findViewById(R.id.foodImage)
        private val foodName: TextView = itemView.findViewById(R.id.foodName)
        private val foodPrice: TextView = itemView.findViewById(R.id.foodPrice)
        private val quantityText: TextView = itemView.findViewById(R.id.quantityText)
        private val decreaseButton: ImageButton = itemView.findViewById(R.id.decreaseButton)
        private val increaseButton: ImageButton = itemView.findViewById(R.id.increaseButton)
        private val removeButton: ImageButton = itemView.findViewById(R.id.removeButton)

        fun bind(item: CartItem) {
            foodName.text = item.foodItem.name
            foodPrice.text = formatPrice(item.totalPrice)
            quantityText.text = item.quantity.toString()

            Glide.with(itemView.context)
                .load(if (item.foodItem.img.isNotEmpty()) item.foodItem.img else R.drawable.placeholder_food)
                .placeholder(R.drawable.placeholder_food)
                .error(R.drawable.placeholder_food)
                .centerCrop()
                .into(foodImage)

            decreaseButton.setOnClickListener(null)
            increaseButton.setOnClickListener(null)
            removeButton.setOnClickListener(null)

            decreaseButton.setOnClickListener {
                if (item.quantity > 1) {
                    onQuantityChange(item, item.quantity - 1)
                } else {
                    onRemoveItem(item)
                }
            }

            increaseButton.setOnClickListener {
                onQuantityChange(item, item.quantity + 1)
            }

            removeButton.setOnClickListener {
                onRemoveItem(item)
            }
        }

        private fun formatPrice(price: Double): String {
            return NumberFormat.getCurrencyInstance(Locale("ru", "RU"))
                .format(price)
                .replace("₽", "₽")
                .trim()
        }
    }
} 