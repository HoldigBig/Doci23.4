package com.example.doci40.exams.fragments

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.card.MaterialCardView
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.Calendar
import com.example.doci40.R
import com.example.doci40.adapters.ExamsAdapter
import com.example.doci40.exams.models.ExamModel

class ExamsFragment : Fragment(R.layout.fragment_exams) {
    private var semester: Int = 1
    private lateinit var dateCard: MaterialCardView
    private lateinit var dateText: TextView
    private lateinit var recyclerView: RecyclerView
    private lateinit var emptyView: TextView
    private lateinit var progressBar: View
    private lateinit var adapter: ExamsAdapter
    private lateinit var clearDateIcon: View
    
    private var selectedDate: Calendar? = null
    
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            semester = it.getInt(ARG_SEMESTER, 1)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        initViews(view)
        setupDateCard()
        setupRecyclerView()
        
        loadExamsForDate(null)
    }

    private fun initViews(view: View) {
        dateCard = view.findViewById(R.id.dateCard)
        dateText = view.findViewById(R.id.dateText)
        recyclerView = view.findViewById(R.id.examsRecyclerView)
        emptyView = view.findViewById(R.id.emptyView)
        progressBar = view.findViewById(R.id.progressBar)
        clearDateIcon = view.findViewById(R.id.clearDateIcon)
        
        updateDateText()
        clearDateIcon.setOnClickListener {
            selectedDate = null
            updateDateText()
            loadExamsForDate(null)
        }
    }

    private fun setupDateCard() {
        dateCard.setOnClickListener {
            showDatePicker()
        }
    }

    private fun setupRecyclerView() {
        adapter = ExamsAdapter()
        recyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = this@ExamsFragment.adapter
        }
    }

    private fun showDatePicker() {
        val calendar = selectedDate ?: Calendar.getInstance()
        val dialog = DatePickerDialog(
            requireContext(),
            R.style.CustomDatePickerDialog,
            { _, year, month, day ->
                selectedDate = Calendar.getInstance().apply { set(year, month, day) }
                updateDateText()
                loadExamsForDate(selectedDate)
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )
        dialog.show()
        dialog.getButton(DatePickerDialog.BUTTON_POSITIVE)?.text = getString(R.string.ok)
        dialog.getButton(DatePickerDialog.BUTTON_NEGATIVE)?.text = getString(R.string.cancel)
    }

    private fun updateDateText() {
        if (selectedDate == null) {
            dateText.text = getString(R.string.all_exams)
            clearDateIcon.visibility = View.GONE
        } else {
            val dateFormat = SimpleDateFormat("d MMMM yyyy", Locale("ru"))
            dateText.text = dateFormat.format(selectedDate!!.time)
            clearDateIcon.visibility = View.VISIBLE
        }
    }

    private fun loadExamsForDate(date: Calendar?) {
        progressBar.visibility = View.VISIBLE
        emptyView.visibility = View.GONE
        recyclerView.visibility = View.GONE

        auth.currentUser?.let { user ->
            var query: Query = db.collection("users")
                .document(user.uid)
                .collection("exams")
                .whereEqualTo("semester", semester)
            if (date != null) {
                val startOfDay = Calendar.getInstance().apply {
                    time = date.time
                    set(Calendar.HOUR_OF_DAY, 0)
                    set(Calendar.MINUTE, 0)
                    set(Calendar.SECOND, 0)
                }.time
                val endOfDay = Calendar.getInstance().apply {
                    time = date.time
                    set(Calendar.HOUR_OF_DAY, 23)
                    set(Calendar.MINUTE, 59)
                    set(Calendar.SECOND, 59)
                }.time
                query = query
                    .whereGreaterThanOrEqualTo("date", startOfDay)
                    .whereLessThanOrEqualTo("date", endOfDay)
            }
            query.get()
                .addOnSuccessListener { documents ->
                    progressBar.visibility = View.GONE
                    if (documents.isEmpty) {
                        emptyView.visibility = View.VISIBLE
                        recyclerView.visibility = View.GONE
                    } else {
                        val exams = documents.mapNotNull { it.toObject(ExamModel::class.java) }
                        adapter.submitList(exams)
                        emptyView.visibility = View.GONE
                        recyclerView.visibility = View.VISIBLE
                    }
                }
                .addOnFailureListener {
                    progressBar.visibility = View.GONE
                    emptyView.visibility = View.VISIBLE
                    recyclerView.visibility = View.GONE
                }
        }
    }

    private fun showLoading() {
        progressBar.visibility = View.VISIBLE
        recyclerView.visibility = View.GONE
        emptyView.visibility = View.GONE
    }

    private fun showContent() {
        progressBar.visibility = View.GONE
        recyclerView.visibility = View.VISIBLE
        emptyView.visibility = View.GONE
    }

    private fun showEmptyState() {
        progressBar.visibility = View.GONE
        recyclerView.visibility = View.GONE
        emptyView.visibility = View.VISIBLE
    }

    private fun showError(message: String) {
        progressBar.visibility = View.GONE
        recyclerView.visibility = View.GONE
        emptyView.visibility = View.VISIBLE
        emptyView.text = message
    }

    fun applyFilter(subject: String?) {
        val filteredExams = subject?.let { subj ->
            adapter.currentList.filter { it.subject == subj }
        } ?: adapter.currentList

        if (filteredExams.isEmpty()) {
            showEmptyState()
        } else {
            adapter.submitList(filteredExams)
            showContent()
        }
    }

    fun applySortOption(sortType: String) {
        val sortedExams = when (sortType) {
            SORT_DATE_ASC -> adapter.currentList.sortedWith(compareBy({ it.date }, { it.startTime }))
            SORT_DATE_DESC -> adapter.currentList.sortedWith(compareByDescending<ExamModel> { it.date }
                .thenByDescending { it.startTime })
            SORT_SUBJECT_ASC -> adapter.currentList.sortedWith(compareBy(
                { it.subject }, { it.date }, { it.startTime }))
            SORT_SUBJECT_DESC -> adapter.currentList.sortedWith(compareByDescending<ExamModel> { it.subject }
                .thenBy { it.date }
                .thenBy { it.startTime })
            else -> adapter.currentList
        }

        if (sortedExams.isEmpty()) {
            showEmptyState()
        } else {
            adapter.submitList(sortedExams)
            showContent()
        }
    }

    private fun showSnackbar(message: String) {
        view?.let {
            Snackbar.make(it, message, Snackbar.LENGTH_LONG)
                .setAction(getString(R.string.retry)) { loadExamsForDate(selectedDate) }
                .show()
        }
    }

    companion object {
        private const val SORT_DATE_ASC = "DATE_ASC"
        private const val SORT_DATE_DESC = "DATE_DESC"
        private const val SORT_SUBJECT_ASC = "SUBJECT_ASC"
        private const val SORT_SUBJECT_DESC = "SUBJECT_DESC"
        private const val ARG_SEMESTER = "semester"

        fun newInstance(semester: Int) = ExamsFragment().apply {
            arguments = Bundle().apply {
                putInt(ARG_SEMESTER, semester)
            }
        }
    }
}