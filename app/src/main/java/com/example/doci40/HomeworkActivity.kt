package com.example.doci40

import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.doci40.databinding.ActivityHomeworkBinding
import com.example.doci40.homework.adapters.DayAdapter
import com.example.doci40.homework.adapters.HomeworkAdapter
import com.example.doci40.homework.models.DayItem
import com.example.doci40.homework.models.HomeworkModel
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class HomeworkActivity : AppCompatActivity(), DayAdapter.DayClickListener {
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private lateinit var homeworkAdapter: HomeworkAdapter
    private lateinit var backButton: ImageButton
    private lateinit var subjectFilter: ChipGroup
    private lateinit var binding: ActivityHomeworkBinding

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

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        initViews()

        setupClickListeners()

        homeworkAdapter = HomeworkAdapter()
        binding.homeworkRecyclerView.layoutManager = LinearLayoutManager(this)
        binding.homeworkRecyclerView.adapter = homeworkAdapter

        setupDaysRecyclerView()

        loadHomework()
    }

    private fun initViews() {
        subjectFilter = findViewById(R.id.subjectFilter)
    }

    private fun setupClickListeners() {
        binding.toolbar.setNavigationOnClickListener { finish() }

        binding.fabAddHomework.setOnClickListener {
            val intent = Intent(this, AddHomeworkActivity::class.java)
            startActivity(intent)
        }

        subjectFilter.setOnCheckedChangeListener { group, checkedId ->
            val chip = group.findViewById<Chip>(checkedId)
            val selectedSubject = chip?.text?.toString() ?: "Все"
            Log.d("HomeworkActivity", "Subject chip checked: $selectedSubject")
            filterHomeworkBySubject(selectedSubject)
        }
    }

    private fun setupDaysRecyclerView() {
        val daysList = generateDays(4)
        dayAdapter = DayAdapter(daysList)
        dayAdapter.setOnDayClickListener(this)

        binding.recyclerViewDays.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        binding.recyclerViewDays.adapter = dayAdapter

        val currentDayPosition = daysList.indexOfFirst { isCurrentDay(it.date) }
        if (currentDayPosition != RecyclerView.NO_POSITION) {
            binding.recyclerViewDays.scrollToPosition(currentDayPosition)
            dayAdapter.setSelectedDay(daysList[currentDayPosition])
            filterHomeworkByDate(daysList[currentDayPosition].date)
        }
    }

    private fun generateDays(weeksAhead: Int): List<DayItem> {
        val days = mutableListOf<DayItem>()
        val calendar = Calendar.getInstance()

        // Начинаем с понедельника текущей недели
        while (calendar.get(Calendar.DAY_OF_WEEK) != Calendar.MONDAY) {
            calendar.add(Calendar.DAY_OF_YEAR, -1)
        }

        // Добавляем дни недели и кнопку "Все" после каждой недели
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
                val dayNumber = calendar.get(Calendar.DAY_OF_MONTH).toString()
                val dateMillis = calendar.timeInMillis
                days.add(DayItem(dayOfWeek, dayNumber, dateMillis))
                calendar.add(Calendar.DAY_OF_YEAR, 1)
            }
            // Добавляем "Все" после каждой недели
            days.add(DayItem("Все", "", -1))
        }

        return days
    }

    private fun isCurrentDay(dateMillis: Long): Boolean {
        val todayCalendar = Calendar.getInstance()
        todayCalendar.set(Calendar.HOUR_OF_DAY, 0)
        todayCalendar.set(Calendar.MINUTE, 0)
        todayCalendar.set(Calendar.SECOND, 0)
        todayCalendar.set(Calendar.MILLISECOND, 0)

        val itemCalendar = Calendar.getInstance()
        itemCalendar.timeInMillis = dateMillis
        itemCalendar.set(Calendar.HOUR_OF_DAY, 0)
        itemCalendar.set(Calendar.MINUTE, 0)
        itemCalendar.set(Calendar.SECOND, 0)
        itemCalendar.set(Calendar.MILLISECOND, 0)

        return itemCalendar.timeInMillis == todayCalendar.timeInMillis
    }

    private fun loadHomework() {
        val currentUser = auth.currentUser
        currentUser?.let { user ->
            db.collection("users").document(user.uid).get()
                .addOnSuccessListener { userDoc ->
                    Log.d("AddHomework", "Документ пользователя загружен. Существует: ${userDoc.exists()}")
                    if (userDoc.exists()) {
                        val groupId = userDoc.getString("group")
                        Log.d("AddHomework", "Получен groupId: $groupId")
                        if (groupId != null) {
                            db.collection("homework")
                                .whereEqualTo("groupId", groupId)
                                .addSnapshotListener { snapshot, e ->
                                    if (e != null) {
                                        Log.e("AddHomework", "Ошибка получения домашних заданий", e)
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
                                            groupId = doc.getString("groupId") ?: ""
                                        )
                                        homeworkList.add(homework)
                                    }
                                    allHomeworkList = homeworkList // Store the full list
                                    // Do not call updateUI here directly, filtering will happen via day selection
                                    // If no day is selected yet (initial load), filter by current day
                                    val daysList = generateDays(4)
                                    val currentDayPosition = daysList.indexOfFirst { isCurrentDay(it.date) }
                                    if (currentDayPosition != RecyclerView.NO_POSITION) {
                                        filterHomeworkByDate(daysList[currentDayPosition].date)
                                    } else {
                                        updateUI(allHomeworkList)
                                    }

                                    updateSubjectFilter(allHomeworkList)
                                }
                        } else {
                            Log.e("AddHomework", "Поле 'group' отсутствует или пустое в документе пользователя.")
                            Toast.makeText(this, "Не удалось получить ID группы", Toast.LENGTH_SHORT).show()
                        }
                    } else {
                         Log.e("AddHomework", "Документ пользователя не найден.")
                         Toast.makeText(this, "Не удалось получить данные пользователя", Toast.LENGTH_SHORT).show()
                    }
                }
                .addOnFailureListener { e ->
                    Log.e("AddHomework", "Ошибка при загрузке документа пользователя", e)
                    Toast.makeText(this, "Ошибка получения данных пользователя: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        } ?: run {
            Log.e("AddHomework", "Пользователь не аутентифицирован.")
            Toast.makeText(this, "Пользователь не аутентифицирован", Toast.LENGTH_SHORT).show()
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
            // Subject filter update is now in loadHomework after getting all data
        }
    }

    private fun updateSubjectFilter(homeworkList: List<HomeworkModel>) {
        val subjects = homeworkList.map { it.subject }.distinct()
        Log.d("HomeworkActivity", "Updating subject filter with subjects: $subjects")

        val chipAll = subjectFilter.findViewById<Chip>(R.id.chipAll)
        subjectFilter.removeAllViews()
        subjectFilter.addView(chipAll)

        subjects.forEach { subject ->
            val chip = Chip(this, null, R.style.SubjectChipStyle).apply {
                text = subject
                isCheckable = true
            }
            subjectFilter.addView(chip)
        }
    }

    private fun filterHomeworkBySubject(subject: String) {
        Log.d("HomeworkActivity", "Filtering homework by subject: $subject")
        val filteredBySubject = if (subject == "Все") {
            allHomeworkList
        } else {
            allHomeworkList.filter { it.subject == subject }
        }
        Log.d("HomeworkActivity", "Filtered by subject count: ${filteredBySubject.size}")

        val selectedDay = dayAdapter.getSelectedDay()
        if (selectedDay != null) {
            Log.d("HomeworkActivity", "Applying date filter after subject filter for day: ${selectedDay.date}")
            filterHomeworkByDate(selectedDay.date, filteredBySubject)
        } else {
            Log.d("HomeworkActivity", "No day selected, updating UI with subject filtered list.")
            updateUI(filteredBySubject)
        }

    }

    private fun filterHomeworkByDate(dateMillis: Long, homeworkToFilter: List<HomeworkModel> = allHomeworkList) {
        val format = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
        Log.d("HomeworkActivity", "Filtering homework by date: ${if (dateMillis != -1L) format.format(dateMillis) else "Все дни"}")
        if (dateMillis == -1L) {
            updateUI(homeworkToFilter)
            Log.d("HomeworkActivity", "Showing all homework (date filter is Все дни)")
            return
        }

        val selectedCalendar = Calendar.getInstance().apply { timeInMillis = dateMillis }
        selectedCalendar.set(Calendar.HOUR_OF_DAY, 0)
        selectedCalendar.set(Calendar.MINUTE, 0)
        selectedCalendar.set(Calendar.SECOND, 0)
        selectedCalendar.set(Calendar.MILLISECOND, 0)
        val selectedDayStartMillis = selectedCalendar.timeInMillis // Начало выбранного дня

        val filteredByDate = homeworkToFilter.filter { homework ->
            try {
                // Добавляем проверку на пустую дату выставления
                if (homework.assignmentDate.isBlank()) {
                     Log.e("HomeworkActivity", "Assignment date is blank for homework: ${homework.title}")
                     return@filter false // Исключаем задание, если дата выставления пустая
                }

                // Парсим дату выставления
                val assignmentCalendar = Calendar.getInstance()
                val parsedAssignmentDate = format.parse(homework.assignmentDate)
                 if (parsedAssignmentDate == null) {
                    Log.e("HomeworkActivity", "Could not parse assignment date (parsedAssignmentDate is null): ${homework.assignmentDate}")
                    return@filter false // Исключаем задание с некорректной датой выставления
                }
                assignmentCalendar.time = parsedAssignmentDate
                assignmentCalendar.set(Calendar.HOUR_OF_DAY, 0)
                assignmentCalendar.set(Calendar.MINUTE, 0)
                assignmentCalendar.set(Calendar.SECOND, 0)
                assignmentCalendar.set(Calendar.MILLISECOND, 0)
                val assignmentDateStartMillis = assignmentCalendar.timeInMillis // Начало даты выставления

                // Парсим дату срока сдачи
                val dueCalendar = Calendar.getInstance()
                val parsedDueDate = format.parse(homework.dueDate)
                if (parsedDueDate == null) {
                    Log.e("HomeworkActivity", "Could not parse due date (parsedDueDate is null): ${homework.dueDate}")
                    return@filter false // Исключаем задание с некорректной датой сдачи
                }
                dueCalendar.time = parsedDueDate
                dueCalendar.set(Calendar.HOUR_OF_DAY, 0)
                dueCalendar.set(Calendar.MINUTE, 0)
                dueCalendar.set(Calendar.SECOND, 0)
                dueCalendar.set(Calendar.MILLISECOND, 0)
                val dueDateStartMillis = dueCalendar.timeInMillis // Начало даты сдачи

                // Проверяем, находится ли выбранный день между датой выставления и датой сдачи (включительно)
                val isInRange = selectedDayStartMillis >= assignmentDateStartMillis && selectedDayStartMillis <= dueDateStartMillis

                Log.d("HomeworkActivity", "Comparing dates for ${homework.title} (ID: ${homework.id}): selectedDay=${format.format(selectedDayStartMillis)}, " +
                    "assignmentDate=${homework.assignmentDate}, dueDate=${homework.dueDate}, " +
                    "isInRange=$isInRange")

                isInRange

            } catch (e: Exception) {
                // Логируем ошибку парсинга, но не пропускаем задание, если даты некорректны
                Log.e("HomeworkActivity", "Error parsing date for homework: ${homework.title} (ID: ${homework.id}), assignmentDate: ${homework.assignmentDate}, dueDate: ${homework.dueDate}", e)
                false // Исключаем задание, если парсинг дат вызвал исключение
            }
        }
        Log.d("HomeworkActivity", "Filtered by date count: ${filteredByDate.size}")
        updateUI(filteredByDate)
    }

    override fun onDayClick(dayItem: DayItem) {
        if (dayItem.date == -1L) {
            updateUI(allHomeworkList)
        } else {
            filterHomeworkByDate(dayItem.date)
        }
    }
}