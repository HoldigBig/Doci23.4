package com.example.doci40.exams.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.doci40.R
import com.example.doci40.exams.models.ExamModel

class ExamDetailAdapter : RecyclerView.Adapter<ExamDetailAdapter.ViewHolder>() {

    private var exams = listOf<ExamModel>()

    fun updateExams(newExams: List<ExamModel>) {
        exams = newExams
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_exam_detail, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val exam = exams[position]
        holder.bind(exam)
    }

    override fun getItemCount() = exams.size

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val examName: TextView = itemView.findViewById(R.id.examName)
        private val score: TextView = itemView.findViewById(R.id.score)
        private val grade: TextView = itemView.findViewById(R.id.grade)

        fun bind(exam: ExamModel) {
            examName.text = exam.type
            score.text = exam.date
            grade.text = exam.time
        }
    }
}