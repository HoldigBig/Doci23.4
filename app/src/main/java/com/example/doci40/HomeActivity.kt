package com.example.doci40

import android.content.Intent
import android.graphics.BitmapFactory
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.card.MaterialCardView
import com.google.android.material.imageview.ShapeableImageView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage

class HomeActivity : AppCompatActivity() {
    private val TAG = "HomeActivity"
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private lateinit var storage: FirebaseStorage
    private var userData: Map<String, Any>? = null

    // Views
    private lateinit var userName: TextView
    private lateinit var studentName: TextView
    private lateinit var studentEmail: TextView
    private lateinit var studentClass: TextView
    private lateinit var profileImage: ShapeableImageView
    private lateinit var studentImage: ShapeableImageView

    // Buttons
    private lateinit var notificationButton: ImageButton
    private lateinit var btnExams: MaterialCardView
    private lateinit var btnHomework: MaterialCardView
    private lateinit var btnTeacher: MaterialCardView
    private lateinit var btnHeadman: MaterialCardView
    private lateinit var btnResults: MaterialCardView
    private lateinit var btnFood: MaterialCardView
    private lateinit var btnNews: MaterialCardView
    private lateinit var bottomNavView: BottomNavigationView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        setContentView(R.layout.activity_home)

        // Устанавливаем цвет иконок статус бара на черный
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
            window.statusBarColor = Color.TRANSPARENT // Опционально, если нужен прозрачный статус бар
        }

        // Возвращаем обработчик отступов
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Инициализация Firebase
        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()
        storage = FirebaseStorage.getInstance()

        // Проверяем авторизацию
        if (auth.currentUser == null) {
            startActivity(Intent(this, LogInActivity::class.java))
            finish()
            return
        }

        // Инициализация UI компонентов
        initViews()

        // Настройка обработчиков нажатий
        setupClickListeners()

        // Настройка нижнего меню
        setupBottomNavigation()

        // Загрузка данных пользователя
        loadUserData()
    }

    private fun initViews() {
        Log.d(TAG, "initViews: Инициализация views")

        userName = findViewById(R.id.userName)
        studentName = findViewById(R.id.studentName)
        studentEmail = findViewById(R.id.studentEmail)
        studentClass = findViewById(R.id.studentClass)
        profileImage = findViewById(R.id.profileImage)
        studentImage = findViewById(R.id.studentImage)

        // Инициализация кнопок
        notificationButton = findViewById(R.id.notificationButton)
        btnExams = findViewById(R.id.btn_exams)
        btnHomework = findViewById(R.id.btn_homework)
        btnTeacher = findViewById(R.id.btn_teacher)
        btnHeadman = findViewById(R.id.btn_headman)
        btnResults = findViewById(R.id.btn_results)
        btnFood = findViewById(R.id.btn_food)
        btnNews = findViewById(R.id.btn_news)
        bottomNavView = findViewById(R.id.bottom_navigation)
    }

    private fun setupClickListeners() {
        Log.d(TAG, "setupClickListeners: Настройка обработчиков нажатий")

        notificationButton.setOnClickListener {
            startActivity(Intent(this, NotificationsActivity::class.java))
        }

        btnExams.setOnClickListener {
            try {
                startActivity(Intent(this, ExamsActivity::class.java))
            } catch (e: Exception) {
                handleError("Ошибка при открытии экзаменов: ${e.message}")
            }
        }

        btnHomework.setOnClickListener {
            startActivity(Intent(this, HomeworkActivity::class.java))
        }

        btnTeacher.setOnClickListener {
            startActivity(Intent(this, HomeActivity::class.java))
        }

        btnHeadman.setOnClickListener {
            startActivity(Intent(this, HomeActivity::class.java))
        }

        btnResults.setOnClickListener {
            startActivity(Intent(this, ExamsResultActivity::class.java))
        }

        btnFood.setOnClickListener {
            startActivity(Intent(this, FoodActivity::class.java))
        }

        btnNews.setOnClickListener {
            startActivity(Intent(this, NewsActivity::class.java))
        }
    }

    private fun setupBottomNavigation() {
        Log.d(TAG, "setupBottomNavigation: Настройка нижнего меню")

        bottomNavView.setOnItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.nav_home -> {
                    // Мы уже на главном экране
                    true
                }
                R.id.nav_calendar -> {
                    startActivity(Intent(this, CalendarActivity::class.java))
                    true
                }
                R.id.nav_cart -> {
                    startActivity(Intent(this, CartActivity::class.java))
                    true
                }
                R.id.nav_chat -> {
                    startActivity(Intent(this, HomeActivity::class.java))
                    true
                }
                R.id.nav_profile -> {
                    startActivity(Intent(this, HomeActivity::class.java))
                    true
                }
                else -> false
            }
        }
    }

    private fun loadUserData() {
        val currentUser = auth.currentUser
        if (currentUser == null) {
            handleError("Пользователь не авторизован")
            return
        }

        Log.d(TAG, "Начало загрузки данных пользователя: ${currentUser.uid}")

        // Сначала попробуем найти пользователя по email
        db.collection("users")
            .whereEqualTo("email", currentUser.email)
            .get()
            .addOnSuccessListener { documents ->
                if (!documents.isEmpty) {
                    val document = documents.documents[0]
                    Log.d(TAG, "Пользователь найден по email: ${document.id}")
                    Log.d(TAG, "Данные пользователя: ${document.data}")

                    userData = document.data
                    if (userData != null) {
                        // Обновляем UI с данными пользователя
                        val fio = userData!!["FIO"]?.toString() ?: ""
                        userName.text = fio
                        studentName.text = fio
                        studentEmail.text = userData!!["email"]?.toString() ?: ""
                        studentClass.text = "Группа ${userData!!["group"]?.toString() ?: ""}, Курс ${userData!!["kurs"]?.toString() ?: ""}"

                        // Обновляем displayName в Firebase Authentication
                        if (fio.isNotEmpty() && currentUser.displayName != fio) {
                            val profileUpdates = UserProfileChangeRequest.Builder()
                                .setDisplayName(fio)
                                .build()

                            currentUser.updateProfile(profileUpdates)
                                .addOnCompleteListener { task ->
                                    if (task.isSuccessful) {
                                        Log.d(TAG, "displayName пользователя успешно обновлен")
                                    } else {
                                        Log.e(TAG, "Ошибка обновления displayName пользователя", task.exception)
                                    }
                                }
                        }

                        // Получаем роль пользователя и настраиваем видимость кнопок
                        val userRole = userData!!["userType"]?.toString() ?: "student"
                        setupButtonsVisibility(userRole)

                        // Загружаем фото профиля
                        loadProfileImage(currentUser)
                    }
                } else {
                    handleError("Пользователь не найден в базе данных")
                }
            }
            .addOnFailureListener { e ->
                handleError("Ошибка при загрузке данных пользователя: ${e.message}")
            }
    }

    private fun setupButtonsVisibility(role: String) {
        when (role.toLowerCase()) {
            "admin", "administration" -> {
                btnTeacher.visibility = View.VISIBLE
                btnHeadman.visibility = View.GONE
            }
            "teacher" -> {
                btnTeacher.visibility = View.VISIBLE
                btnHeadman.visibility = View.GONE
            }
            "headman" -> {
                btnTeacher.visibility = View.GONE
                btnHeadman.visibility = View.VISIBLE
            }
            else -> { // student
                btnTeacher.visibility = View.GONE
                btnHeadman.visibility = View.GONE
            }
        }
    }

    private fun loadProfileImage(currentUser: FirebaseUser) {
        val photoUrl = userData?.get("photoUrl")?.toString()
        if (photoUrl.isNullOrEmpty()) {
            Log.d(TAG, "URL фото профиля отсутствует")
            return
        }

        Log.d(TAG, "Начало загрузки фото профиля: $photoUrl")

        try {
            storage.getReferenceFromUrl(photoUrl)
                .getBytes(Long.MAX_VALUE)
                .addOnSuccessListener { bytes ->
                    Log.d(TAG, "Фото профиля успешно загружено")
                    val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                    profileImage.setImageBitmap(bitmap)
                    studentImage.setImageBitmap(bitmap)
                }
                .addOnFailureListener { e ->
                    handleError("Ошибка при загрузке фото: ${e.message}")
                    Log.e(TAG, "Ошибка при загрузке фото профиля", e)
                }
        } catch (e: IllegalArgumentException) {
            Log.e(TAG, "Некорректный URL фото профиля: $photoUrl", e)
            handleError("Некорректный URL фото профиля")
        }
    }

    private fun handleError(message: String) {
        Log.e(TAG, message)
        runOnUiThread {
            Toast.makeText(this, message, Toast.LENGTH_LONG).show()
        }
    }
}