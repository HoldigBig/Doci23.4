package com.example.doci40.homework

import android.app.DatePickerDialog
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.doci40.databinding.ActivityAddHomeworkBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class AddHomeworkActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAddHomeworkBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private var selectedDueDate: Calendar = Calendar.getInstance()
    private var selectedAssignmentDate: Calendar = Calendar.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddHomeworkBinding.inflate(layoutInflater)
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
        loadSubjects()
    }

    private fun setupViews() {
        // Настраиваем DatePicker для даты сдачи
        binding.dueDateInput.setOnClickListener {
            showDatePicker(selectedDueDate) { date ->
                selectedDueDate = date
                binding.dueDateInput.setText(formatDate(date))
            }
        }

        // Настраиваем DatePicker для даты выставления
        binding.assignmentDateInput.setOnClickListener {
            showDatePicker(selectedAssignmentDate) { date ->
                selectedAssignmentDate = date
                binding.assignmentDateInput.setText(formatDate(date))
            }
        }

        // Устанавливаем текущие даты
        binding.dueDateInput.setText(formatDate(selectedDueDate))
        binding.assignmentDateInput.setText(formatDate(selectedAssignmentDate))
    }

    private fun setupClickListeners() {
        binding.backButton.setOnClickListener { finish() }

        binding.saveButton.setOnClickListener {
            if (validateInputs()) {
                saveHomework()
            }
        }
    }

    private fun loadSubjects() {
        val subjects = listOf("Математика", "Английский язык", "Арабский", "История", "Физика")
        val adapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, subjects)
        binding.subjectInput.setAdapter(adapter)
    }

    private fun showDatePicker(initialDate: Calendar, onDateSelected: (Calendar) -> Unit) {
        DatePickerDialog(
            this,
            { _, year, month, day ->
                val calendar = Calendar.getInstance().apply {
                    set(Calendar.YEAR, year)
                    set(Calendar.MONTH, month)
                    set(Calendar.DAY_OF_MONTH, day)
                }
                onDateSelected(calendar)
            },
            initialDate.get(Calendar.YEAR),
            initialDate.get(Calendar.MONTH),
            initialDate.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    private fun formatDate(calendar: Calendar): String {
        val dateFormat = SimpleDateFormat("dd.MM.yyyy", Locale("ru"))
        return dateFormat.format(calendar.time)
    }

    private fun validateInputs(): Boolean {
        var isValid = true

        if (binding.subjectInput.text.isNullOrBlank()) {
            binding.subjectLayout.error = "Выберите предмет"
            isValid = false
        }

        if (binding.titleInput.text.isNullOrBlank()) {
            binding.titleLayout.error = "Введите название"
            isValid = false
        }

        if (binding.teacherInput.text.isNullOrBlank()) {
            binding.teacherLayout.error = "Введите имя преподавателя"
            isValid = false
        }

        if (binding.descriptionInput.text.isNullOrBlank()) {
            binding.descriptionLayout.error = "Введите описание"
            isValid = false
        }

        return isValid
    }

    private fun saveHomework() {
        val currentUser = auth.currentUser
        if (currentUser == null) {
            showError("Пользователь не авторизован")
            return
        }

        // Получаем группу пользователя
        db.collection("users").document(currentUser.uid).get()
            .addOnSuccessListener { document ->
                val groupId = document.getString("group")
                if (groupId != null) {
                    val homework = hashMapOf(
                        "subject" to binding.subjectInput.text.toString(),
                        "title" to binding.titleInput.text.toString(),
                        "teacher" to binding.teacherInput.text.toString(),
                        "description" to binding.descriptionInput.text.toString(),
                        "dueDate" to formatDate(selectedDueDate),
                        "assignmentDate" to formatDate(selectedAssignmentDate),
                        "groupId" to groupId
                    )

                    db.collection("homework")
                        .add(homework)
                        .addOnSuccessListener {
                            Toast.makeText(this, "Домашнее задание добавлено", Toast.LENGTH_SHORT).show()
                            finish()
                        }
                        .addOnFailureListener { e ->
                            showError("Ошибка при сохранении: ${e.message}")
                        }
                } else {
                    showError("Группа не найдена")
                }
            }
            .addOnFailureListener { e ->
                showError("Ошибка при получении данных пользователя: ${e.message}")
            }
    }

    private fun showError(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
} 