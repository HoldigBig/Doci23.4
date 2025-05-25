package com.example.doci40.calendar.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.GridLayout
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.example.doci40.R
import com.example.doci40.databinding.FragmentAttendanceBinding
import com.example.doci40.models.Attendance
import com.example.doci40.calendar.dialogs.AddAttendanceDialog
import com.google.android.material.card.MaterialCardView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import java.text.SimpleDateFormat
import java.util.*

class AttendanceFragment : Fragment() {
    private var _binding: FragmentAttendanceBinding? = null
    private val binding get() = _binding!!
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private var currentDate = Calendar.getInstance()
    private var attendanceMap = mutableMapOf<String, Attendance>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAttendanceBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        setupViews()
        updateCalendarDisplay()
        loadAttendanceData()
    }

    private fun setupViews() {
        // Настройка навигации по месяцам
        binding.prevMonth.setOnClickListener {
            currentDate.add(Calendar.MONTH, -1)
            updateCalendarDisplay()
            loadAttendanceData()
        }

        binding.nextMonth.setOnClickListener {
            currentDate.add(Calendar.MONTH, 1)
            updateCalendarDisplay()
            loadAttendanceData()
        }

        // Настройка кнопки добавления
        binding.fabAddAttendance.setOnClickListener {
            showAddAttendanceDialog()
        }
    }

    private fun updateCalendarDisplay() {
        // Обновляем заголовок месяца и года
        val monthFormat = SimpleDateFormat("MMMM yyyy", Locale("ru"))
        binding.monthText.text = monthFormat.format(currentDate.time)

        // Очищаем и заполняем сетку календаря
        binding.calendarGrid.removeAllViews()
        createCalendarGrid()
    }

    private fun createCalendarGrid() {
        val calendar = currentDate.clone() as Calendar
        calendar.set(Calendar.DAY_OF_MONTH, 1)

        // Получаем день недели первого дня месяца (0 = воскресенье)
        var dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK)
        // Корректируем для русской локали (1 = понедельник)
        dayOfWeek = if (dayOfWeek == Calendar.SUNDAY) 7 else dayOfWeek - 1

        // Добавляем заголовки дней недели
        val daysOfWeek = arrayOf("Пн", "Вт", "Ср", "Чт", "Пт", "Сб", "Вс")
        for (day in daysOfWeek) {
            addDayHeader(day)
        }

        // Добавляем пустые ячейки до первого дня месяца
        for (i in 1 until dayOfWeek) {
            addEmptyDay()
        }

        // Добавляем дни месяца
        val daysInMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)
        for (day in 1..daysInMonth) {
            addDay(day)
        }
    }

    private fun addDayHeader(text: String) {
        val textView = TextView(context).apply {
            this.text = text
            textAlignment = View.TEXT_ALIGNMENT_CENTER
            setPadding(8, 8, 8, 8)
        }
        val params = GridLayout.LayoutParams().apply {
            width = 0
            height = GridLayout.LayoutParams.WRAP_CONTENT
            columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f)
        }
        binding.calendarGrid.addView(textView, params)
    }

    private fun addEmptyDay() {
        val view = View(context)
        val params = GridLayout.LayoutParams().apply {
            width = 0
            height = 48 // Высота как у дней с числами
            columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f)
        }
        binding.calendarGrid.addView(view, params)
    }

    private fun addDay(day: Int) {
        val calendar = currentDate.clone() as Calendar
        calendar.set(Calendar.DAY_OF_MONTH, day)
        val dateFormat = SimpleDateFormat("dd.MM.yyyy", Locale("ru"))
        val dateString = dateFormat.format(calendar.time)

        val cardView = MaterialCardView(requireContext()).apply {
            radius = resources.getDimension(R.dimen.calendar_day_radius)
            cardElevation = 0f
            setCardBackgroundColor(getDayColor(dateString))
        }

        val textView = TextView(context).apply {
            text = day.toString()
            textAlignment = View.TEXT_ALIGNMENT_CENTER
            setTextColor(ContextCompat.getColor(context, R.color.white))
            setPadding(8, 8, 8, 8)
        }

        cardView.addView(textView)

        val params = GridLayout.LayoutParams().apply {
            width = 0
            height = GridLayout.LayoutParams.WRAP_CONTENT
            columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f)
            setMargins(4, 4, 4, 4)
        }
        binding.calendarGrid.addView(cardView, params)
    }

    private fun getDayColor(dateString: String): Int {
        val attendance = attendanceMap[dateString]
        val today = SimpleDateFormat("dd.MM.yyyy", Locale("ru")).format(Date())

        return when {
            dateString == today -> ContextCompat.getColor(requireContext(), R.color.background)
            attendance?.isHoliday == true -> ContextCompat.getColor(requireContext(), R.color.gray)
            attendance?.isPresent == false -> ContextCompat.getColor(requireContext(), R.color.red)
            attendance?.isPresent == true -> ContextCompat.getColor(requireContext(), R.color.success)
            else -> ContextCompat.getColor(requireContext(), R.color.gray_light)
        }
    }

    private fun loadAttendanceData() {
        binding.progressBar.visibility = View.VISIBLE
        binding.calendarGrid.visibility = View.GONE
        
        val currentUser = auth.currentUser
        if (currentUser == null) {
            showError("Пользователь не авторизован")
            return
        }

        // Получаем начало и конец текущего месяца
        val startDate = currentDate.clone() as Calendar
        startDate.set(Calendar.DAY_OF_MONTH, 1)
        startDate.set(Calendar.HOUR_OF_DAY, 0)
        startDate.set(Calendar.MINUTE, 0)
        startDate.set(Calendar.SECOND, 0)
        startDate.set(Calendar.MILLISECOND, 0)

        val endDate = startDate.clone() as Calendar
        endDate.add(Calendar.MONTH, 1)
        endDate.add(Calendar.MILLISECOND, -1)

        // Загрузка данных о посещаемости из Firebase
        db.collection("attendance")
            .whereEqualTo("userId", currentUser.uid)
            .whereGreaterThanOrEqualTo("date", startDate.time)
            .whereLessThanOrEqualTo("date", endDate.time)
            .orderBy("date", Query.Direction.ASCENDING)
            .get()
            .addOnSuccessListener { documents ->
                binding.progressBar.visibility = View.GONE
                binding.calendarGrid.visibility = View.VISIBLE
                attendanceMap.clear()

                var totalDays = 0
                var presentDays = 0
                var absentDays = 0

                documents.forEach { doc ->
                    val attendance = doc.toObject(Attendance::class.java)
                    val dateString = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
                        .format(attendance.date)
                    attendanceMap[dateString] = attendance

                    if (!attendance.isHoliday) {
                        totalDays++
                        if (attendance.isPresent) presentDays++ else absentDays++
                    }
                }

                // Обновляем статистику
                binding.totalDaysText.text = getString(R.string.attendance_total_present, totalDays)
                binding.presentDaysText.text = getString(R.string.attendance_total_present, presentDays)
                binding.absentDaysText.text = getString(R.string.attendance_total_absent, absentDays)

                // Обновляем календарь
                updateCalendarDisplay()
            }
            .addOnFailureListener { e ->
                binding.progressBar.visibility = View.GONE
                binding.calendarGrid.visibility = View.VISIBLE
                val message = when {
                    e.message?.contains("FAILED_PRECONDITION") == true -> 
                        "Подождите немного, идет настройка базы данных..."
                    else -> "Ошибка при загрузке данных: ${e.message}"
                }
                showError(message)
            }
    }

    private fun showAddAttendanceDialog() {
        AddAttendanceDialog.newInstance().apply {
            setOnAttendanceSavedListener { date: Date, isPresent: Boolean, isHoliday: Boolean ->
                val attendance = Attendance(
                    userId = auth.currentUser?.uid ?: "",
                    date = date,
                    isPresent = isPresent,
                    isHoliday = isHoliday
                )
                saveAttendance(attendance)
            }
        }.show(childFragmentManager, "AddAttendanceDialog")
    }

    private fun saveAttendance(attendance: Attendance) {
        db.collection("attendance")
            .add(attendance)
            .addOnSuccessListener {
                Toast.makeText(context, "Посещаемость сохранена", Toast.LENGTH_SHORT).show()
                loadAttendanceData()
            }
            .addOnFailureListener { e ->
                showError("Ошибка при сохранении: ${e.message}")
            }
    }

    private fun showError(message: String) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        fun newInstance() = AttendanceFragment()
    }
} 