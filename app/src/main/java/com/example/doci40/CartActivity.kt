package com.example.doci40

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.doci40.food.adapters.CartAdapter
import com.example.doci40.food.models.CartItem
import com.example.doci40.food.models.FoodItem
import com.example.doci40.food.models.UserBalance
import com.google.android.material.button.MaterialButton
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import org.json.JSONArray
import org.json.JSONObject
import java.text.NumberFormat
import java.util.Locale

class CartActivity : AppCompatActivity() {

    private lateinit var backButton: ImageButton
    private lateinit var cartList: RecyclerView
    private lateinit var totalPriceText: TextView
    private lateinit var checkoutButton: MaterialButton
    private lateinit var clearButton: MaterialButton
    private lateinit var emptyCartText: TextView
    private lateinit var cartAdapter: CartAdapter
    private lateinit var balanceText: TextView
    private lateinit var balanceStatusText: TextView

    private var cartItems = mutableListOf<CartItem>()
    private val CART_PREFS_KEY = "cart_items"
    private val BALANCE_PREFS_KEY = "user_balance"
    private var userBalance = UserBalance()
    private lateinit var db: FirebaseFirestore
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_cart)
        
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        db = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()
        
        initViews()
        loadUserBalance()
        loadCartFromPrefs()
        setupRecyclerView()
        setupListeners()
    }

    private fun initViews() {
        backButton = findViewById(R.id.backButton)
        cartList = findViewById(R.id.cartList)
        totalPriceText = findViewById(R.id.totalPriceText)
        checkoutButton = findViewById(R.id.checkoutButton)
        clearButton = findViewById(R.id.clearButton)
        emptyCartText = findViewById(R.id.emptyCartText)
        balanceText = findViewById(R.id.balanceText)
        balanceStatusText = findViewById(R.id.balanceStatusText)
    }

    private fun loadUserBalance() {
        val userId = auth.currentUser?.uid ?: return
        db.collection("users").document(userId)
            .get()
            .addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    userBalance.balance = document.getDouble("balance") ?: 0.0
                    userBalance.lastUpdated = document.getLong("lastUpdated") ?: System.currentTimeMillis()
                    updateBalanceUI()
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Ошибка загрузки баланса: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun updateBalanceUI() {
        val totalPrice = cartItems.sumOf { it.totalPrice }
        val remainingBalance = userBalance.balance - totalPrice
        
        balanceText.text = formatPrice(userBalance.balance)
        
        when {
            cartItems.isEmpty() -> {
                balanceStatusText.text = "Добавьте товары в корзину"
                balanceStatusText.setTextColor(getColor(R.color.gray_dark))
            }
            remainingBalance >= 0 -> {
                balanceStatusText.text = "Достаточно средств"
                balanceStatusText.setTextColor(getColor(R.color.background))
            }
            else -> {
                balanceStatusText.text = "Не хватает: ${formatPrice(-remainingBalance)}"
                balanceStatusText.setTextColor(getColor(R.color.error))
            }
        }
    }

    private fun setupRecyclerView() {
        cartList.layoutManager = GridLayoutManager(this, 1)
        cartAdapter = CartAdapter(
            cartItems = cartItems,
            onQuantityChange = { item, newQuantity -> 
                updateCartItemQuantity(item, newQuantity)
                cartAdapter.updateItems(cartItems)
                updateBalanceUI()
            },
            onRemoveItem = { item -> 
                removeFromCart(item)
                cartAdapter.updateItems(cartItems)
                updateBalanceUI()
            }
        )
        cartList.adapter = cartAdapter
    }

    private fun setupListeners() {
        backButton.setOnClickListener { onBackPressed() }

        checkoutButton.setOnClickListener {
            if (cartItems.isNotEmpty()) {
                val totalPrice = cartItems.sumOf { it.totalPrice }
                if (userBalance.balance >= totalPrice) {
                    // TODO: Реализовать оформление заказа
                    Toast.makeText(this, "Функция оформления заказа в разработке", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "Недостаточно средств", Toast.LENGTH_SHORT).show()
                }
            }
        }

        clearButton.setOnClickListener {
            if (cartItems.isNotEmpty()) {
                MaterialAlertDialogBuilder(this)
                    .setTitle("Очистить корзину")
                    .setMessage("Вы уверены, что хотите удалить все товары из корзины?")
                    .setPositiveButton("Да") { _, _ ->
                        clearCart()
                        updateBalanceUI()
                    }
                    .setNegativeButton("Нет", null)
                    .show()
            }
        }
    }

    private fun loadCartFromPrefs() {
        val prefs = getSharedPreferences("food_prefs", MODE_PRIVATE)
        val cartJson = prefs.getString(CART_PREFS_KEY, null)
        if (cartJson != null) {
            try {
                val jsonArray = JSONArray(cartJson)
                cartItems.clear()
                for (i in 0 until jsonArray.length()) {
                    val itemObj = jsonArray.getJSONObject(i)
                    val foodItemObj = itemObj.getJSONObject("foodItem")
                    
                    val foodItem = FoodItem(
                        id = foodItemObj.getString("id"),
                        name = foodItemObj.getString("name"),
                        description = foodItemObj.getString("description"),
                        price = foodItemObj.getDouble("price"),
                        img = foodItemObj.getString("img"),
                        category = foodItemObj.getString("category"),
                        isPopular = foodItemObj.getBoolean("isPopular"),
                        preparationTime = foodItemObj.getInt("preparationTime")
                    )
                    
                    val quantity = itemObj.getInt("quantity")
                    cartItems.add(CartItem(foodItem, quantity))
                }
                updateUI()
            } catch (e: Exception) {
                Toast.makeText(this, "Ошибка загрузки корзины", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun saveCartToPrefs() {
        try {
            val jsonArray = JSONArray()
            cartItems.forEach { cartItem ->
                val itemObj = JSONObject().apply {
                    put("quantity", cartItem.quantity)
                    put("foodItem", JSONObject().apply {
                        put("id", cartItem.foodItem.id)
                        put("name", cartItem.foodItem.name)
                        put("description", cartItem.foodItem.description)
                        put("price", cartItem.foodItem.price)
                        put("img", cartItem.foodItem.img)
                        put("category", cartItem.foodItem.category)
                        put("isPopular", cartItem.foodItem.isPopular)
                        put("preparationTime", cartItem.foodItem.preparationTime)
                    })
                }
                jsonArray.put(itemObj)
            }
            
            val prefs = getSharedPreferences("food_prefs", MODE_PRIVATE)
            prefs.edit().putString(CART_PREFS_KEY, jsonArray.toString()).apply()
        } catch (e: Exception) {
            Toast.makeText(this, "Ошибка сохранения корзины", Toast.LENGTH_SHORT).show()
        }
    }

    private fun updateCartItemQuantity(cartItem: CartItem, newQuantity: Int) {
        val index = cartItems.indexOf(cartItem)
        if (index != -1) {
            if (newQuantity > 0) {
                cartItems[index].quantity = newQuantity
            } else {
                cartItems.removeAt(index)
            }
            updateUI()
            saveCartToPrefs()
        }
    }

    private fun removeFromCart(cartItem: CartItem) {
        cartItems.remove(cartItem)
        updateUI()
        saveCartToPrefs()
    }

    private fun clearCart() {
        cartItems.clear()
        updateUI()
        saveCartToPrefs()
    }

    private fun updateUI() {
        val totalItems = cartItems.sumOf { it.quantity }
        val totalPrice = cartItems.sumOf { it.totalPrice }

        if (totalItems > 0) {
            cartList.visibility = View.VISIBLE
            emptyCartText.visibility = View.GONE
            checkoutButton.isEnabled = true
            clearButton.isEnabled = true
            totalPriceText.text = formatPrice(totalPrice)
        } else {
            cartList.visibility = View.GONE
            emptyCartText.visibility = View.VISIBLE
            checkoutButton.isEnabled = false
            clearButton.isEnabled = false
            totalPriceText.text = formatPrice(0.0)
        }
        
        updateBalanceUI()
    }

    private fun formatPrice(price: Double): String {
        return NumberFormat.getCurrencyInstance(Locale("ru", "RU"))
            .format(price)
            .replace("₽", "₽")
            .trim()
    }

    override fun onBackPressed() {
        // Отправляем результат обратно в FoodActivity
        val resultIntent = Intent().apply {
            putExtra("cart_updated", true)
        }
        setResult(RESULT_OK, resultIntent)
        super.onBackPressed()
    }
}