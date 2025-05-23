package com.example.doci40.exams.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.doci40.R
import com.example.doci40.exams.models.SubjectResult

class SubjectResultAdapter(
    private val onSubjectClick: (SubjectResult) -> Unit
) : RecyclerView.Adapter<SubjectResultAdapter.ViewHolder>() {

    private var subjects = listOf<SubjectResult>()

    fun updateSubjects(newSubjects: List<SubjectResult>) {
        subjects = newSubjects
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_subject_result, parent, false)
        return ViewHolder(view, onSubjectClick)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val subject = subjects[position]
        holder.bind(subject)
    }

    override fun getItemCount() = subjects.size

    class ViewHolder(
        itemView: View,
        private val onSubjectClick: (SubjectResult) -> Unit
    ) : RecyclerView.ViewHolder(itemView) {
        private val subjectName: TextView = itemView.findViewById(R.id.subjectName)
        private val score: TextView = itemView.findViewById(R.id.score)
        private val grade: TextView = itemView.findViewById(R.id.grade)

        fun bind(subject: SubjectResult) {
            subjectName.text = subject.name
            score.text = subject.score.toString()
            grade.text = subject.grade

            itemView.setOnClickListener {
                onSubjectClick(subject)
            }
        }
    }
}