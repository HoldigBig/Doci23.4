package com.example.doci40.dialogs

import android.app.TimePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import androidx.core.widget.doAfterTextChanged
import com.example.doci40.R
import com.example.doci40.databinding.DialogAddScheduleBinding
import com.example.doci40.models.Schedule
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import java.text.SimpleDateFormat
import java.util.*

class AddScheduleDialog : BottomSheetDialogFragment() {
    private var _binding: DialogAddScheduleBinding? = null
    private val binding get() = _binding!!
    
    private var scheduleToEdit: Schedule? = null
    private var onScheduleSavedListener: ((Schedule) -> Unit)? = null
    
    private val daysOfWeek = arrayOf(
        "Понедельник",
        "Вторник",
        "Среда",
        "Четверг",
        "Пятница",
        "Суббота",
        "Воскресенье"
    )
    
    private val lessonTypes = arrayOf(
        "Лекция",
        "Практика",
        "Лабораторная работа",
        "Семинар",
        "Консультация"
    )
    
    private val weekTypes = arrayOf(
        "Каждая неделя",
        "Четная неделя",
        "Нечетная неделя"
    )

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DialogAddScheduleBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupViews()
        setupListeners()
        fillScheduleDataIfEditing()
    }

    private fun setupViews() {
        // Настройка выпадающих списков
        setupDropdown(binding.dayOfWeekInput, daysOfWeek)
        setupDropdown(binding.typeInput, lessonTypes)
        setupDropdown(binding.weekTypeInput, weekTypes)

        // Установка заголовка
        binding.dialogTitle.text = if (scheduleToEdit == null) {
            getString(R.string.add_schedule)
        } else {
            getString(R.string.edit_schedule)
        }
    }

    private fun setupDropdown(view: AutoCompleteTextView, items: Array<String>) {
        val adapter = ArrayAdapter(requireContext(), R.layout.item_dropdown, items)
        view.setAdapter(adapter)
    }

    private fun setupListeners() {
        // Обработчики для полей времени
        binding.startTimeInput.setOnClickListener { showTimePicker(true) }
        binding.endTimeInput.setOnClickListener { showTimePicker(false) }

        // Обработчики кнопок
        binding.buttonCancel.setOnClickListener { dismiss() }
        binding.buttonSave.setOnClickListener { saveSchedule() }

        // Валидация полей
        setupValidation()
    }

    private fun setupValidation() {
        val requiredFields = listOf(
            binding.titleInput to "Название предмета",
            binding.teacherInput to "ФИО преподавателя",
            binding.roomInput to "Номер аудитории",
            binding.startTimeInput to "Время начала",
            binding.endTimeInput to "Время окончания"
        )

        requiredFields.forEach { (field, hint) ->
            field.doAfterTextChanged {
                if (it.isNullOrBlank()) {
                    field.error = "Поле обязательно для заполнения"
                } else {
                    field.error = null
                }
            }
        }
    }

    private fun showTimePicker(isStartTime: Boolean) {
        val calendar = Calendar.getInstance()
        val currentTime = if (isStartTime) binding.startTimeInput.text.toString() 
                         else binding.endTimeInput.text.toString()
        
        if (currentTime.isNotEmpty()) {
            val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
            try {
                val date = timeFormat.parse(currentTime)
                if (date != null) {
                    calendar.time = date
                }
            } catch (e: Exception) {
                // Игнорируем ошибку парсинга
            }
        }

        TimePickerDialog(
            requireContext(),
            { _, hourOfDay, minute ->
                val time = String.format("%02d:%02d", hourOfDay, minute)
                if (isStartTime) {
                    binding.startTimeInput.setText(time)
                } else {
                    binding.endTimeInput.setText(time)
                }
            },
            calendar.get(Calendar.HOUR_OF_DAY),
            calendar.get(Calendar.MINUTE),
            true
        ).show()
    }

    private fun fillScheduleDataIfEditing() {
        scheduleToEdit?.let { schedule ->
            binding.titleInput.setText(schedule.subject)
            binding.teacherInput.setText(schedule.teacher)
            binding.roomInput.setText(schedule.room)
            binding.dayOfWeekInput.setText(daysOfWeek[schedule.dayOfWeek - 1], false)
            binding.startTimeInput.setText(schedule.startTime)
            binding.endTimeInput.setText(schedule.endTime)
            binding.typeInput.setText(schedule.type, false)
            binding.groupInput.setText(schedule.group)
            binding.weekTypeInput.setText(schedule.weekType, false)
        }
    }

    private fun saveSchedule() {
        if (!validateFields()) {
            return
        }

        val schedule = Schedule(
            id = scheduleToEdit?.id ?: UUID.randomUUID().toString(),
            subject = binding.titleInput.text.toString(),
            teacher = binding.teacherInput.text.toString(),
            room = binding.roomInput.text.toString(),
            dayOfWeek = daysOfWeek.indexOf(binding.dayOfWeekInput.text.toString()) + 1,
            startTime = binding.startTimeInput.text.toString(),
            endTime = binding.endTimeInput.text.toString(),
            type = binding.typeInput.text.toString(),
            group = binding.groupInput.text.toString(),
            weekType = binding.weekTypeInput.text.toString(),
            userId = scheduleToEdit?.userId ?: "",
            groupId = scheduleToEdit?.groupId ?: ""
        )

        onScheduleSavedListener?.invoke(schedule)
        dismiss()
    }

    private fun validateFields(): Boolean {
        var isValid = true

        with(binding) {
            if (titleInput.text.isNullOrBlank()) {
                titleInput.error = "Введите название предмета"
                isValid = false
            }
            if (teacherInput.text.isNullOrBlank()) {
                teacherInput.error = "Введите ФИО преподавателя"
                isValid = false
            }
            if (roomInput.text.isNullOrBlank()) {
                roomInput.error = "Введите номер аудитории"
                isValid = false
            }
            if (dayOfWeekInput.text.isNullOrBlank()) {
                dayOfWeekInput.error = "Выберите день недели"
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
            if (typeInput.text.isNullOrBlank()) {
                typeInput.error = "Выберите тип занятия"
                isValid = false
            }
            if (weekTypeInput.text.isNullOrBlank()) {
                weekTypeInput.error = "Выберите тип недели"
                isValid = false
            }
        }

        return isValid
    }

    fun setOnScheduleSavedListener(listener: (Schedule) -> Unit) {
        onScheduleSavedListener = listener
    }

    companion object {
        fun newInstance(scheduleToEdit: Schedule? = null): AddScheduleDialog {
            return AddScheduleDialog().apply {
                this.scheduleToEdit = scheduleToEdit
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
} 