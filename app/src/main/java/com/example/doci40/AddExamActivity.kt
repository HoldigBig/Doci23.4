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
    private lateinit var semesterInput: MaterialAutoCompleteTextView
    private lateinit var saveButton: MaterialButton

    private var selectedDate: Calendar = Calendar.getInstance()
    private var semester: Int = 1
    private var examToEdit: ExamModel? = null
    private var isEditMode = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        setContentView(R.layout.activity_add_exam)

        // Получаем данные для редактирования
        semester = intent.getIntExtra(EXTRA_SEMESTER, 1)
        isEditMode = intent.hasExtra("exam_id")
        if (isEditMode) {
            examToEdit = ExamModel(
                examId = intent.getStringExtra("exam_id") ?: "",
                subject = intent.getStringExtra("subject") ?: "",
                date = intent.getStringExtra("date") ?: "",
                startTime = intent.getStringExtra("start_time") ?: "",
                endTime = intent.getStringExtra("end_time") ?: "",
                location = intent.getStringExtra("location") ?: "",
                examiner = intent.getStringExtra("examiner") ?: "",
                type = intent.getStringExtra("type") ?: "",
                duration = intent.getIntExtra("duration", 0),
                semester = semester
            )
        }

        initViews()
        setupToolbar()
        setupInputs()
        setupDatePicker()
        setupTimePickers()
        setupSaveButton()
        setupWindowInsets()

        // Если это режим редактирования, заполняем поля данными
        examToEdit?.let { fillFieldsWithExam(it) }
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
        semesterInput = findViewById(R.id.semesterInput)
        saveButton = findViewById(R.id.saveButton)

        // Обновляем заголовок в зависимости от режима
        toolbar.title = if (isEditMode) "Редактировать экзамен" else "Добавить экзамен"
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

        // Настройка выпадающего списка семестров
        val semesters = (1..8).map { "Семестр $it" }.toTypedArray()
        semesterInput.setSimpleItems(semesters)
        semesterInput.setText(semesters[semester - 1], false)
        semesterInput.setOnItemClickListener { _, _, position, _ ->
            semester = position + 1
        }

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

        if (semesterInput.text.isNullOrBlank()) {
            semesterInput.error = "Выберите семестр"
            isValid = false
        }

        return isValid
    }

    private fun fillFieldsWithExam(exam: ExamModel) {
        subjectInput.setText(exam.subject)
        dateInput.setText(exam.date)
        startTimeInput.setText(exam.startTime)
        endTimeInput.setText(exam.endTime)
        locationInput.setText(exam.location)
        examinerInput.setText(exam.examiner)
        typeInput.setText(exam.type)
        durationInput.setText(exam.duration.toString())
    }

    private fun saveExam() {
        val examId = examToEdit?.examId ?: UUID.randomUUID().toString()
        val exam = ExamModel(
            examId = examId,
            subject = subjectInput.text.toString(),
            date = dateInput.text.toString(),
            startTime = startTimeInput.text.toString(),
            endTime = endTimeInput.text.toString(),
            location = locationInput.text.toString(),
            examiner = examinerInput.text.toString(),
            type = typeInput.text.toString(),
            duration = durationInput.text.toString().toIntOrNull() ?: 0,
            isActive = true,
            semester = semester
        )

        if (!exam.valid) {
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
                val message = if (isEditMode) "Экзамен успешно обновлен" else "Экзамен успешно добавлен"
                Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
                finish()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Ошибка: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun setupDatePicker() {
        dateInput.setOnClickListener {
            val calendar = Calendar.getInstance()
            val year = calendar.get(Calendar.YEAR)
            val month = calendar.get(Calendar.MONTH)
            val day = calendar.get(Calendar.DAY_OF_MONTH)

            DatePickerDialog(
                this,
                { _, selectedYear, selectedMonth, selectedDay ->
                    calendar.set(selectedYear, selectedMonth, selectedDay)
                    val dateFormat = SimpleDateFormat("dd.MM.yyyy", Locale("ru"))
                    dateInput.setText(dateFormat.format(calendar.time))
                },
                year,
                month,
                day
            ).show()
        }
    }

    private fun setupTimePickers() {
        // Настройка выбора времени начала
        startTimeInput.setOnClickListener {
            val calendar = Calendar.getInstance()
            val hour = calendar.get(Calendar.HOUR_OF_DAY)
            val minute = calendar.get(Calendar.MINUTE)

            TimePickerDialog(
                this,
                { _, selectedHour, selectedMinute ->
                    val timeFormat = SimpleDateFormat("HH:mm", Locale("ru"))
                    calendar.set(Calendar.HOUR_OF_DAY, selectedHour)
                    calendar.set(Calendar.MINUTE, selectedMinute)
                    startTimeInput.setText(timeFormat.format(calendar.time))
                },
                hour,
                minute,
                true
            ).show()
        }

        // Настройка выбора времени окончания
        endTimeInput.setOnClickListener {
            val calendar = Calendar.getInstance()
            val hour = calendar.get(Calendar.HOUR_OF_DAY)
            val minute = calendar.get(Calendar.MINUTE)

            TimePickerDialog(
                this,
                { _, selectedHour, selectedMinute ->
                    val timeFormat = SimpleDateFormat("HH:mm", Locale("ru"))
                    calendar.set(Calendar.HOUR_OF_DAY, selectedHour)
                    calendar.set(Calendar.MINUTE, selectedMinute)
                    endTimeInput.setText(timeFormat.format(calendar.time))
                },
                hour,
                minute,
                true
            ).show()
        }
    }

    private fun setupWindowInsets() {
        // Возвращаем обработчик отступов
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    companion object {
        const val EXTRA_SEMESTER = "extra_semester"
        const val EXTRA_EXAM = "extra_exam"
    }
}