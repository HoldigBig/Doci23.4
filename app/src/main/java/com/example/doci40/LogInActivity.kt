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
import android.graphics.Color
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
        setContentView(R.layout.activity_log_in)

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
                performLogin(savedEmail, savedPassword, savedUserType)
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
        Log.d(TAG, "performLogin: Начало входа с email: $email, userType: $userType")

        auth.signInWithEmailAndPassword(email, password)
            .addOnSuccessListener { result ->
                val user = result.user
                if (user != null) {
                    Log.d(TAG, "performLogin: Пользователь успешно аутентифицирован, uid: ${user.uid}")

                    // Проверяем тип пользователя в Firestore
                    db.collection("users")
                        .whereEqualTo("email", email)
                        .get()
                        .addOnSuccessListener { documents ->
                            if (!documents.isEmpty) {
                                val document = documents.documents[0]
                                val userTypeInDb = document.getString("userType")
                                Log.d(TAG, "performLogin: Тип пользователя в БД: $userTypeInDb")

                                if (userTypeInDb == userType) {
                                    Log.d(TAG, "performLogin: Тип пользователя соответствует ожидаемому")
                                    startActivity(Intent(this, HomeActivity::class.java))
                                    finish()
                                } else {
                                    Log.e(TAG, "performLogin: Тип пользователя не соответствует ожидаемому")
                                    Toast.makeText(this, "Ошибка: вы выбрали неверный тип пользователя", Toast.LENGTH_LONG).show()
                                    auth.signOut()
                                }
                            } else {
                                // Если не нашли по email, проверяем по uid
                                db.collection("users")
                                    .document(user.uid)
                                    .get()
                                    .addOnSuccessListener { document ->
                                        if (document != null && document.exists()) {
                                            val userTypeInDb = document.getString("userType")
                                            Log.d(TAG, "performLogin: Тип пользователя в БД (по uid): $userTypeInDb")

                                            if (userTypeInDb == userType) {
                                                Log.d(TAG, "performLogin: Тип пользователя соответствует ожидаемому")
                                                startActivity(Intent(this, HomeActivity::class.java))
                                                finish()
                                            } else {
                                                Log.e(TAG, "performLogin: Тип пользователя не соответствует ожидаемому")
                                                Toast.makeText(this, "Ошибка: вы выбрали неверный тип пользователя", Toast.LENGTH_LONG).show()
                                                auth.signOut()
                                            }
                                        } else {
                                            Log.e(TAG, "performLogin: Пользователь не найден в базе данных")
                                            Toast.makeText(this, "Ошибка: пользователь не найден в базе данных. Пожалуйста, зарегистрируйтесь", Toast.LENGTH_LONG).show()
                                            auth.signOut()
                                        }
                                    }
                                    .addOnFailureListener { e ->
                                        Log.e(TAG, "performLogin: Ошибка получения данных пользователя по uid", e)
                                        Toast.makeText(this, "Ошибка: ${e.message}", Toast.LENGTH_SHORT).show()
                                    }
                            }
                        }
                        .addOnFailureListener { e ->
                            Log.e(TAG, "performLogin: Ошибка получения данных пользователя", e)
                            Toast.makeText(this, "Ошибка: ${e.message}", Toast.LENGTH_SHORT).show()
                        }
                } else {
                    Log.e(TAG, "performLogin: Пользователь не найден после успешной аутентификации")
                    Toast.makeText(this, "Ошибка: пользователь не найден", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "performLogin: Ошибка аутентификации", e)
                Toast.makeText(this, "Ошибка: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }
}