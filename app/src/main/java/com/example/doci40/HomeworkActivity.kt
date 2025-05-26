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
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.doci40.databinding.ActivityHomeworkBinding
import com.example.doci40.AddHomeworkActivity
import com.example.doci40.homework.adapters.DayAdapter
import com.example.doci40.homework.adapters.HomeworkAdapter
import com.example.doci40.homework.models.DayItem
import com.example.doci40.homework.models.HomeworkModel
import com.google.android.material.chip.Chip
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
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

    private var snapshotListener: ListenerRegistration? = null

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
        setupRecyclerView()
        startListeningForChanges()
    }

    private fun setupViews() {
        homeworkAdapter = HomeworkAdapter()
        binding.homeworkRecyclerView.layoutManager = LinearLayoutManager(this)
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
            val intent = Intent(this, AddHomeworkActivity::class.java)
            startActivity(intent)
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

    private fun startListeningForChanges() {
        val currentUser = auth.currentUser
        if (currentUser == null) {
            Toast.makeText(this, "Ошибка авторизации", Toast.LENGTH_SHORT).show()
            return
        }

        // Сначала получаем groupId пользователя
        db.collection("users").document(currentUser.uid).get()
            .addOnSuccessListener { document ->
                val groupId = document.getString("group")
                if (groupId != null) {
                    // Теперь слушаем изменения в коллекции homework с фильтром по groupId
                    snapshotListener = db.collection("homework")
                        .whereEqualTo("groupId", groupId)
                        .addSnapshotListener { snapshot, error ->
                            if (error != null) {
                                Log.e(TAG, "Ошибка при получении данных: ", error)
                                Toast.makeText(this, "Ошибка при получении данных: ${error.message}", Toast.LENGTH_SHORT).show()
                                return@addSnapshotListener
                            }

                            if (snapshot != null) {
                                val homeworkList = mutableListOf<HomeworkModel>()
                                for (document in snapshot.documents) {
                                    val homework = document.toObject(HomeworkModel::class.java)
                                    if (homework != null) {
                                        // Добавляем id документа в модель
                                        homework.id = document.id
                                        homeworkList.add(homework)
                                    }
                                }
                                
                                // Сортируем по дате (сначала новые)
                                homeworkList.sortByDescending { it.dueDate }
                                
                                allHomeworkList = homeworkList
                                updateUI(homeworkList)
                                updateSubjectFilter(homeworkList)
                            }
                        }
                } else {
                    Toast.makeText(this, "Группа не найдена", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Ошибка при получении данных пользователя: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun updateUI(homeworkList: List<HomeworkModel>) {
        if (homeworkList.isEmpty()) {
            binding.emptyState.visibility = View.VISIBLE
            binding.homeworkRecyclerView.visibility = View.GONE
        } else {
            binding.emptyState.visibility = View.GONE
            binding.homeworkRecyclerView.visibility = View.VISIBLE
            homeworkAdapter.submitList(homeworkList)
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

    private fun setupRecyclerView() {
        homeworkAdapter.setOnHomeworkClickListener { homework ->
            showHomeworkOptions(homework)
        }
    }

    private fun showHomeworkOptions(homework: HomeworkModel) {
        val options = arrayOf("Редактировать", "Удалить")
        MaterialAlertDialogBuilder(this)
            .setTitle("Действия с домашним заданием")
            .setItems(options) { dialog, which ->
                when (which) {
                    0 -> editHomework(homework)
                    1 -> deleteHomework(homework)
                }
            }
            .show()
    }

    private fun editHomework(homework: HomeworkModel) {
        val intent = Intent(this, AddHomeworkActivity::class.java).apply {
            putExtra("homework_id", homework.id)
            putExtra("subject", homework.subject)
            putExtra("title", homework.title)
            putExtra("description", homework.description)
            putExtra("due_date", homework.dueDate)
            putExtra("teacher", homework.teacher)
            putExtra("group_id", homework.groupId)
            putExtra("assignment_date", homework.assignmentDate)
        }
        startActivity(intent)
    }

    private fun deleteHomework(homework: HomeworkModel) {
        MaterialAlertDialogBuilder(this)
            .setTitle("Удаление домашнего задания")
            .setMessage("Вы уверены, что хотите удалить это домашнее задание?")
            .setPositiveButton("Удалить") { dialog, _ ->
                val currentUser = auth.currentUser
                if (currentUser != null) {
                    db.collection("homework")
                        .document(homework.id)
                        .delete()
                        .addOnSuccessListener {
                            Toast.makeText(this, "Домашнее задание удалено", Toast.LENGTH_SHORT).show()
                        }
                        .addOnFailureListener { e ->
                            Toast.makeText(this, "Ошибка при удалении: ${e.message}", Toast.LENGTH_SHORT).show()
                        }
                }
            }
            .setNegativeButton("Отмена", null)
            .show()
    }

    override fun onDestroy() {
        super.onDestroy()
        snapshotListener?.remove()
    }

    companion object {
        private const val TAG = "HomeworkActivity"
    }
} 