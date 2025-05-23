package com.example.doci40.exams.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RatingBar
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.progressindicator.CircularProgressIndicator
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.example.doci40.R
import com.example.doci40.AddExamActivity
import com.example.doci40.exams.adapters.ExamDetailAdapter
import com.example.doci40.exams.models.ExamModel
import com.example.doci40.exams.models.SubjectResult

class SubjectDetailFragment : Fragment() {

    private lateinit var subjectTitle: TextView
    private lateinit var gradeIndicator: CircularProgressIndicator
    private lateinit var overallGrade: TextView
    private lateinit var behaviourRating: RatingBar
    private lateinit var attendanceRating: RatingBar
    private lateinit var workRating: RatingBar
    private lateinit var examsRecyclerView: RecyclerView
    private lateinit var examAdapter: ExamDetailAdapter
    private lateinit var addExamButton: FloatingActionButton

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    private var subjectName: String? = null
    private var termId: Int = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            subjectName = it.getString(ARG_SUBJECT_NAME)
            termId = it.getInt(ARG_TERM_ID, 1)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_subject_detail, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initializeFirebase()
        initializeViews(view)
        loadSubjectDetails()
        loadExams()
        setupAddExamButton()
    }

    private fun initializeFirebase() {
        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()
    }

    private fun initializeViews(view: View) {
        subjectTitle = view.findViewById(R.id.subjectTitle)
        gradeIndicator = view.findViewById(R.id.gradeIndicator)
        overallGrade = view.findViewById(R.id.overallGrade)
        behaviourRating = view.findViewById(R.id.behaviourRating)
        attendanceRating = view.findViewById(R.id.attendanceRating)
        workRating = view.findViewById(R.id.workRating)
        examsRecyclerView = view.findViewById(R.id.examsRecyclerView)
        addExamButton = view.findViewById(R.id.addExamButton)

        // Настраиваем RecyclerView
        examAdapter = ExamDetailAdapter()
        examsRecyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = examAdapter
        }

        // Устанавливаем заголовок предмета
        subjectTitle.text = subjectName
    }

    private fun setupAddExamButton() {
        addExamButton.setOnClickListener {
            val intent = Intent(requireContext(), AddExamActivity::class.java).apply {
                putExtra(AddExamActivity.EXTRA_SEMESTER, termId)
                putExtra("subject", subjectName)
            }
            startActivity(intent)
        }
    }

    private fun loadSubjectDetails() {
        val currentUser = auth.currentUser ?: return

        db.collection("users")
            .document(currentUser.uid)
            .collection("results")
            .document("term_$termId")
            .get()
            .addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    val termResult = document.toObject(com.example.doci40.exams.models.TermResult::class.java)
                    termResult?.let { result ->
                        // Находим данные по текущему предмету
                        val subjectResult = result.subjects.find { it.name == subjectName }
                        subjectResult?.let { updateUI(it) }
                    }
                }
            }
    }

    private fun loadExams() {
        val currentUser = auth.currentUser ?: return

        db.collection("users")
            .document(currentUser.uid)
            .collection("exams")
            .whereEqualTo("subject", subjectName)
            .whereEqualTo("semester", termId)
            .get()
            .addOnSuccessListener { documents ->
                val exams = documents.mapNotNull { doc ->
                    doc.toObject(ExamModel::class.java)
                }.sortedBy { it.date }
                examAdapter.updateExams(exams)
            }
    }

    private fun updateUI(subject: SubjectResult) {
        // Обновляем круговой индикатор и оценку
        val score = subject.score
        gradeIndicator.progress = score
        overallGrade.text = "$score%"

        // В реальном приложении здесь бы загружались реальные рейтинги для конкретного предмета
        // Сейчас используем тестовые данные
        behaviourRating.rating = 4f
        attendanceRating.rating = 4f
        workRating.rating = 4f
    }

    companion object {
        private const val ARG_SUBJECT_NAME = "subject_name"
        private const val ARG_TERM_ID = "term_id"

        fun newInstance(subjectName: String, termId: Int) = SubjectDetailFragment().apply {
            arguments = Bundle().apply {
                putString(ARG_SUBJECT_NAME, subjectName)
                putInt(ARG_TERM_ID, termId)
            }
        }
    }
}