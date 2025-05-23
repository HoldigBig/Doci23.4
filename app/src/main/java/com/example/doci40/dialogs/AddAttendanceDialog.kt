package com.example.doci40.dialogs

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.example.doci40.R
import com.example.doci40.databinding.DialogAddAttendanceBinding
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.chip.Chip
import java.text.SimpleDateFormat
import java.util.*

class AddAttendanceDialog : BottomSheetDialogFragment() {

    private var _binding: DialogAddAttendanceBinding? = null
    private val binding get() = _binding!!

    private var onAttendanceRecordAddedListener: ((Date, String) -> Unit)? = null

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
        setupDateSelection()
        setupListeners()
    }

    private fun setupDateSelection() {
        binding.tilAttendanceDate.setOnClickListener { showDatePicker() }
        binding.etAttendanceDate.setOnClickListener { showDatePicker() }

        // Set current date by default
        val calendar = Calendar.getInstance()
        val dateFormat = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
        binding.etAttendanceDate.setText(dateFormat.format(calendar.time))
    }

    private fun showDatePicker() {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        val datePickerDialog = DatePickerDialog(
            requireContext(),
            { _, selectedYear, selectedMonth, selectedDay ->
                val selectedCalendar = Calendar.getInstance()
                selectedCalendar.set(selectedYear, selectedMonth, selectedDay)
                val dateFormat = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
                binding.etAttendanceDate.setText(dateFormat.format(selectedCalendar.time))
            },
            year,
            month,
            day
        )
        datePickerDialog.show()
    }

    private fun setupListeners() {
        binding.buttonCancel.setOnClickListener { dismiss() }
        binding.buttonSave.setOnClickListener { saveAttendanceRecord() }
    }

    private fun saveAttendanceRecord() {
        val dateString = binding.etAttendanceDate.text.toString()
        val selectedChipId = binding.chipGroupStatus.checkedChipId

        if (dateString.isBlank()) {
            binding.tilAttendanceDate.error = "Выберите дату"
            return
        }

        if (selectedChipId == -1) {
            Toast.makeText(requireContext(), "Выберите статус посещаемости", Toast.LENGTH_SHORT).show()
            return
        }

        val dateFormat = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
        val date = try {
            dateFormat.parse(dateString)
        } catch (e: Exception) {
            Toast.makeText(requireContext(), "Неверный формат даты", Toast.LENGTH_SHORT).show()
            return
        }

        val status = when (selectedChipId) {
            binding.chipPresent.id -> "present"
            binding.chipAbsent.id -> "absent"
            else -> ""
        }

        if (date != null && status.isNotBlank()) {
            onAttendanceRecordAddedListener?.invoke(date, status)
            dismiss()
        }
    }

    fun setOnAttendanceRecordAddedListener(listener: (Date, String) -> Unit) {
        onAttendanceRecordAddedListener = listener
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        fun newInstance(): AddAttendanceDialog {
            return AddAttendanceDialog()
        }
    }
} 