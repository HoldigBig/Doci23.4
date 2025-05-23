package com.example.doci40

import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.doci40.food.adapters.FoodAdapter
import com.example.doci40.food.models.CartItem
import com.example.doci40.food.models.FoodCategories
import com.example.doci40.food.models.FoodItem
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import java.text.NumberFormat
import java.util.Locale
import org.json.JSONArray
import org.json.JSONObject

class FoodActivity : AppCompatActivity() {

    companion object {
        private const val CART_ACTIVITY_REQUEST_CODE = 1001
    }

    private lateinit var db: FirebaseFirestore
    private lateinit var foodAdapter: FoodAdapter
    private lateinit var searchField: EditText
    private lateinit var filterButton: ImageButton
    private lateinit var backButton: ImageButton
    private lateinit var cartButton: ImageButton
    private lateinit var categoriesGroup: ChipGroup
    private lateinit var foodGrid: RecyclerView
    private lateinit var basketPanel: View
    private lateinit var basketItemCount: TextView
    private lateinit var basketTotalPrice: TextView

    private var allFoodItems = listOf<FoodItem>()
    private var cartItems = mutableListOf<CartItem>()
    private var currentCategory = FoodCategories.ALL
    private var searchQuery = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        setContentView(R.layout.activity_food)

        // Устанавливаем цвет иконок статус бара на черный
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
            window.statusBarColor = Color.TRANSPARENT // Опционально, если нужен прозрачный статус бар
        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        db = FirebaseFirestore.getInstance()
        initViews()
        setupRecyclerView()
        setupListeners()
        loadCartFromPrefs()
        loadFoodItems()
    }

    private fun initViews() {
        searchField = findViewById(R.id.searchField)
        filterButton = findViewById(R.id.filterButton)
        backButton = findViewById(R.id.backButton)
        cartButton = findViewById(R.id.cartButton)
        categoriesGroup = findViewById(R.id.categoriesGroup)
        foodGrid = findViewById(R.id.foodGrid)
        basketPanel = findViewById(R.id.basketPanel)
        basketItemCount = findViewById(R.id.basketItemCount)
        basketTotalPrice = findViewById(R.id.basketTotalPrice)

        // Настройка категорий
        FoodCategories.categories.forEach { category ->
            val chip = Chip(this).apply {
                text = category
                isCheckable = true
                isChecked = category == FoodCategories.ALL
                chipBackgroundColor = getColorStateList(R.color.chip_background)
                setTextColor(getColorStateList(R.color.chip_text_color))
                chipStrokeWidth = 1f
                chipStrokeColor = getColorStateList(R.color.chip_text_color)
                rippleColor = getColorStateList(R.color.chip_text_color)
            }
            categoriesGroup.addView(chip)
        }
    }

    private fun setupRecyclerView() {
        foodAdapter = FoodAdapter(
            onAddToCart = { foodItem -> addToCart(foodItem) },
            onItemClick = { foodItem -> showFoodDetails(foodItem) }
        )

        foodGrid.apply {
            layoutManager = GridLayoutManager(this@FoodActivity, 1)
            adapter = foodAdapter
        }
    }

    private fun setupListeners() {
        backButton.setOnClickListener { onBackPressed() }
        
        cartButton.setOnClickListener { 
            val intent = Intent(this, CartActivity::class.java)
            startActivityForResult(intent, CART_ACTIVITY_REQUEST_CODE)
        }
        
        basketPanel.setOnClickListener { 
            val intent = Intent(this, CartActivity::class.java)
            startActivityForResult(intent, CART_ACTIVITY_REQUEST_CODE)
        }

        filterButton.setOnClickListener { showFilterDialog() }

        searchField.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                searchQuery = s?.toString() ?: ""
                filterAndUpdateList()
            }
        })

        categoriesGroup.setOnCheckedChangeListener { group, checkedId ->
            val chip = group.findViewById<Chip>(checkedId)
            currentCategory = chip?.text?.toString() ?: FoodCategories.ALL
            filterAndUpdateList()
        }
    }

    private fun loadFoodItems() {
        Log.d("FoodActivity", "Начало загрузки данных из Firestore")
        db.collection("food")
            .get()
            .addOnSuccessListener { documents ->
                Log.d("FoodActivity", "Получено ${documents.size()} документов")
                val storage = FirebaseStorage.getInstance()
                
                documents.forEach { doc ->
                    try {
                        Log.d("FoodActivity", "Обработка документа ${doc.id}: ${doc.data}")
                        
                        // Получаем download URL для изображения
                        val imgPath = doc.getString("img") ?: run {
                            Log.w("FoodActivity", "Документ ${doc.id}: отсутствует поле img")
                            return@forEach
                        }
                        
                        storage.getReferenceFromUrl(imgPath)
                            .downloadUrl
                            .addOnSuccessListener { downloadUrl ->
                                val foodItem = FoodItem(
                                    id = doc.id,
                                    name = doc.getString("name") ?: "Без названия",
                                    description = doc.getString("description") ?: "",
                                    price = doc.getDouble("price") ?: 0.0,
                                    img = downloadUrl.toString(),
                                    category = FoodCategories.mapCategory(doc.getString("category") ?: ""),
                                    isPopular = when (val isPopular = doc.getString("isPopular")) {
                                        "true" -> true
                                        "false" -> false
                                        else -> false
                                    },
                                    preparationTime = doc.getLong("preparationTime")?.toInt() ?: 0
                                )
                                
                                Log.d("FoodActivity", "Успешно создан FoodItem: $foodItem")
                                allFoodItems = allFoodItems + foodItem
                                filterAndUpdateList()
                            }
                            .addOnFailureListener { e ->
                                Log.e("FoodActivity", "Ошибка получения download URL для ${doc.id}", e)
                                // Создаем FoodItem даже если не удалось загрузить изображение
                                val foodItem = FoodItem(
                                    id = doc.id,
                                    name = doc.getString("name") ?: "Без названия",
                                    description = doc.getString("description") ?: "",
                                    price = doc.getDouble("price") ?: 0.0,
                                    img = "", // Пустой URL для изображения
                                    category = FoodCategories.mapCategory(doc.getString("category") ?: ""),
                                    isPopular = when (val isPopular = doc.getString("isPopular")) {
                                        "true" -> true
                                        "false" -> false
                                        else -> false
                                    },
                                    preparationTime = doc.getLong("preparationTime")?.toInt() ?: 0
                                )
                                allFoodItems = allFoodItems + foodItem
                                filterAndUpdateList()
                            }
                    } catch (e: Exception) {
                        Log.e("FoodActivity", "Ошибка при обработке документа ${doc.id}", e)
                    }
                }
            }
            .addOnFailureListener { e ->
                Log.e("FoodActivity", "Ошибка загрузки данных из Firestore", e)
                Toast.makeText(this, "Ошибка загрузки меню: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun filterAndUpdateList() {
        val filteredItems = allFoodItems.filter { item ->
            val matchesCategory = currentCategory == FoodCategories.ALL || item.category == currentCategory
            val matchesSearch = searchQuery.isEmpty() || 
                item.name.contains(searchQuery, ignoreCase = true) ||
                item.description.contains(searchQuery, ignoreCase = true)
            matchesCategory && matchesSearch
        }
        foodAdapter.updateItems(filteredItems)
    }

    private fun loadCartFromPrefs() {
        val prefs = getSharedPreferences("food_prefs", MODE_PRIVATE)
        val cartJson = prefs.getString("cart_items", null)
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
                updateBasketUI()
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
            prefs.edit().putString("cart_items", jsonArray.toString()).apply()
        } catch (e: Exception) {
            Toast.makeText(this, "Ошибка сохранения корзины", Toast.LENGTH_SHORT).show()
        }
    }

    private fun addToCart(foodItem: FoodItem) {
        val existingItem = cartItems.find { it.foodItem.id == foodItem.id }
        if (existingItem != null) {
            existingItem.quantity++
        } else {
            cartItems.add(CartItem(foodItem))
        }
        updateBasketUI()
        saveCartToPrefs()
        Toast.makeText(this, "Добавлено в корзину", Toast.LENGTH_SHORT).show()
    }

    private fun updateBasketUI() {
        val totalItems = cartItems.sumOf { it.quantity }
        val totalPrice = cartItems.sumOf { it.totalPrice }
        
        basketItemCount.text = if (totalItems > 0) "$totalItems" else ""
        basketTotalPrice.text = formatPrice(totalPrice)
        
        basketPanel.visibility = if (totalItems > 0) View.VISIBLE else View.GONE
    }

    private fun showCart() {
        if (cartItems.isEmpty()) {
            Toast.makeText(this, "Корзина пуста", Toast.LENGTH_SHORT).show()
            return
        }

        val itemsText = cartItems.joinToString("\n") { item ->
            "${item.foodItem.name} x${item.quantity} - ${formatPrice(item.totalPrice)}"
        }
        val totalText = "Итого: ${formatPrice(cartItems.sumOf { it.totalPrice })}"

        MaterialAlertDialogBuilder(this)
            .setTitle("Корзина")
            .setMessage("$itemsText\n\n$totalText")
            .setPositiveButton("Оформить заказ") { _, _ ->
                // TODO: Реализовать оформление заказа
                Toast.makeText(this, "Функция оформления заказа в разработке", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("Очистить") { _, _ ->
                cartItems.clear()
                updateBasketUI()
            }
            .setNeutralButton("Закрыть", null)
            .show()
    }

    private fun showFoodDetails(foodItem: FoodItem) {
        MaterialAlertDialogBuilder(this)
            .setTitle(foodItem.name)
            .setMessage("""
                ${foodItem.description}
                
                Время приготовления: ${foodItem.preparationTime} мин
                Цена: ${formatPrice(foodItem.price)}
            """.trimIndent())
            .setPositiveButton("Добавить в корзину") { _, _ ->
                addToCart(foodItem)
            }
            .setNegativeButton("Закрыть", null)
            .show()
    }

    private fun showFilterDialog() {
        val options = arrayOf("По популярности", "По цене (возр.)", "По цене (убыв.)")
        val icons = arrayOf(
            R.drawable.ic_popular,
            R.drawable.ic_price_up,
            R.drawable.ic_price_down
        )
        
        val dialog = MaterialAlertDialogBuilder(this)
            .setTitle("Сортировка")
            .setItems(options) { _, which ->
                val sortedItems = when (which) {
                    0 -> allFoodItems.sortedByDescending { it.isPopular }
                    1 -> allFoodItems.sortedBy { it.price }
                    2 -> allFoodItems.sortedByDescending { it.price }
                    else -> allFoodItems
                }
                allFoodItems = sortedItems
                filterAndUpdateList()
            }
            .create()

        dialog.setOnShowListener {
            val listView = dialog.listView
            listView?.apply {
                for (i in 0 until childCount) {
                    val item = getChildAt(i)
                    val textView = item.findViewById<TextView>(android.R.id.text1)
                    textView?.apply {
                        setTextColor(getColor(R.color.black))
                        textSize = 16f
                        setPadding(48, 32, 48, 32)
                        
                        // Добавляем иконку
                        val drawable = getDrawable(icons[i])
                        drawable?.setTint(getColor(R.color.background))
                        setCompoundDrawablesWithIntrinsicBounds(drawable, null, null, null)
                        compoundDrawablePadding = 24
                    }
                }
            }
        }

        dialog.show()
    }

    private fun formatPrice(price: Double): String {
        return NumberFormat.getCurrencyInstance(Locale("ru", "RU"))
            .format(price)
            .replace("₽", "₽")
            .trim()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == CART_ACTIVITY_REQUEST_CODE && resultCode == RESULT_OK) {
            // Обновляем UI корзины после возврата из CartActivity
            loadCartFromPrefs()
        }
    }
}