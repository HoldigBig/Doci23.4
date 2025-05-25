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
import com.example.doci40.models.Event
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import java.text.SimpleDateFormat
import java.util.*

class AddEventDialog : BottomSheetDialogFragment() {
    private var _binding: DialogAddEventBinding? = null
    private val binding get() = _binding!!
    
    private var eventToEdit: Event? = null
    private var onEventSavedListener: ((Event) -> Unit)? = null
    
    private val eventTypes = arrayOf(
        "Встреча",
        "Дедлайн",
        "Мероприятие",
        "Праздник",
        "Другое"
    )
    
    private val priorities = arrayOf(
        "Высокий",
        "Средний",
        "Низкий"
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
        // Настройка выпадающих списков
        setupDropdown(binding.typeInput, eventTypes)
        setupDropdown(binding.priorityInput, priorities)

        // Установка заголовка
        binding.dialogTitle.text = if (eventToEdit == null) {
            getString(R.string.add_event)
        } else {
            getString(R.string.edit_event)
        }
    }

    private fun setupDropdown(view: AutoCompleteTextView, items: Array<String>) {
        val adapter = ArrayAdapter(requireContext(), R.layout.item_dropdown, items)
        view.setAdapter(adapter)
    }

    private fun setupListeners() {
        // Обработчики для полей даты и времени
        binding.dateInput.setOnClickListener { showDatePicker() }
        binding.startTimeInput.setOnClickListener { showTimePicker(true) }
        binding.endTimeInput.setOnClickListener { showTimePicker(false) }

        // Обработчики кнопок
        binding.buttonCancel.setOnClickListener { dismiss() }
        binding.buttonSave.setOnClickListener { saveEvent() }

        // Валидация полей
        setupValidation()
    }

    private fun setupValidation() {
        val requiredFields = listOf(
            binding.titleInput to "Название события",
            binding.dateInput to "Дата",
            binding.startTimeInput to "Время начала",
            binding.endTimeInput to "Время окончания",
            binding.typeInput to "Тип события",
            binding.priorityInput to "Приоритет"
        )

        requiredFields.forEach { (field, hint) ->
            field.doAfterTextChanged {
                if (it.isNullOrBlank()) {
                    field.error = "Поле '$hint' обязательно для заполнения"
                } else {
                    field.error = null
                }
            }
        }
    }

    private fun showDatePicker() {
        val calendar = Calendar.getInstance()
        val currentDate = binding.dateInput.text.toString()
        
        if (currentDate.isNotEmpty()) {
            val dateFormat = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
            try {
                val date = dateFormat.parse(currentDate)
                if (date != null) {
                    calendar.time = date
                }
            } catch (e: Exception) {
                // Игнорируем ошибку парсинга
            }
        }

        DatePickerDialog(
            requireContext(),
            { _, year, month, dayOfMonth ->
                val selectedDate = String.format("%02d.%02d.%04d", dayOfMonth, month + 1, year)
                binding.dateInput.setText(selectedDate)
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        ).show()
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

    private fun fillEventDataIfEditing() {
        eventToEdit?.let { event ->
            binding.titleInput.setText(event.title)
            binding.descriptionInput.setText(event.description)
            binding.dateInput.setText(event.date)
            binding.startTimeInput.setText(event.startTime)
            binding.endTimeInput.setText(event.endTime)
            binding.locationInput.setText(event.location)
            binding.typeInput.setText(event.type, false)
            binding.priorityInput.setText(event.priority, false)
        }
    }

    private fun saveEvent() {
        if (!validateFields()) {
            return
        }

        val event = Event(
            id = eventToEdit?.id ?: UUID.randomUUID().toString(),
            userId = eventToEdit?.userId ?: "",
            title = binding.titleInput.text.toString(),
            description = binding.descriptionInput.text.toString(),
            date = binding.dateInput.text.toString(),
            startTime = binding.startTimeInput.text.toString(),
            endTime = binding.endTimeInput.text.toString(),
            location = binding.locationInput.text.toString(),
            type = binding.typeInput.text.toString(),
            priority = binding.priorityInput.text.toString()
        )

        onEventSavedListener?.invoke(event)
        dismiss()
    }

    private fun validateFields(): Boolean {
        var isValid = true

        if (binding.titleInput.text.isNullOrBlank()) {
            binding.titleInput.error = "Введите название события"
            isValid = false
        }
        if (binding.dateInput.text.isNullOrBlank()) {
            binding.dateInput.error = "Выберите дату"
            isValid = false
        }
        if (binding.startTimeInput.text.isNullOrBlank()) {
            binding.startTimeInput.error = "Выберите время начала"
            isValid = false
        }
        if (binding.endTimeInput.text.isNullOrBlank()) {
            binding.endTimeInput.error = "Выберите время окончания"
            isValid = false
        }
        if (binding.typeInput.text.isNullOrBlank()) {
            binding.typeInput.error = "Выберите тип события"
            isValid = false
        }
        if (binding.priorityInput.text.isNullOrBlank()) {
            binding.priorityInput.error = "Выберите приоритет"
            isValid = false
        }

        return isValid
    }

    fun setOnEventSavedListener(listener: (Event) -> Unit) {
        onEventSavedListener = listener
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        fun newInstance(eventToEdit: Event? = null): AddEventDialog {
            return AddEventDialog().apply {
                this.eventToEdit = eventToEdit
            }
        }
    }
} 