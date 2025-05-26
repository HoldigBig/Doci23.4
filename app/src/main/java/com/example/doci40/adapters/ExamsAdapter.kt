package com.example.doci40.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.doci40.R
import com.example.doci40.exams.models.ExamModel

class ExamsAdapter : ListAdapter<ExamModel, ExamsAdapter.ExamViewHolder>(ExamDiffCallback()) {

    private var onExamClickListener: ((ExamModel) -> Unit)? = null

    fun setOnExamClickListener(listener: (ExamModel) -> Unit) {
        onExamClickListener = listener
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ExamViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_exam, parent, false)
        return ExamViewHolder(view, onExamClickListener)
    }

    override fun onBindViewHolder(holder: ExamViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class ExamViewHolder(
        itemView: View,
        private val onExamClickListener: ((ExamModel) -> Unit)?
    ) : RecyclerView.ViewHolder(itemView) {
        private val subjectText: TextView = itemView.findViewById(R.id.subjectText)
        private val typeText: TextView = itemView.findViewById(R.id.typeText)
        private val dateText: TextView = itemView.findViewById(R.id.dateText)
        private val timeText: TextView = itemView.findViewById(R.id.timeText)
        private val locationText: TextView = itemView.findViewById(R.id.locationText)
        private val examinerText: TextView = itemView.findViewById(R.id.examinerText)

        fun bind(exam: ExamModel) {
            subjectText.text = exam.subject
            typeText.text = exam.type
            dateText.text = exam.formattedDate
            timeText.text = exam.fullFormattedTime
            locationText.text = exam.location.takeIf { it.isNotBlank() } ?: "Место не указано"
            examinerText.text = exam.examiner.takeIf { it.isNotBlank() } ?: "Преподаватель не указан"

            itemView.setOnClickListener {
                onExamClickListener?.invoke(exam)
            }
        }
    }

    private class ExamDiffCallback : DiffUtil.ItemCallback<ExamModel>() {
        override fun areItemsTheSame(oldItem: ExamModel, newItem: ExamModel): Boolean {
            return oldItem.examId == newItem.examId
        }

        override fun areContentsTheSame(oldItem: ExamModel, newItem: ExamModel): Boolean {
            return oldItem == newItem
        }
    }
} 