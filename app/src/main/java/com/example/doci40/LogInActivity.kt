package com.example.doci40

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.button.MaterialButton
import android.widget.ImageButton
import android.widget.CheckBox
import android.content.Context
import android.os.Build
import android.view.View
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowCompat

class LogInActivity : AppCompatActivity() {
    private val TAG = "LogInActivity"
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    // Тип пользователя, переданный из ChoosingTeacherOrStudentActivity
    private var userType: String? = null

    private lateinit var emailInput: TextInputEditText
    private lateinit var passwordInput: TextInputEditText
    private lateinit var loginButton: MaterialButton
    private lateinit var backButton: ImageButton
    private lateinit var rememberMeCheckBox: CheckBox

    companion object {
        private const val PREF_NAME = "login_preferences"
        private const val KEY_REMEMBER_ME = "remember_me"
        private const val KEY_EMAIL = "email"
        private const val KEY_PASSWORD = "password"
        private const val KEY_USER_TYPE = "user_type"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        WindowCompat.setDecorFitsSystemWindows(window, false)
        setContentView(R.layout.activity_login)

        // Устанавливаем цвет статус бара на @color/background и иконки на черный
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            window.statusBarColor = resources.getColor(R.color.background, this.theme) // Устанавливаем цвет из ресурсов
            window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR // Устанавливаем темные иконки
        }

        // Обновляем существующий обработчик отступов
        // Предполагаем, что R.id.main - это правильный ID корневого элемента
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            // Устанавливаем отступы. Возможно, потребуется убрать systemBars.top
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        Log.d(TAG, "onCreate: LogInActivity запущен")

        // Получаем тип пользователя из intent
        userType = intent.getStringExtra(ChoosingTeacherOrStudentActivity.EXTRA_USER_TYPE)
        Log.d(TAG, "onCreate: Тип пользователя: $userType")

        // Инициализация Firebase
        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        // Инициализация views
        initViews()

        // Настройка обработчиков нажатий
        setupClickListeners()
        checkSavedCredentials()
    }

    private fun initViews() {
        Log.d(TAG, "initViews: Инициализация views")

        emailInput = findViewById(R.id.etEmail)
        passwordInput = findViewById(R.id.etPassword)
        loginButton = findViewById(R.id.btnLogin)
        backButton = findViewById(R.id.btnBack)
        rememberMeCheckBox = findViewById(R.id.cbRememberMe)
    }

    private fun checkSavedCredentials() {
        val prefs = getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        val rememberMe = prefs.getBoolean(KEY_REMEMBER_ME, false)

        if (rememberMe) {
            val savedEmail = prefs.getString(KEY_EMAIL, "")
            val savedPassword = prefs.getString(KEY_PASSWORD, "")
            val savedUserType = prefs.getString(KEY_USER_TYPE, "")

            if (!savedEmail.isNullOrEmpty() && !savedPassword.isNullOrEmpty() && !savedUserType.isNullOrEmpty()) {
                emailInput.setText(savedEmail)
                passwordInput.setText(savedPassword)
                rememberMeCheckBox.isChecked = true
                
                // Показываем индикатор загрузки
                val loadingView = findViewById<View>(R.id.loadingView)
                loadingView?.visibility = View.VISIBLE
                
                auth.signInWithEmailAndPassword(savedEmail, savedPassword)
                    .addOnSuccessListener { result ->
                        val user = result.user
                        if (user != null) {
                            // Проверяем тип пользователя в Firestore только по uid
                            db.collection("users")
                                .document(user.uid)
                                .get()
                                .addOnSuccessListener { document ->
                                    loadingView?.visibility = View.GONE
                                    if (document != null && document.exists()) {
                                        val userTypeInDb = document.getString("userType")
                                        if (userTypeInDb == savedUserType) {
                                            startActivity(Intent(this, HomeActivity::class.java))
                                            finish()
                                        } else {
                                            showError("Ошибка: неверный тип пользователя")
                                            auth.signOut()
                                            clearSavedCredentials()
                                        }
                                    } else {
                                        showError("Ошибка: пользователь не найден")
                                        auth.signOut()
                                        clearSavedCredentials()
                                    }
                                }
                                .addOnFailureListener { e ->
                                    loadingView?.visibility = View.GONE
                                    showError("Ошибка: ${e.message}")
                                    clearSavedCredentials()
                                }
                        }
                    }
                    .addOnFailureListener { e ->
                        loadingView?.visibility = View.GONE
                        showError("Ошибка входа: ${e.message}")
                        clearSavedCredentials()
                    }
            }
        }
    }

    private fun saveCredentials(email: String, password: String) {
        if (rememberMeCheckBox.isChecked) {
            val prefs = getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            prefs.edit().apply {
                putBoolean(KEY_REMEMBER_ME, true)
                putString(KEY_EMAIL, email)
                putString(KEY_PASSWORD, password)
                putString(KEY_USER_TYPE, userType)
                apply()
            }
        } else {
            clearSavedCredentials()
        }
    }

    private fun clearSavedCredentials() {
        val prefs = getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        prefs.edit().clear().apply()
    }

    private fun setupClickListeners() {
        Log.d(TAG, "setupClickListeners: Настройка обработчиков нажатий")

        loginButton.setOnClickListener {
            val email = emailInput.text.toString().trim()
            val password = passwordInput.text.toString().trim()

            if (email.isEmpty() || password.isEmpty()) {
                Log.w(TAG, "setupClickListeners: Пустые поля ввода")
                Toast.makeText(this, "Пожалуйста, заполните все поля", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            saveCredentials(email, password)
            performLogin(email, password, userType!!)
        }

        backButton.setOnClickListener {
            Log.d(TAG, "setupClickListeners: Возврат на предыдущий экран")
            finish()
        }
    }

    private fun performLogin(email: String, password: String, userType: String) {
        // Показываем индикатор загрузки
        val loadingView = findViewById<View>(R.id.loadingView)
        loadingView?.visibility = View.VISIBLE

        auth.signInWithEmailAndPassword(email, password)
            .addOnSuccessListener { result ->
                val user = result.user
                if (user != null) {
                    // Проверяем тип пользователя в Firestore только по uid
                    db.collection("users")
                        .document(user.uid)
                        .get()
                        .addOnSuccessListener { document ->
                            loadingView?.visibility = View.GONE
                            if (document != null && document.exists()) {
                                val userTypeInDb = document.getString("userType")
                                if (userTypeInDb == userType) {
                                    saveCredentials(email, password)
                                    startActivity(Intent(this, HomeActivity::class.java))
                                    finish()
                                } else {
                                    showError("Ошибка: вы выбрали неверный тип пользователя")
                                    auth.signOut()
                                }
                            } else {
                                showError("Ошибка: пользователь не найден")
                                auth.signOut()
                            }
                        }
                        .addOnFailureListener { e ->
                            loadingView?.visibility = View.GONE
                            showError("Ошибка: ${e.message}")
                        }
                }
            }
            .addOnFailureListener { e ->
                loadingView?.visibility = View.GONE
                showError("Ошибка входа: ${e.message}")
            }
    }

    private fun showError(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}