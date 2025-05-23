package com.example.doci40.exams.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.doci40.R
import com.example.doci40.exams.models.ExamModel

class ExamsAdapter(
    private var exams: MutableList<ExamModel>
) : RecyclerView.Adapter<ExamsAdapter.ExamViewHolder>() {

    class ExamViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val subjectName: TextView = view.findViewById(R.id.subjectName)
        val examTime: TextView = view.findViewById(R.id.examTime)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ExamViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_exam, parent, false)
        return ExamViewHolder(view)
    }

    override fun onBindViewHolder(holder: ExamViewHolder, position: Int) {
        val exam = exams[position]

        holder.subjectName.text = exam.subject
        holder.examTime.text = exam.time
    }

    override fun getItemCount() = exams.size

    fun updateExams(newExams: List<ExamModel>) {
        exams.clear()
        exams.addAll(newExams)
        notifyDataSetChanged()
    }
}