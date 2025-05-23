package com.example.doci40

import android.app.DatePickerDialog
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
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
    private val calendar = Calendar.getInstance()
    private val dateFormat = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddHomeworkBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        setupToolbar()
        setupDatePickers()
        setupSaveButton()
    }

    private fun setupToolbar() {
        binding.toolbar.setNavigationOnClickListener { finish() }
    }

    private fun setupDatePickers() {
        // Устанавливаем текущую дату как дату выставления задания
        binding.editTextAssignmentDate.setText(dateFormat.format(calendar.time))

        binding.editTextAssignmentDate.setOnClickListener {
            showDatePicker { selectedDate ->
                binding.editTextAssignmentDate.setText(dateFormat.format(selectedDate.time))
            }
        }

        binding.editTextDueDate.setOnClickListener {
            showDatePicker { selectedDate ->
                binding.editTextDueDate.setText(dateFormat.format(selectedDate.time))
            }
        }
    }

    private fun showDatePicker(onDateSelected: (Calendar) -> Unit) {
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        DatePickerDialog(
            this,
            { _, selectedYear, selectedMonth, selectedDay ->
                val selectedDate = Calendar.getInstance().apply {
                    set(selectedYear, selectedMonth, selectedDay)
                }
                onDateSelected(selectedDate)
            },
            year,
            month,
            day
        ).show()
    }

    private fun setupSaveButton() {
        binding.buttonSaveHomework.setOnClickListener {
            val subject = binding.editTextSubject.text.toString()
            val title = binding.editTextTaskTitle.text.toString()
            val description = binding.editTextDescription.text.toString()
            val assignmentDate = binding.editTextAssignmentDate.text.toString()
            val dueDate = binding.editTextDueDate.text.toString()
            val teacher = binding.editTextTeacher.text.toString()

            if (subject.isBlank() || title.isBlank() || dueDate.isBlank()) {
                Toast.makeText(this, "Заполните обязательные поля", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Добавляем валидацию формата даты для dueDate
            dateFormat.isLenient = false // Строгий парсинг: дата должна точно соответствовать формату

            try {
                dateFormat.parse(dueDate)
            } catch (e: java.text.ParseException) {
                Toast.makeText(this, "Неверный формат даты срока сдачи. Используйте формат ДД.ММ.ГГГГ", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            saveHomework(subject, title, description, assignmentDate, dueDate, teacher)
        }
    }

    private fun saveHomework(
        subject: String,
        title: String,
        description: String,
        assignmentDate: String,
        dueDate: String,
        teacher: String
    ) {
        val currentUser = auth.currentUser
        currentUser?.let { user ->
            db.collection("users").document(user.uid).get()
                .addOnSuccessListener { userDoc ->
                    if (userDoc.exists()) {
                        val groupId = userDoc.getString("group")
                        if (groupId != null) {
                            val homework = hashMapOf(
                                "subject" to subject,
                                "title" to title,
                                "description" to description,
                                "assignmentDate" to assignmentDate,
                                "dueDate" to dueDate,
                                "teacher" to teacher,
                                "groupId" to groupId
                            )

                            db.collection("homework")
                                .add(homework)
                                .addOnSuccessListener {
                                    Toast.makeText(this, "Задание добавлено", Toast.LENGTH_SHORT).show()
                                    finish()
                                }
                                .addOnFailureListener { e ->
                                    Log.e("AddHomework", "Ошибка при добавлении задания", e)
                                    Toast.makeText(this, "Ошибка при добавлении задания", Toast.LENGTH_SHORT).show()
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
} 