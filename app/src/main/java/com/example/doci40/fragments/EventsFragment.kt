package com.example.doci40.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.doci40.R
import com.example.doci40.adapters.EventAdapter
import com.example.doci40.databinding.FragmentEventsBinding
import com.example.doci40.models.Event
import com.example.doci40.calendar.dialogs.AddEventDialog
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import java.text.SimpleDateFormat
import java.util.*

class EventsFragment : Fragment() {
    private var _binding: FragmentEventsBinding? = null
    private val binding get() = _binding!!
    
    private lateinit var eventAdapter: EventAdapter
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentEventsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        setupViews()
        loadEvents()
    }

    private fun setupViews() {
        // Настройка RecyclerView
        eventAdapter = EventAdapter { event ->
            showEditEventDialog(event)
        }
        binding.eventsRecyclerView.apply {
            adapter = eventAdapter
            layoutManager = LinearLayoutManager(context)
        }

        // Настройка кнопки добавления
        binding.fabAddEvent.setOnClickListener {
            showAddEventDialog()
        }
    }

    private fun loadEvents() {
        binding.progressBar.visibility = View.VISIBLE
        binding.eventsRecyclerView.visibility = View.GONE
        binding.emptyView.visibility = View.GONE

        val userId = auth.currentUser?.uid ?: return
        
        // Получаем текущую дату в формате dd.MM.yyyy
        val dateFormat = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
        val currentDate = dateFormat.format(Date())

        db.collection("events")
            .whereEqualTo("userId", userId)
            .whereGreaterThanOrEqualTo("date", currentDate)
            .orderBy("date", Query.Direction.ASCENDING)
            .orderBy("startTime", Query.Direction.ASCENDING)
            .get()
            .addOnSuccessListener { documents ->
                val events = documents.mapNotNull { it.toObject(Event::class.java) }
                if (events.isEmpty()) {
                    binding.emptyView.visibility = View.VISIBLE
                    binding.eventsRecyclerView.visibility = View.GONE
                } else {
                    eventAdapter.submitList(events)
                    binding.eventsRecyclerView.visibility = View.VISIBLE
                    binding.emptyView.visibility = View.GONE
                }
                binding.progressBar.visibility = View.GONE
            }
            .addOnFailureListener { e ->
                binding.progressBar.visibility = View.GONE
                val message = when {
                    e.message?.contains("FAILED_PRECONDITION") == true -> 
                        "Подождите немного, идет настройка базы данных..."
                    else -> getString(R.string.load_events_error, e.message)
                }
                Snackbar.make(binding.root, message, Snackbar.LENGTH_LONG)
                    .setAction(R.string.retry) { loadEvents() }
                    .show()
            }
    }

    private fun showAddEventDialog() {
        AddEventDialog.newInstance().apply {
            setOnEventSavedListener { event ->
                saveEvent(event)
            }
        }.show(childFragmentManager, "AddEventDialog")
    }

    private fun showEditEventDialog(event: Event) {
        AddEventDialog.newInstance(event).apply {
            setOnEventSavedListener { updatedEvent ->
                updateEvent(updatedEvent)
            }
        }.show(childFragmentManager, "EditEventDialog")
    }

    private fun saveEvent(event: Event) {
        val userId = auth.currentUser?.uid ?: return
        val eventData = event.copy(userId = userId)
        
        db.collection("events")
            .add(eventData)
            .addOnSuccessListener {
                loadEvents()
            }
            .addOnFailureListener { e ->
                Snackbar.make(binding.root, getString(R.string.save_event_error, e.message), Snackbar.LENGTH_LONG).show()
            }
    }

    private fun updateEvent(event: Event) {
        val userId = auth.currentUser?.uid ?: return
        val eventData = event.copy(userId = userId)
        
        db.collection("events")
            .document(event.id)
            .set(eventData)
            .addOnSuccessListener {
                loadEvents()
            }
            .addOnFailureListener { e ->
                Snackbar.make(binding.root, getString(R.string.save_event_error, e.message), Snackbar.LENGTH_LONG).show()
            }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        fun newInstance() = EventsFragment()
    }
} 