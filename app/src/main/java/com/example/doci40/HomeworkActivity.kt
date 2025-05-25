package com.example.doci40

import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.doci40.databinding.ActivityHomeworkBinding
import com.example.doci40.homework.AddHomeworkActivity
import com.example.doci40.homework.adapters.DayAdapter
import com.example.doci40.homework.adapters.HomeworkAdapter
import com.example.doci40.homework.models.DayItem
import com.example.doci40.homework.models.HomeworkModel
import com.google.android.material.chip.Chip
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class HomeworkActivity : AppCompatActivity(), DayAdapter.DayClickListener {
    private lateinit var binding: ActivityHomeworkBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private lateinit var homeworkAdapter: HomeworkAdapter
    private lateinit var dayAdapter: DayAdapter
    private var allHomeworkList: List<HomeworkModel> = listOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomeworkBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
            window.statusBarColor = Color.TRANSPARENT
        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(android.R.id.content)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        setupViews()
        setupClickListeners()
        setupDaysRecyclerView()
        loadHomework()
    }

    private fun setupViews() {
        homeworkAdapter = HomeworkAdapter()
        binding.homeworkRecyclerView.adapter = homeworkAdapter

        binding.subjectsChipGroup.setOnCheckedChangeListener { group, checkedId ->
            val chip = group.findViewById<Chip>(checkedId)
            val selectedSubject = chip?.text?.toString() ?: getString(R.string.all)
            filterHomeworkBySubject(selectedSubject)
        }
    }

    private fun setupClickListeners() {
        binding.backButton.setOnClickListener { finish() }
        
        binding.addHomeworkButton.setOnClickListener {
            startActivity(Intent(this, AddHomeworkActivity::class.java))
        }
    }

    private fun setupDaysRecyclerView() {
        val daysList = generateDays(4)
        dayAdapter = DayAdapter(daysList)
        dayAdapter.setOnDayClickListener(this)
        binding.daysRecyclerView.adapter = dayAdapter

        val currentDayPosition = daysList.indexOfFirst { isCurrentDay(it.date) }
        if (currentDayPosition != -1) {
            binding.daysRecyclerView.scrollToPosition(currentDayPosition)
            dayAdapter.setSelectedDay(daysList[currentDayPosition])
        }
    }

    private fun generateDays(weeksAhead: Int): List<DayItem> {
        val days = mutableListOf<DayItem>()
        val calendar = Calendar.getInstance()
        val dayFormat = SimpleDateFormat("d", Locale("ru"))
        
        while (calendar.get(Calendar.DAY_OF_WEEK) != Calendar.MONDAY) {
            calendar.add(Calendar.DAY_OF_YEAR, -1)
        }

        for (week in 0 until weeksAhead) {
            for (i in 0 until 7) {
                val dayOfWeek = when (calendar.get(Calendar.DAY_OF_WEEK)) {
                    Calendar.MONDAY -> "Пн"
                    Calendar.TUESDAY -> "Вт"
                    Calendar.WEDNESDAY -> "Ср"
                    Calendar.THURSDAY -> "Чт"
                    Calendar.FRIDAY -> "Пт"
                    Calendar.SATURDAY -> "Сб"
                    Calendar.SUNDAY -> "Вс"
                    else -> ""
                }
                val dayNumber = dayFormat.format(calendar.time)
                val dateMillis = calendar.timeInMillis
                days.add(DayItem(dayOfWeek, dayNumber, dateMillis))
                calendar.add(Calendar.DAY_OF_YEAR, 1)
            }
        }

        return days
    }

    private fun isCurrentDay(dateMillis: Long): Boolean {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = dateMillis
        val today = Calendar.getInstance()

        return calendar.get(Calendar.YEAR) == today.get(Calendar.YEAR) &&
                calendar.get(Calendar.DAY_OF_YEAR) == today.get(Calendar.DAY_OF_YEAR)
    }

    private fun loadHomework() {
        val currentUser = auth.currentUser
        currentUser?.let { user ->
            db.collection("users").document(user.uid).get()
                .addOnSuccessListener { userDoc ->
                    if (userDoc.exists()) {
                        val groupId = userDoc.getString("group")
                        if (groupId != null) {
                            db.collection("homework")
                                .whereEqualTo("groupId", groupId)
                                .addSnapshotListener { snapshot, e ->
                                    if (e != null) {
                                        showError("Ошибка при загрузке заданий")
                                        return@addSnapshotListener
                                    }

                                    val homeworkList = mutableListOf<HomeworkModel>()
                                    snapshot?.documents?.forEach { doc ->
                                        val homework = HomeworkModel(
                                            id = doc.id,
                                            subject = doc.getString("subject") ?: "",
                                            title = doc.getString("title") ?: "",
                                            description = doc.getString("description") ?: "",
                                            dueDate = doc.getString("dueDate") ?: "",
                                            teacher = doc.getString("teacher") ?: "",
                                            groupId = doc.getString("groupId") ?: "",
                                            assignmentDate = doc.getString("assignmentDate") ?: ""
                                        )
                                        homeworkList.add(homework)
                                    }
                                    allHomeworkList = homeworkList
                                    updateUI(homeworkList)
                                    updateSubjectFilter(homeworkList)
                                }
                        } else {
                            showError("Группа не найдена")
                        }
                    } else {
                        showError("Пользователь не найден")
                    }
                }
                .addOnFailureListener { e ->
                    showError("Ошибка: ${e.message}")
                }
        }
    }

    private fun updateUI(homeworkList: List<HomeworkModel>) {
        if (homeworkList.isEmpty()) {
            binding.emptyState.visibility = View.VISIBLE
            binding.homeworkRecyclerView.visibility = View.GONE
        } else {
            binding.emptyState.visibility = View.GONE
            binding.homeworkRecyclerView.visibility = View.VISIBLE
            homeworkAdapter.setData(homeworkList)
        }
    }

    private fun updateSubjectFilter(homeworkList: List<HomeworkModel>) {
        val subjects = homeworkList.map { it.subject }.distinct()
        binding.subjectsChipGroup.removeAllViews()

        // Добавляем чип "Все"
        val chipAll = Chip(this).apply {
            id = View.generateViewId()
            text = getString(R.string.all)
            isCheckable = true
            isChecked = true
        }
        binding.subjectsChipGroup.addView(chipAll)

        // Добавляем чипы для каждого предмета
        subjects.forEach { subject ->
            val chip = Chip(this).apply {
                id = View.generateViewId()
                text = subject
                isCheckable = true
            }
            binding.subjectsChipGroup.addView(chip)
        }
    }

    private fun filterHomeworkBySubject(subject: String) {
        val filteredList = if (subject == getString(R.string.all)) {
            allHomeworkList
        } else {
            allHomeworkList.filter { it.subject == subject }
        }
        updateUI(filteredList)
    }

    override fun onDayClick(dayItem: DayItem) {
        val selectedSubject = binding.subjectsChipGroup.findViewById<Chip>(
            binding.subjectsChipGroup.checkedChipId
        )?.text?.toString() ?: getString(R.string.all)

        val filteredList = filterHomeworkByDate(dayItem.date, selectedSubject)
        updateUI(filteredList)
    }

    private fun filterHomeworkByDate(dateMillis: Long, subject: String): List<HomeworkModel> {
        val calendar = Calendar.getInstance().apply { timeInMillis = dateMillis }
        val dateFormat = SimpleDateFormat("dd.MM.yyyy", Locale("ru"))
        val selectedDate = dateFormat.format(calendar.time)

        return allHomeworkList.filter { homework ->
            val matchesSubject = subject == getString(R.string.all) || homework.subject == subject
            val matchesDate = homework.dueDate == selectedDate
            matchesSubject && matchesDate
        }
    }

    private fun showError(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
} 