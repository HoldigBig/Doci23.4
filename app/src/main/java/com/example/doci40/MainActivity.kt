package com.example.doci40

import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.animation.AnimationUtils
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import com.google.firebase.auth.FirebaseAuth

class MainActivity : AppCompatActivity() {
    private val TAG = "MainActivity"
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        setContentView(R.layout.activity_main)

        // Устанавливаем цвет иконок статус бара на черный
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
            window.statusBarColor = Color.TRANSPARENT // Опционально, если нужен прозрачный статус бар
        }

        Log.d(TAG, "onCreate: MainActivity запущен")

        // Инициализация Firebase Auth
        auth = FirebaseAuth.getInstance()

        // Анимация логотипа
        val logoImage = findViewById<ImageView>(R.id.logoImage)
        val titleText = findViewById<TextView>(R.id.titleText)
        val subtitleText = findViewById<TextView>(R.id.subtitleText)

        // Анимация масштабирования для логотипа
        val scaleAnimation = AnimationUtils.loadAnimation(this, R.anim.scale)
        logoImage.startAnimation(scaleAnimation)

        // Анимация появления для текста
        val fadeIn = AnimationUtils.loadAnimation(this, R.anim.fade_in)
        fadeIn.startOffset = 500 // Задержка для текста
        titleText.startAnimation(fadeIn)

        val fadeInSubtitle = AnimationUtils.loadAnimation(this, R.anim.fade_in)
        fadeInSubtitle.startOffset = 1000 // Большая задержка для подзаголовка
        subtitleText.startAnimation(fadeInSubtitle)

        // Задержка перед переходом на следующий экран
        android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
            checkUserAndNavigate()
        }, 3000) // Увеличиваем задержку до 3 секунд
    }

    private fun checkUserAndNavigate() {
        startActivity(Intent(this, ChoosingTeacherOrStudentActivity::class.java))
        finish()
    }
}