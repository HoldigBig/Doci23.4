package com.example.doci40

import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageButton
import android.widget.RatingBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.doci40.exams.adapters.SubjectResultAdapter
import com.example.doci40.exams.fragments.SubjectDetailFragment
import com.example.doci40.exams.models.SubjectResult
import com.example.doci40.exams.models.TermResult
import com.example.doci40.utils.FirestoreInitializer
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.progressindicator.CircularProgressIndicator
import com.google.android.material.tabs.TabLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore


class ExamsResultActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private lateinit var subjectAdapter: SubjectResultAdapter
    private lateinit var progressIndicator: CircularProgressIndicator
    private lateinit var overallGradeText: TextView
    private lateinit var behaviourRating: RatingBar
    private lateinit var attendanceRating: RatingBar
    private lateinit var workRating: RatingBar
    private lateinit var tabLayout: TabLayout

    private var currentTermId: Int = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d("ExamsResultActivity", "onCreate started")

        WindowCompat.setDecorFitsSystemWindows(window, false)
        setContentView(R.layout.activity_exams_result)

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

        initializeFirebase()
        initializeViews()
        setupTabLayout()
        setupBackButton()
        setupRecyclerView()
        setupInitDataButton()

        Log.d("ExamsResultActivity", "onCreate finished")

        // Загружаем данные для первого семестра по умолчанию
        loadTermResults(1)
    }

    private fun initializeFirebase() {
        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()
    }

    private fun initializeViews() {
        progressIndicator = findViewById(R.id.progressIndicator)
        overallGradeText = findViewById(R.id.overallGradeText)
        behaviourRating = findViewById(R.id.behaviourRating)
        attendanceRating = findViewById(R.id.attendanceRating)
        workRating = findViewById(R.id.workRating)
        tabLayout = findViewById(R.id.tabLayout)
    }

    private fun setupTabLayout() {
        tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                tab?.let {
                    currentTermId = it.position + 1
                    loadTermResults(currentTermId)
                }
            }
            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })
    }

    private fun setupBackButton() {
        findViewById<ImageButton>(R.id.backButton).setOnClickListener {
            if (supportFragmentManager.backStackEntryCount > 0) {
                supportFragmentManager.popBackStack()
            } else {
                finish()
            }
        }
    }

    private fun setupRecyclerView() {
        subjectAdapter = SubjectResultAdapter { subject ->
            showSubjectDetail(subject)
        }
        findViewById<RecyclerView>(R.id.subjectsRecyclerView).apply {
            layoutManager = LinearLayoutManager(this@ExamsResultActivity)
            adapter = subjectAdapter
        }
    }

    private fun showSubjectDetail(subject: SubjectResult) {
        // Скрываем основной контент и показываем контейнер фрагментов
        findViewById<View>(R.id.mainContent).visibility = View.GONE
        findViewById<View>(R.id.fragmentContainer).visibility = View.VISIBLE
        findViewById<FloatingActionButton>(R.id.initDataButton).visibility = View.GONE

        val fragment = SubjectDetailFragment.newInstance(subject.name, currentTermId)
        val transaction = supportFragmentManager.beginTransaction()
        transaction.replace(R.id.fragmentContainer, fragment)
        transaction.addToBackStack(null)
        transaction.commit()
    }

    private fun setupInitDataButton() {
        Log.d("ExamsResultActivity", "Setting up init data button")
        val initButton = findViewById<FloatingActionButton>(R.id.initDataButton)
        Log.d("ExamsResultActivity", "Found button: ${initButton != null}")

        initButton.setOnClickListener {
            Log.d("ExamsResultActivity", "Init button clicked")
            auth.currentUser?.let { user ->
                Log.d("ExamsResultActivity", "Current user ID: ${user.uid}")
                FirestoreInitializer.initializeExamResults(user.uid)
                Toast.makeText(this, "Инициализация данных начата", Toast.LENGTH_SHORT).show()
            } ?: run {
                Log.e("ExamsResultActivity", "No user logged in")
                Toast.makeText(this, "Ошибка: пользователь не авторизован", Toast.LENGTH_SHORT).show()
            }
        }
        Log.d("ExamsResultActivity", "Init button setup completed")
    }

    private fun loadTermResults(termId: Int) {
        val currentUser = auth.currentUser
        currentUser?.let { user ->
            db.collection("users").document(user.uid)
                .collection("results")
                .document("term_$termId")
                .get()
                .addOnSuccessListener { document ->
                    if (document != null && document.exists()) {
                        val termResult = document.toObject(TermResult::class.java)
                        termResult?.let {
                            updateUI(it)
                        }
                    }
                }
        }
    }

    private fun updateUI(termResult: TermResult) {
        // Обновляем общую оценку
        progressIndicator.progress = termResult.overallGrade
        overallGradeText.text = "${termResult.overallGrade}%"

        // Обновляем рейтинги
        behaviourRating.rating = termResult.behaviour.toFloat()
        attendanceRating.rating = termResult.attendance.toFloat()
        workRating.rating = termResult.work.toFloat()

        // Обновляем список предметов
        subjectAdapter.updateSubjects(termResult.subjects)
    }

    override fun onBackPressed() {
        if (supportFragmentManager.backStackEntryCount > 0) {
            supportFragmentManager.popBackStack()
            // Показываем основной контент и скрываем контейнер фрагментов
            findViewById<View>(R.id.mainContent).visibility = View.VISIBLE
            findViewById<View>(R.id.fragmentContainer).visibility = View.GONE
            findViewById<FloatingActionButton>(R.id.initDataButton).visibility = View.VISIBLE
        } else {
            super.onBackPressed()
        }
    }
}