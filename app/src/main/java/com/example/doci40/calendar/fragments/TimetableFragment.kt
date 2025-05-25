package com.example.doci40.calendar.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.doci40.R
import com.example.doci40.databinding.FragmentTimetableBinding
import com.example.doci40.models.Schedule
import com.example.doci40.calendar.dialogs.AddScheduleDialog
import com.example.doci40.adapters.ScheduleAdapter
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import java.util.*

class TimetableFragment : Fragment() {
    private var _binding: FragmentTimetableBinding? = null
    private val binding get() = _binding!!
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private lateinit var scheduleAdapter: ScheduleAdapter
    private var currentDayOfWeek = Calendar.getInstance().get(Calendar.DAY_OF_WEEK)

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTimetableBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        setupViews()
        loadSchedule()
    }

    private fun setupViews() {
        // Настройка RecyclerView
        scheduleAdapter = ScheduleAdapter { schedule ->
            showEditScheduleDialog(schedule)
        }
        binding.scheduleRecyclerView.apply {
            adapter = scheduleAdapter
            layoutManager = LinearLayoutManager(context)
        }

        // Настройка кнопки добавления
        binding.fabAddSchedule.setOnClickListener {
            showAddScheduleDialog()
        }

        // Настройка выбора дня недели
        setupDayButtons()
    }

    private fun setupDayButtons() {
        val dayButtons = listOf(
            binding.mondayButton,
            binding.tuesdayButton,
            binding.wednesdayButton,
            binding.thursdayButton,
            binding.fridayButton,
            binding.saturdayButton,
            binding.sundayButton
        )

        // Конвертируем Calendar.DAY_OF_WEEK (1-7, где 1 = воскресенье) в наш формат (1-7, где 1 = понедельник)
        val today = if (currentDayOfWeek == Calendar.SUNDAY) 7 else currentDayOfWeek - 1

        dayButtons.forEachIndexed { index, button ->
            button.setOnClickListener {
                currentDayOfWeek = index + 1
                updateSelectedDay(dayButtons)
                loadSchedule()
            }
        }

        // Выбираем текущий день
        dayButtons[today - 1].isChecked = true
    }

    private fun updateSelectedDay(dayButtons: List<View>) {
        dayButtons.forEachIndexed { index, button ->
            button.isSelected = index == currentDayOfWeek - 1
        }
    }

    private fun loadSchedule() {
        binding.progressBar.visibility = View.VISIBLE
        binding.scheduleRecyclerView.visibility = View.GONE
        binding.emptyView.visibility = View.GONE

        val userId = auth.currentUser?.uid ?: return

        db.collection("schedules")
            .whereEqualTo("userId", userId)
            .whereEqualTo("dayOfWeek", currentDayOfWeek)
            .orderBy("startTime", Query.Direction.ASCENDING)
            .get()
            .addOnSuccessListener { documents ->
                val schedules = documents.mapNotNull { it.toObject(Schedule::class.java) }
                if (schedules.isEmpty()) {
                    binding.emptyView.visibility = View.VISIBLE
                    binding.scheduleRecyclerView.visibility = View.GONE
                } else {
                    scheduleAdapter.submitList(schedules)
                    binding.scheduleRecyclerView.visibility = View.VISIBLE
                    binding.emptyView.visibility = View.GONE
                }
                binding.progressBar.visibility = View.GONE
            }
            .addOnFailureListener { e ->
                binding.progressBar.visibility = View.GONE
                Snackbar.make(binding.root, getString(R.string.load_error, e.message), Snackbar.LENGTH_LONG)
                    .setAction(R.string.retry) { loadSchedule() }
                    .show()
            }
    }

    private fun showAddScheduleDialog() {
        AddScheduleDialog.newInstance().apply {
            setOnScheduleSavedListener { schedule ->
                saveSchedule(schedule)
            }
        }.show(childFragmentManager, "AddScheduleDialog")
    }

    private fun showEditScheduleDialog(schedule: Schedule) {
        AddScheduleDialog.newInstance(schedule).apply {
            setOnScheduleSavedListener { updatedSchedule ->
                updateSchedule(updatedSchedule)
            }
        }.show(childFragmentManager, "EditScheduleDialog")
    }

    private fun saveSchedule(schedule: Schedule) {
        val userId = auth.currentUser?.uid ?: return
        val scheduleData = schedule.copy(userId = userId)
        
        db.collection("schedules")
            .add(scheduleData)
            .addOnSuccessListener {
                loadSchedule()
            }
            .addOnFailureListener { e ->
                Toast.makeText(context, getString(R.string.update_error, e.message), Toast.LENGTH_SHORT).show()
            }
    }

    private fun updateSchedule(schedule: Schedule) {
        val userId = auth.currentUser?.uid ?: return
        val scheduleData = schedule.copy(userId = userId)
        
        db.collection("schedules")
            .document(schedule.id)
            .set(scheduleData)
            .addOnSuccessListener {
                loadSchedule()
            }
            .addOnFailureListener { e ->
                Toast.makeText(context, getString(R.string.update_error, e.message), Toast.LENGTH_SHORT).show()
            }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        fun newInstance() = TimetableFragment()
    }
} 