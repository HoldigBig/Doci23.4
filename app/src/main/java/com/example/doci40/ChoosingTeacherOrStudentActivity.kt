package com.example.doci40

import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.button.MaterialButton

class ChoosingTeacherOrStudentActivity : AppCompatActivity() {
    private val TAG = "ChoosingActivity"

    companion object {
        const val EXTRA_USER_TYPE = "user_type"
        const val USER_TYPE_TEACHER = "teacher"
        const val USER_TYPE_STUDENT = "student"
        const val USER_TYPE_ADMIN = "admin"
        const val USER_TYPE_HEADMAN = "headman"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        setContentView(R.layout.activity_choosing_teacher_or_student)

        // Устанавливаем цвет статус бара на @color/background и иконки на черный
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            window.statusBarColor = resources.getColor(R.color.background, this.theme) // Устанавливаем цвет из ресурсов
            window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR // Устанавливаем темные иконки
        }

        // Обработчик отступов
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        Log.d(TAG, "onCreate: Инициализация активити выбора роли")

        // Находим кнопки
        val btnTeacher = findViewById<MaterialButton>(R.id.btnTeacher)
        val btnStudent = findViewById<MaterialButton>(R.id.btnStudent)

        // Обработка нажатия на кнопку Teacher (Наставник)
        btnTeacher.setOnClickListener {
            Log.d(TAG, "onCreate: Выбрана роль наставника")
            // Показываем диалог выбора между преподавателем и администратором
            showTeacherTypeDialog()
        }

        // Обработка нажатия на кнопку Student (Студент)
        btnStudent.setOnClickListener {
            Log.d(TAG, "onCreate: Выбрана роль студента")
            // Показываем диалог выбора между студентом и старостой
            showStudentTypeDialog()
        }
    }

    private fun showTeacherTypeDialog() {
        val items = arrayOf("Преподаватель", "Администратор")
        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Выберите тип наставника")
            .setItems(items) { _, which ->
                when (which) {
                    0 -> navigateToLogin(USER_TYPE_TEACHER)
                    1 -> navigateToLogin(USER_TYPE_ADMIN)
                }
            }
            .show()
    }

    private fun showStudentTypeDialog() {
        val items = arrayOf("Студент", "Староста")
        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Выберите тип студента")
            .setItems(items) { _, which ->
                when (which) {
                    0 -> navigateToLogin(USER_TYPE_STUDENT)
                    1 -> navigateToLogin(USER_TYPE_HEADMAN)
                }
            }
            .show()
    }

    private fun navigateToLogin(userType: String) {
        Log.d(TAG, "navigateToLogin: Переход к экрану входа с типом пользователя: $userType")
        val intent = Intent(this, LogInActivity::class.java).apply {
            putExtra(EXTRA_USER_TYPE, userType)
        }
        startActivity(intent)
        finish() // Закрываем текущую активность, чтобы нельзя было вернуться назад
    }
}