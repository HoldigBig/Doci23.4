package com.example.doci40.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.doci40.R
import com.example.doci40.adapters.EventsAdapter
import com.example.doci40.databinding.FragmentEventsBinding
import com.example.doci40.dialogs.AddEventDialog
import com.example.doci40.models.EventModel
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import java.util.*

class EventsFragment : Fragment() {

    private var _binding: FragmentEventsBinding? = null
    private val binding get() = _binding!!

    private lateinit var eventsAdapter: EventsAdapter
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

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
        setupRecyclerView()
        setupViews()
        loadEvents()
    }

    private fun setupRecyclerView() {
        eventsAdapter = EventsAdapter { event ->
            showEditEventDialog(event)
        }

        binding.eventsRecyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = eventsAdapter
        }
    }

    private fun setupViews() {
        binding.addEventFab.setOnClickListener {
            showAddEventDialog()
        }
    }

    private fun showAddEventDialog() {
        AddEventDialog.newInstance().apply {
            setOnEventSavedListener { event ->
                saveEvent(event)
            }
        }.show(childFragmentManager, "AddEventDialog")
    }

    private fun showEditEventDialog(event: EventModel) {
        AddEventDialog.newInstance(event).apply {
            setOnEventSavedListener { updatedEvent ->
                updateEvent(event.copy(
                    title = updatedEvent.title,
                    description = updatedEvent.description,
                    date = updatedEvent.date,
                    startTime = updatedEvent.startTime,
                    endTime = updatedEvent.endTime,
                    location = updatedEvent.location,
                    type = updatedEvent.type
                ))
            }
        }.show(childFragmentManager, "EditEventDialog")
    }

    private fun loadEvents() {
        showLoading(true)
        
        auth.currentUser?.let { user ->
            db.collection("users")
                .document(user.uid)
                .collection("events")
                .orderBy("date", Query.Direction.DESCENDING)
                .addSnapshotListener { snapshot, e ->
                    showLoading(false)
                    
                    if (e != null) {
                        showError("Ошибка загрузки событий")
                        return@addSnapshotListener
                    }

                    val events = snapshot?.documents?.mapNotNull { doc ->
                        doc.toObject(EventModel::class.java)?.copy(id = doc.id)
                    } ?: emptyList()

                    updateUI(events)
                }
        } ?: run {
            showLoading(false)
            showError("Пользователь не авторизован")
        }
    }

    private fun saveEvent(event: EventModel) {
        auth.currentUser?.let { user ->
            val eventModel = event.copy(userId = user.uid)

            db.collection("users")
                .document(user.uid)
                .collection("events")
                .add(eventModel)
                .addOnSuccessListener {
                    showMessage("Событие добавлено")
                }
                .addOnFailureListener {
                    showError("Ошибка при добавлении события")
                }
        }
    }

    private fun updateEvent(event: EventModel) {
        auth.currentUser?.let { user ->
            db.collection("users")
                .document(user.uid)
                .collection("events")
                .document(event.id)
                .set(event)
                .addOnSuccessListener {
                    showMessage("Событие обновлено")
                }
                .addOnFailureListener {
                    showError("Ошибка при обновлении события")
                }
        }
    }

    private fun updateUI(events: List<EventModel>) {
        if (events.isEmpty()) {
            binding.emptyView.visibility = View.VISIBLE
            binding.eventsRecyclerView.visibility = View.GONE
        } else {
            binding.emptyView.visibility = View.GONE
            binding.eventsRecyclerView.visibility = View.VISIBLE
            eventsAdapter.submitList(events)
        }
    }

    private fun showLoading(show: Boolean) {
        binding.progressBar.visibility = if (show) View.VISIBLE else View.GONE
    }

    private fun showError(message: String) {
        Snackbar.make(binding.root, message, Snackbar.LENGTH_LONG).show()
    }

    private fun showMessage(message: String) {
        Snackbar.make(binding.root, message, Snackbar.LENGTH_SHORT).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        fun newInstance() = EventsFragment()
    }
} 