package com.example.doci40.calendar.dialogs

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import com.example.doci40.R
import com.example.doci40.databinding.DialogAddScheduleBinding
import com.example.doci40.models.Schedule
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.timepicker.MaterialTimePicker
import com.google.android.material.timepicker.TimeFormat
import java.text.SimpleDateFormat
import java.util.*

class AddScheduleDialog : BottomSheetDialogFragment() {
    private var _binding: DialogAddScheduleBinding? = null
    private val binding get() = _binding!!
    private var onScheduleSavedListener: ((Schedule) -> Unit)? = null
    private var scheduleToEdit: Schedule? = null

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
        fillScheduleDataIfEditing()
    }

    private fun setupViews() {
        // Установка заголовка
        binding.dialogTitle.text = if (scheduleToEdit == null) {
            getString(R.string.add_schedule)
        } else {
            getString(R.string.edit_schedule)
        }

        // Настройка выбора дня недели
        val daysAdapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_dropdown_item_1line,
            arrayOf("Понедельник", "Вторник", "Среда", "Четверг", "Пятница", "Суббота", "Воскресенье")
        )
        binding.dayOfWeekInput.setAdapter(daysAdapter)

        // Настройка типа занятия
        val typeAdapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_dropdown_item_1line,
            arrayOf("Лекция", "Практика", "Семинар", "Лабораторная")
        )
        binding.typeInput.setAdapter(typeAdapter)

        // Настройка типа недели
        val weekTypeAdapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_dropdown_item_1line,
            arrayOf("Каждая неделя", "Четная неделя", "Нечетная неделя")
        )
        binding.weekTypeInput.setAdapter(weekTypeAdapter)

        // Настройка выбора времени
        binding.startTimeInput.setOnClickListener {
            showTimePicker(true)
        }

        binding.endTimeInput.setOnClickListener {
            showTimePicker(false)
        }

        // Настройка кнопок
        binding.buttonSave.setOnClickListener {
            saveSchedule()
        }

        binding.buttonCancel.setOnClickListener {
            dismiss()
        }
    }

    private fun fillScheduleDataIfEditing() {
        scheduleToEdit?.let { schedule ->
            binding.titleInput.setText(schedule.subject)
            binding.teacherInput.setText(schedule.teacher)
            binding.roomInput.setText(schedule.room)
            
            // Конвертируем номер дня недели в название
            val dayName = when (schedule.dayOfWeek) {
                1 -> "Понедельник"
                2 -> "Вторник"
                3 -> "Среда"
                4 -> "Четверг"
                5 -> "Пятница"
                6 -> "Суббота"
                7 -> "Воскресенье"
                else -> "Понедельник"
            }
            binding.dayOfWeekInput.setText(dayName, false)
            
            binding.startTimeInput.setText(schedule.startTime)
            binding.endTimeInput.setText(schedule.endTime)
            binding.typeInput.setText(schedule.type, false)
            binding.weekTypeInput.setText(schedule.weekType, false)
        }
    }

    private fun showTimePicker(isStartTime: Boolean) {
        val picker = MaterialTimePicker.Builder()
            .setTimeFormat(TimeFormat.CLOCK_24H)
            .setHour(8)
            .setMinute(0)
            .setTitleText(if (isStartTime) "Выберите время начала" else "Выберите время окончания")
            .build()

        picker.addOnPositiveButtonClickListener {
            val timeString = String.format("%02d:%02d", picker.hour, picker.minute)
            if (isStartTime) {
                binding.startTimeInput.setText(timeString)
            } else {
                binding.endTimeInput.setText(timeString)
            }
        }

        picker.show(childFragmentManager, "TimePicker")
    }

    private fun saveSchedule() {
        val subject = binding.titleInput.text.toString()
        val teacher = binding.teacherInput.text.toString()
        val room = binding.roomInput.text.toString()
        val dayOfWeek = binding.dayOfWeekInput.text.toString()
        val startTime = binding.startTimeInput.text.toString()
        val endTime = binding.endTimeInput.text.toString()
        val type = binding.typeInput.text.toString()
        val weekType = binding.weekTypeInput.text.toString()

        // Валидация
        if (subject.isBlank() || teacher.isBlank() || room.isBlank() || 
            dayOfWeek.isBlank() || startTime.isBlank() || endTime.isBlank() ||
            type.isBlank() || weekType.isBlank()) {
            return
        }

        val dayNumber = when (dayOfWeek) {
            "Понедельник" -> 1
            "Вторник" -> 2
            "Среда" -> 3
            "Четверг" -> 4
            "Пятница" -> 5
            "Суббота" -> 6
            "Воскресенье" -> 7
            else -> 1
        }

        val schedule = Schedule(
            id = scheduleToEdit?.id ?: UUID.randomUUID().toString(),
            subject = subject,
            teacher = teacher,
            room = room,
            dayOfWeek = dayNumber,
            startTime = startTime,
            endTime = endTime,
            type = type,
            weekType = weekType,
            userId = scheduleToEdit?.userId ?: ""
        )

        onScheduleSavedListener?.invoke(schedule)
        dismiss()
    }

    fun setOnScheduleSavedListener(listener: (Schedule) -> Unit) {
        onScheduleSavedListener = listener
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        fun newInstance() = AddScheduleDialog()
        
        fun newInstance(schedule: Schedule) = AddScheduleDialog().apply {
            this.scheduleToEdit = schedule
        }
    }
} 