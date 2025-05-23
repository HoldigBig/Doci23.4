package com.example.doci40.fragments

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.doci40.R
import com.example.doci40.adapters.MonthlyAttendanceAdapter
import com.example.doci40.databinding.FragmentAttendanceBinding
import com.example.doci40.dialogs.AddAttendanceDialog
import com.example.doci40.models.AttendanceRecord
import com.example.doci40.models.AttendanceSummary
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import java.text.SimpleDateFormat
import java.util.*

class AttendanceFragment : Fragment() {

    private var _binding: FragmentAttendanceBinding? = null
    private val binding get() = _binding!!

    private lateinit var monthlyAttendanceAdapter: MonthlyAttendanceAdapter
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

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
        setupRecyclerView()
        setupDateSelection()
        setupFab()
        loadAttendanceData()
    }

    private fun setupRecyclerView() {
        monthlyAttendanceAdapter = MonthlyAttendanceAdapter { attendanceSummary ->
            // Handle item click (e.g., show detailed attendance for the month)
            Snackbar.make(binding.root, getString(R.string.attendance_view_details) + " ${attendanceSummary.month}", Snackbar.LENGTH_SHORT).show()
        }
        binding.rvMonthlyAttendance.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = monthlyAttendanceAdapter
        }
    }

    private fun setupDateSelection() {
        binding.tilSelectDate.setOnClickListener { showDatePicker() }
        binding.etSelectDate.setOnClickListener { showDatePicker() }
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
                binding.etSelectDate.setText(dateFormat.format(selectedCalendar.time))
                // TODO: Filter attendance data based on selected date if needed
            },
            year,
            month,
            day
        )
        datePickerDialog.show()
    }

    private fun setupFab() {
        binding.fabAddAttendance.setOnClickListener { showAddAttendanceDialog() }
    }

    private fun showAddAttendanceDialog() {
        AddAttendanceDialog.newInstance().apply {
            setOnAttendanceRecordAddedListener { date, status ->
                saveAttendanceRecord(date, status)
            }
        }.show(childFragmentManager, "AddAttendanceDialog")
    }

    private fun saveAttendanceRecord(date: Date, status: String) {
        val userId = auth.currentUser?.uid ?: return
        val attendanceRecord = AttendanceRecord(userId = userId, timestamp = date, status = status)

        db.collection("attendance")
            .add(attendanceRecord)
            .addOnSuccessListener {
                Snackbar.make(binding.root, "Запись добавлена", Snackbar.LENGTH_SHORT).show()
                loadAttendanceData() // Reload data after adding
            }
            .addOnFailureListener { e ->
                Snackbar.make(binding.root, "Ошибка при добавлении записи: ${e.message}", Snackbar.LENGTH_LONG).show()
            }
    }

    private fun loadAttendanceData() {
        val userId = auth.currentUser?.uid ?: return

        // Show loading indicator
        binding.progressBar.visibility = View.VISIBLE
        binding.rvMonthlyAttendance.visibility = View.GONE
        binding.emptyView.visibility = View.GONE

        db.collection("attendance")
            .whereEqualTo("userId", userId)
            .orderBy("timestamp", Query.Direction.ASCENDING)
            .get()
            .addOnSuccessListener { documents ->
                binding.progressBar.visibility = View.GONE
                val attendanceRecords = documents.mapNotNull { it.toObject(AttendanceRecord::class.java) }
                processAttendanceData(attendanceRecords)
            }
            .addOnFailureListener { e ->
                binding.progressBar.visibility = View.GONE
                binding.emptyView.visibility = View.VISIBLE
                Snackbar.make(binding.root, "Ошибка загрузки данных: ${e.message}", Snackbar.LENGTH_LONG).show()
            }
    }

    private fun processAttendanceData(records: List<AttendanceRecord>) {
        if (records.isEmpty()) {
            binding.emptyView.visibility = View.VISIBLE
            binding.rvMonthlyAttendance.visibility = View.GONE
            // Reset overall attendance views
            binding.tvAttendancePercentage.text = getString(R.string.attendance_overall_percentage, 0)
            binding.circularProgressIndicator.progress = 0
            binding.tvTotalPresentDays.text = getString(R.string.attendance_total_present, 0)
            binding.tvTotalAbsentDays.text = getString(R.string.attendance_total_absent, 0)
            return
        }

        binding.emptyView.visibility = View.GONE
        binding.rvMonthlyAttendance.visibility = View.VISIBLE

        // Calculate overall attendance
        val totalDays = records.size
        val presentDays = records.count { it.status == "present" }
        val absentDays = totalDays - presentDays
        val overallPercentage = if (totalDays == 0) 0 else (presentDays * 100) / totalDays

        binding.tvAttendancePercentage.text = getString(R.string.attendance_overall_percentage, overallPercentage)
        binding.circularProgressIndicator.progress = overallPercentage
        binding.tvTotalPresentDays.text = getString(R.string.attendance_total_present, presentDays)
        binding.tvTotalAbsentDays.text = getString(R.string.attendance_total_absent, absentDays)

        // Aggregate data by month
        val monthlyData = records.groupBy { record ->
            // Group by Month and Year
            val calendar = Calendar.getInstance()
            calendar.time = record.timestamp ?: Date()
            SimpleDateFormat("MMMM yyyy", Locale("ru")).format(calendar.time)
        }.map { (month, monthRecords) ->
            val monthTotalDays = monthRecords.size
            val monthPresentDays = monthRecords.count { it.status == "present" }
            val monthAbsentDays = monthTotalDays - monthPresentDays
            AttendanceSummary(month, monthPresentDays, monthAbsentDays, monthTotalDays)
        }.sortedBy { // Sort months chronologically (basic attempt)
            try {
                SimpleDateFormat("MMMM yyyy", Locale("ru")).parse(it.month)
            } catch (e: Exception) {
                Date(0) // Put unparseable dates at the beginning
            }
        }

        monthlyAttendanceAdapter.submitList(monthlyData)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        fun newInstance() = AttendanceFragment()
    }
} 