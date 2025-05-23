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
import java.text.SimpleDateFormat
import java.util.*

class ExamsAdapter : ListAdapter<ExamModel, ExamsAdapter.ExamViewHolder>(ExamDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ExamViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_exam, parent, false)
        return ExamViewHolder(view)
    }

    override fun onBindViewHolder(holder: ExamViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class ExamViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val subjectText: TextView = itemView.findViewById(R.id.subjectText)
        private val timeText: TextView = itemView.findViewById(R.id.timeText)
        private val locationText: TextView = itemView.findViewById(R.id.locationText)
        private val examinerText: TextView = itemView.findViewById(R.id.examinerText)

        fun bind(exam: ExamModel) {
            subjectText.text = exam.subject
            timeText.text = "${exam.startTime} - ${exam.endTime}"
            locationText.text = exam.location
            examinerText.text = exam.examiner
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