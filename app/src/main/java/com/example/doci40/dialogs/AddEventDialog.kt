package com.example.doci40.dialogs

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import androidx.core.widget.doAfterTextChanged
import com.example.doci40.R
import com.example.doci40.databinding.DialogAddEventBinding
import com.example.doci40.models.EventModel
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import java.text.SimpleDateFormat
import java.util.*

class AddEventDialog : BottomSheetDialogFragment() {

    private var _binding: DialogAddEventBinding? = null
    private val binding get() = _binding!!

    private var selectedDate: Calendar = Calendar.getInstance()
    private var startTime: Calendar = Calendar.getInstance()
    private var endTime: Calendar = Calendar.getInstance().apply { add(Calendar.HOUR_OF_DAY, 1) }

    private val dateFormat = SimpleDateFormat("d MMMM yyyy", Locale("ru"))
    private val timeFormat = SimpleDateFormat("HH:mm", Locale("ru"))

    private var eventToEdit: EventModel? = null
    private var onEventSavedListener: ((EventModel) -> Unit)? = null

    private val eventTypes = arrayOf(
        // Учебные события
        "Лекция",
        "Практика",
        "Лабораторная работа",
        "Семинар",
        "Экзамен",
        "Зачет",
        "Консультация",
        "Пересдача",
        "Защита работы",
        
        // Праздники и мероприятия
        "Праздник",
        "День рождения",
        "Выпускной",
        "Посвящение",
        "Конференция",
        "Олимпиада",
        "Конкурс",
        "Фестиваль",
        
        // Внеучебные мероприятия
        "Собрание",
        "Встреча",
        "Собеседование",
        "Тренинг",
        "Мастер-класс",
        "Экскурсия",
        "Спортивное мероприятие",
        "Культурное мероприятие",
        
        // Другое
        "Другое"
    )

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DialogAddEventBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupViews()
        setupListeners()
        fillEventDataIfEditing()
    }

    private fun setupViews() {
        // Настройка выпадающего списка типов событий
        val adapter = ArrayAdapter(requireContext(), R.layout.item_dropdown, eventTypes)
        (binding.typeLayout.editText as? AutoCompleteTextView)?.setAdapter(adapter)

        // Установка начальных значений
        updateDateField()
        updateTimeFields()
    }

    private fun setupListeners() {
        // Обработчик выбора даты
        binding.dateEditText.setOnClickListener {
            showDatePicker()
        }

        // Обработчики выбора времени
        binding.startTimeEditText.setOnClickListener {
            showTimePicker(true)
        }

        binding.endTimeEditText.setOnClickListener {
            showTimePicker(false)
        }

        // Валидация полей
        binding.titleEditText.doAfterTextChanged {
            validateTitle()
        }

        binding.descriptionEditText.doAfterTextChanged {
            validateDescription()
        }

        binding.locationEditText.doAfterTextChanged {
            validateLocation()
        }

        // Кнопки действий
        binding.buttonCancel.setOnClickListener {
            dismiss()
        }

        binding.buttonSave.setOnClickListener {
            if (validateAll()) {
                saveEvent()
            }
        }
    }

    private fun fillEventDataIfEditing() {
        eventToEdit?.let { event ->
            binding.apply {
                titleEditText.setText(event.title)
                descriptionEditText.setText(event.description)
                locationEditText.setText(event.location)
                (typeLayout.editText as? AutoCompleteTextView)?.setText(event.type, false)

                selectedDate.time = event.date
                startTime.time = event.startTime
                endTime.time = event.endTime

                updateDateField()
                updateTimeFields()
            }
        }
    }

    private fun showDatePicker() {
        DatePickerDialog(
            requireContext(),
            { _, year, month, day ->
                selectedDate.set(year, month, day)
                updateDateField()
            },
            selectedDate.get(Calendar.YEAR),
            selectedDate.get(Calendar.MONTH),
            selectedDate.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    private fun showTimePicker(isStartTime: Boolean) {
        val calendar = if (isStartTime) startTime else endTime
        TimePickerDialog(
            requireContext(),
            { _, hour, minute ->
                calendar.set(Calendar.HOUR_OF_DAY, hour)
                calendar.set(Calendar.MINUTE, minute)
                updateTimeFields()
            },
            calendar.get(Calendar.HOUR_OF_DAY),
            calendar.get(Calendar.MINUTE),
            true
        ).show()
    }

    private fun updateDateField() {
        binding.dateEditText.setText(dateFormat.format(selectedDate.time))
    }

    private fun updateTimeFields() {
        binding.startTimeEditText.setText(timeFormat.format(startTime.time))
        binding.endTimeEditText.setText(timeFormat.format(endTime.time))
    }

    private fun validateTitle(): Boolean {
        val title = binding.titleEditText.text.toString().trim()
        val isValid = title.length >= 3
        binding.titleLayout.error = if (isValid) null else "Минимум 3 символа"
        return isValid
    }

    private fun validateDescription(): Boolean {
        val description = binding.descriptionEditText.text.toString().trim()
        val isValid = description.isNotEmpty()
        binding.descriptionLayout.error = if (isValid) null else "Обязательное поле"
        return isValid
    }

    private fun validateLocation(): Boolean {
        val location = binding.locationEditText.text.toString().trim()
        val isValid = location.isNotEmpty()
        binding.locationLayout.error = if (isValid) null else "Обязательное поле"
        return isValid
    }

    private fun validateTime(): Boolean {
        val isValid = !endTime.before(startTime)
        binding.endTimeLayout.error = if (isValid) null else "Время окончания должно быть позже времени начала"
        return isValid
    }

    private fun validateType(): Boolean {
        val type = (binding.typeLayout.editText as? AutoCompleteTextView)?.text.toString()
        val isValid = type.isNotEmpty()
        binding.typeLayout.error = if (isValid) null else "Выберите тип события"
        return isValid
    }

    private fun validateAll(): Boolean {
        return validateTitle() &&
                validateDescription() &&
                validateLocation() &&
                validateTime() &&
                validateType()
    }

    private fun saveEvent() {
        val event = EventModel(
            title = binding.titleEditText.text.toString().trim(),
            description = binding.descriptionEditText.text.toString().trim(),
            date = selectedDate.time,
            startTime = startTime.time,
            endTime = endTime.time,
            location = binding.locationEditText.text.toString().trim(),
            type = (binding.typeLayout.editText as? AutoCompleteTextView)?.text.toString()
        )

        onEventSavedListener?.invoke(event)
        dismiss()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    fun setOnEventSavedListener(listener: (EventModel) -> Unit) {
        onEventSavedListener = listener
    }

    companion object {
        fun newInstance(event: EventModel? = null) = AddEventDialog().apply {
            eventToEdit = event
        }
    }
} 