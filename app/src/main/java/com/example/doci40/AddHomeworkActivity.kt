package com.example.doci40

import android.app.DatePickerDialog
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.example.doci40.R
import com.example.doci40.homework.models.HomeworkModel
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.MaterialAutoCompleteTextView
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.UUID

class AddHomeworkActivity : AppCompatActivity() {

    private lateinit var toolbar: Toolbar
    private lateinit var subjectInput: MaterialAutoCompleteTextView
    private lateinit var titleInput: TextInputEditText
    private lateinit var descriptionInput: TextInputEditText
    private lateinit var dueDateInput: TextInputEditText
    private lateinit var teacherInput: TextInputEditText
    private lateinit var assignmentDateInput: TextInputEditText
    private lateinit var editDateInput: TextInputEditText
    private lateinit var saveButton: MaterialButton

    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()
    private var isEditMode = false
    private var homeworkToEdit: HomeworkModel? = null
    private val dateFormat = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale("ru"))

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_homework)

        initViews()
        setupToolbar()
        setupInputs()
        setupDatePicker()
        setupSaveButton()

        // Получаем данные для редактирования
        isEditMode = intent.hasExtra("homework_id")
        if (isEditMode) {
            homeworkToEdit = HomeworkModel(
                id = intent.getStringExtra("homework_id") ?: "",
                subject = intent.getStringExtra("subject") ?: "",
                title = intent.getStringExtra("title") ?: "",
                description = intent.getStringExtra("description") ?: "",
                dueDate = intent.getStringExtra("due_date") ?: "",
                teacher = intent.getStringExtra("teacher") ?: "",
                groupId = intent.getStringExtra("group_id") ?: "",
                assignmentDate = intent.getStringExtra("assignment_date") ?: "",
                editDate = intent.getStringExtra("edit_date")
            )
            // Заполняем поля данными
            homeworkToEdit?.let { fillFieldsWithHomework(it) }
            // Обновляем заголовок
            toolbar.title = "Редактировать задание"
        } else {
            toolbar.title = "Новое задание"
            // Устанавливаем текущую дату как дату выставления
            assignmentDateInput.setText(dateFormat.format(Calendar.getInstance().time))
        }
    }

    private fun initViews() {
        toolbar = findViewById(R.id.toolbar)
        subjectInput = findViewById(R.id.subjectInput)
        titleInput = findViewById(R.id.titleInput)
        descriptionInput = findViewById(R.id.descriptionInput)
        dueDateInput = findViewById(R.id.dueDateInput)
        teacherInput = findViewById(R.id.teacherInput)
        assignmentDateInput = findViewById(R.id.assignmentDateInput)
        editDateInput = findViewById(R.id.editDateInput)
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
        val subjects = HomeworkModel.SUBJECTS.toTypedArray()
        subjectInput.setSimpleItems(subjects)
    }

    private fun setupDatePicker() {
        dueDateInput.setOnClickListener {
            val calendar = Calendar.getInstance()
            DatePickerDialog(
                this,
                { _, year, month, day ->
                    calendar.set(year, month, day)
                    val dateFormat = SimpleDateFormat("dd.MM.yyyy", Locale("ru"))
                    dueDateInput.setText(dateFormat.format(calendar.time))
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            ).show()
        }
    }

    private fun fillFieldsWithHomework(homework: HomeworkModel) {
        subjectInput.setText(homework.subject, false)
        titleInput.setText(homework.title)
        descriptionInput.setText(homework.description)
        dueDateInput.setText(homework.dueDate)
        teacherInput.setText(homework.teacher)
        assignmentDateInput.setText(homework.assignmentDate)
        editDateInput.setText(homework.editDate ?: "")
    }

    private fun setupSaveButton() {
        saveButton.setOnClickListener {
            if (validateInputs()) {
                saveHomework()
            }
        }
    }

    private fun validateInputs(): Boolean {
        var isValid = true

        if (subjectInput.text.isNullOrBlank()) {
            subjectInput.error = "Выберите предмет"
            isValid = false
        }

        if (titleInput.text.isNullOrBlank()) {
            titleInput.error = "Введите название"
            isValid = false
        }

        if (descriptionInput.text.isNullOrBlank()) {
            descriptionInput.error = "Введите описание"
            isValid = false
        }

        if (dueDateInput.text.isNullOrBlank()) {
            dueDateInput.error = "Выберите дату сдачи"
            isValid = false
        }

        if (teacherInput.text.isNullOrBlank()) {
            teacherInput.error = "Укажите преподавателя"
            isValid = false
        }

        return isValid
    }

    private fun saveHomework() {
        val currentUser = auth.currentUser
        if (currentUser == null) {
            Toast.makeText(this, "Ошибка авторизации", Toast.LENGTH_SHORT).show()
            return
        }

        // Получаем groupId пользователя
        db.collection("users").document(currentUser.uid).get()
            .addOnSuccessListener { document ->
                val groupId = document.getString("group")
                if (groupId != null) {
                    val homeworkId = homeworkToEdit?.id ?: UUID.randomUUID().toString()
                    val currentTime = dateFormat.format(Calendar.getInstance().time)
                    
                    val homework = HomeworkModel(
                        id = homeworkId,
                        subject = subjectInput.text.toString(),
                        title = titleInput.text.toString(),
                        description = descriptionInput.text.toString(),
                        dueDate = dueDateInput.text.toString(),
                        teacher = teacherInput.text.toString(),
                        groupId = groupId,
                        assignmentDate = if (isEditMode) homeworkToEdit?.assignmentDate ?: currentTime else currentTime,
                        editDate = if (isEditMode) currentTime else null
                    )

                    db.collection("homework")
                        .document(homeworkId)
                        .set(homework)
                        .addOnSuccessListener {
                            val message = if (isEditMode) "Задание успешно обновлено" else "Задание успешно добавлено"
                            Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
                            finish()
                        }
                        .addOnFailureListener { e ->
                            Toast.makeText(this, "Ошибка: ${e.message}", Toast.LENGTH_SHORT).show()
                        }
                } else {
                    Toast.makeText(this, "Ошибка: не найдена группа", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Ошибка: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }
} 