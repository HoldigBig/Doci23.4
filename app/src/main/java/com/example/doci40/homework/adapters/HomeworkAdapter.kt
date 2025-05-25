package com.example.doci40.homework.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.doci40.R
import com.example.doci40.homework.models.HomeworkModel

class HomeworkAdapter : RecyclerView.Adapter<HomeworkAdapter.HomeworkViewHolder>() {
    private var homeworkList: List<HomeworkModel> = listOf()

    fun setData(newHomework: List<HomeworkModel>) {
        homeworkList = newHomework
        notifyDataSetChanged()
    }

    fun getCurrentList(): List<HomeworkModel> = homeworkList

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HomeworkViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_homework, parent, false)
        return HomeworkViewHolder(view)
    }

    override fun onBindViewHolder(holder: HomeworkViewHolder, position: Int) {
        holder.bind(homeworkList[position])
    }

    override fun getItemCount(): Int = homeworkList.size

    inner class HomeworkViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val subjectText: TextView = itemView.findViewById(R.id.subjectText)
        private val teacherText: TextView = itemView.findViewById(R.id.teacherText)
        private val dueDateText: TextView = itemView.findViewById(R.id.dueDateText)
        private val titleText: TextView = itemView.findViewById(R.id.titleText)
        private val descriptionText: TextView = itemView.findViewById(R.id.descriptionText)
        private val assignmentDateText: TextView = itemView.findViewById(R.id.assignmentDateText)

        fun bind(homework: HomeworkModel) {
            subjectText.text = homework.subject
            teacherText.text = homework.teacher
            dueDateText.text = itemView.context.getString(R.string.due_date, homework.dueDate)
            titleText.text = homework.title
            descriptionText.text = homework.description
            assignmentDateText.text = "Выставлено: ${homework.assignmentDate}"
        }
    }
} 