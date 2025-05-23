package com.example.doci40.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.doci40.R
import com.example.doci40.adapters.ScheduleAdapter
import com.example.doci40.databinding.FragmentScheduleBinding
import com.example.doci40.dialogs.AddScheduleDialog
import com.example.doci40.models.ScheduleModel
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class ScheduleFragment : Fragment() {
    private var _binding: FragmentScheduleBinding? = null
    private val binding get() = _binding!!

    private lateinit var scheduleAdapter: ScheduleAdapter
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentScheduleBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        setupFab()
        loadSchedule()
    }

    private fun setupRecyclerView() {
        scheduleAdapter = ScheduleAdapter { schedule ->
            showEditScheduleDialog(schedule)
        }
        binding.scheduleRecyclerView.apply {
            adapter = scheduleAdapter
            layoutManager = LinearLayoutManager(context)
        }
    }

    private fun setupFab() {
        binding.fabAddSchedule.setOnClickListener {
            showAddScheduleDialog()
        }
    }

    private fun loadSchedule() {
        binding.progressBar.visibility = View.VISIBLE
        binding.scheduleRecyclerView.visibility = View.GONE
        binding.emptyView.visibility = View.GONE

        val userId = auth.currentUser?.uid ?: return
        db.collection("schedules")
            .whereEqualTo("userId", userId)
            .orderBy("dayOfWeek")
            .orderBy("startTime")
            .get()
            .addOnSuccessListener { documents ->
                val schedules = documents.mapNotNull { it.toObject(ScheduleModel::class.java) }
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

    private fun showEditScheduleDialog(schedule: ScheduleModel) {
        AddScheduleDialog.newInstance(schedule).apply {
            setOnScheduleSavedListener { updatedSchedule ->
                updateSchedule(updatedSchedule)
            }
        }.show(childFragmentManager, "EditScheduleDialog")
    }

    private fun saveSchedule(schedule: ScheduleModel) {
        val userId = auth.currentUser?.uid ?: return
        val scheduleData = schedule.copy(userId = userId)
        
        db.collection("schedules")
            .add(scheduleData)
            .addOnSuccessListener {
                loadSchedule()
            }
            .addOnFailureListener { e ->
                Snackbar.make(binding.root, getString(R.string.update_error, e.message), Snackbar.LENGTH_LONG).show()
            }
    }

    private fun updateSchedule(schedule: ScheduleModel) {
        val userId = auth.currentUser?.uid ?: return
        val scheduleData = schedule.copy(userId = userId)
        
        db.collection("schedules")
            .document(schedule.id)
            .set(scheduleData)
            .addOnSuccessListener {
                loadSchedule()
            }
            .addOnFailureListener { e ->
                Snackbar.make(binding.root, getString(R.string.update_error, e.message), Snackbar.LENGTH_LONG).show()
            }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        fun newInstance() = ScheduleFragment()
    }
} 