package com.example.doci40

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import com.example.doci40.exams.models.ExamModel
import com.example.doci40.exams.utils.DateUtils
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.MaterialAutoCompleteTextView
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.UUID

class AddExamActivity : AppCompatActivity() {

    private lateinit var toolbar: Toolbar
    private lateinit var subjectInput: MaterialAutoCompleteTextView
    private lateinit var dateInput: TextInputEditText
    private lateinit var startTimeInput: TextInputEditText
    private lateinit var endTimeInput: TextInputEditText
    private lateinit var locationInput: TextInputEditText
    private lateinit var examinerInput: TextInputEditText
    private lateinit var typeInput: MaterialAutoCompleteTextView
    private lateinit var durationInput: TextInputEditText
    private lateinit var saveButton: MaterialButton

    private var selectedDate: Calendar = Calendar.getInstance()
    private var semester: Int = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Включаем edge-to-edge
        WindowCompat.setDecorFitsSystemWindows(window, false)

        setContentView(R.layout.activity_add_exam)

        // Устанавливаем темные иконки для строки состояния (для светлого фона)
        val windowInsetsController = WindowCompat.getInsetsController(window, window.decorView)
        windowInsetsController.isAppearanceLightStatusBars = true

        // Возвращаем обработчик отступов
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        semester = intent.getIntExtra(EXTRA_SEMESTER, 1)

        initViews()
        setupToolbar()
        setupInputs()
        setupSaveButton()
    }

    private fun initViews() {
        toolbar = findViewById(R.id.toolbar)
        subjectInput = findViewById(R.id.subjectInput)
        dateInput = findViewById(R.id.dateInput)
        startTimeInput = findViewById(R.id.startTimeInput)
        endTimeInput = findViewById(R.id.endTimeInput)
        locationInput = findViewById(R.id.locationInput)
        examinerInput = findViewById(R.id.examinerInput)
        typeInput = findViewById(R.id.typeInput)
        durationInput = findViewById(R.id.durationInput)
        saveButton = findViewById(R.id.saveButton)
    }

    private fun setupToolbar() {
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)
        toolbar.setNavigationOnClickListener { onBackPressed() }
    }

    private fun setupInputs() {
        // Настройка выпадающего списка предметов
        subjectInput.setSimpleItems(ExamModel.SUBJECTS.toTypedArray())

        // Настройка выпадающего списка типов экзаменов
        typeInput.setSimpleItems(ExamModel.EXAM_TYPES.toTypedArray())

        // Настройка выбора даты
        dateInput.setOnClickListener {
            showDatePicker()
        }

        // Настройка выбора времени начала
        startTimeInput.setOnClickListener {
            showTimePicker(startTimeInput)
        }

        // Настройка выбора времени окончания
        endTimeInput.setOnClickListener {
            showTimePicker(endTimeInput)
        }
    }

    private fun showDatePicker() {
        val calendar = Calendar.getInstance()
        DatePickerDialog(
            this,
            { _, year, month, day ->
                selectedDate.set(year, month, day)
                val dateFormat = SimpleDateFormat("dd.MM.yyyy", Locale("ru"))
                dateInput.setText(dateFormat.format(selectedDate.time))
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    private fun showTimePicker(timeInput: TextInputEditText) {
        val calendar = Calendar.getInstance()
        TimePickerDialog(
            this,
            { _, hour, minute ->
                val timeFormat = SimpleDateFormat("HH:mm", Locale("ru"))
                calendar.set(Calendar.HOUR_OF_DAY, hour)
                calendar.set(Calendar.MINUTE, minute)
                timeInput.setText(timeFormat.format(calendar.time))
            },
            calendar.get(Calendar.HOUR_OF_DAY),
            calendar.get(Calendar.MINUTE),
            true
        ).show()
    }

    private fun setupSaveButton() {
        saveButton.setOnClickListener {
            if (validateInputs()) {
                saveExam()
            }
        }
    }

    private fun validateInputs(): Boolean {
        var isValid = true

        if (subjectInput.text.isNullOrBlank()) {
            subjectInput.error = "Выберите предмет"
            isValid = false
        }

        if (dateInput.text.isNullOrBlank()) {
            dateInput.error = "Выберите дату"
            isValid = false
        }

        if (startTimeInput.text.isNullOrBlank()) {
            startTimeInput.error = "Выберите время начала"
            isValid = false
        }

        if (endTimeInput.text.isNullOrBlank()) {
            endTimeInput.error = "Выберите время окончания"
            isValid = false
        }

        // Проверка, что время окончания позже времени начала
        if (!startTimeInput.text.isNullOrBlank() && !endTimeInput.text.isNullOrBlank()) {
            val startMinutes = DateUtils.parseTime(startTimeInput.text.toString())
            val endMinutes = DateUtils.parseTime(endTimeInput.text.toString())
            if (endMinutes <= startMinutes) {
                endTimeInput.error = "Время окончания должно быть позже времени начала"
                isValid = false
            }
        }

        if (locationInput.text.isNullOrBlank()) {
            locationInput.error = "Укажите место проведения"
            isValid = false
        }

        if (examinerInput.text.isNullOrBlank()) {
            examinerInput.error = "Укажите экзаменатора"
            isValid = false
        }

        if (typeInput.text.isNullOrBlank()) {
            typeInput.error = "Выберите тип"
            isValid = false
        }

        if (durationInput.text.isNullOrBlank()) {
            durationInput.error = "Укажите длительность"
            isValid = false
        }

        return isValid
    }

    private fun saveExam() {
        val examId = UUID.randomUUID().toString()
        val exam = ExamModel(
            examId = examId,
            subject = subjectInput.text.toString(),
            date = dateInput.text.toString(),
            startTime = startTimeInput.text.toString(),
            endTime = endTimeInput.text.toString(),
            isActive = true,
            semester = semester,
        )

        if (!exam.isValid()) {
            Toast.makeText(this, "Проверьте правильность заполнения полей", Toast.LENGTH_SHORT).show()
            return
        }

        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser == null) {
            Toast.makeText(this, "Ошибка авторизации", Toast.LENGTH_SHORT).show()
            return
        }

        FirebaseFirestore.getInstance()
            .collection("users")
            .document(currentUser.uid)
            .collection("exams")
            .document(examId)
            .set(exam)
            .addOnSuccessListener {
                Toast.makeText(this, "Экзамен успешно добавлен", Toast.LENGTH_SHORT).show()
                finish()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Ошибка: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
            }
    }

    companion object {
        const val EXTRA_SEMESTER = "extra_semester"
    }
}