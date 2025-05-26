package com.example.doci40.exams.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.doci40.R
import com.example.doci40.exams.models.SubjectResult

class SubjectResultAdapter(
    private val onSubjectClick: (SubjectResult) -> Unit
) : ListAdapter<SubjectResult, SubjectResultAdapter.ViewHolder>(SubjectDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_subject_result, parent, false)
        return ViewHolder(view, onSubjectClick)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

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

    private class SubjectDiffCallback : DiffUtil.ItemCallback<SubjectResult>() {
        override fun areItemsTheSame(oldItem: SubjectResult, newItem: SubjectResult): Boolean {
            return oldItem.name == newItem.name
        }

        override fun areContentsTheSame(oldItem: SubjectResult, newItem: SubjectResult): Boolean {
            return oldItem == newItem
        }
    }
}