package com.example.doci40.calendar.dialogs

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import com.example.doci40.R
import com.example.doci40.databinding.DialogAddAttendanceBinding
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import java.text.SimpleDateFormat
import java.util.*

class AddAttendanceDialog : BottomSheetDialogFragment() {
    private var _binding: DialogAddAttendanceBinding? = null
    private val binding get() = _binding!!
    private var selectedDate = Calendar.getInstance()
    private var onAttendanceSavedListener: ((Date, Boolean, Boolean) -> Unit)? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DialogAddAttendanceBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupViews()
    }

    private fun setupViews() {
        // Настройка выбора даты
        updateDateDisplay()
        binding.dateLayout.setOnClickListener {
            showDatePicker()
        }

        // Настройка статуса
        val statusAdapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_dropdown_item_1line,
            arrayOf("Присутствовал", "Отсутствовал", "Каникулы/Выходной")
        )
        binding.statusSpinner.setAdapter(statusAdapter)

        // Настройка кнопок
        binding.saveButton.setOnClickListener {
            val status = binding.statusSpinner.text.toString()
            val isPresent = status == "Присутствовал"
            val isHoliday = status == "Каникулы/Выходной"
            onAttendanceSavedListener?.invoke(selectedDate.time, isPresent, isHoliday)
            dismiss()
        }

        binding.cancelButton.setOnClickListener {
            dismiss()
        }
    }

    private fun showDatePicker() {
        DatePickerDialog(
            requireContext(),
            { _, year, month, dayOfMonth ->
                selectedDate.set(year, month, dayOfMonth)
                updateDateDisplay()
            },
            selectedDate.get(Calendar.YEAR),
            selectedDate.get(Calendar.MONTH),
            selectedDate.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    private fun updateDateDisplay() {
        val dateFormat = SimpleDateFormat("dd.MM.yyyy", Locale("ru"))
        binding.dateInput.setText(dateFormat.format(selectedDate.time))
    }

    fun setOnAttendanceSavedListener(listener: (Date, Boolean, Boolean) -> Unit) {
        onAttendanceSavedListener = listener
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        fun newInstance() = AddAttendanceDialog()
    }
} 