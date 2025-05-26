package com.example.doci40.homework.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.doci40.R
import com.example.doci40.homework.models.HomeworkModel
import java.text.SimpleDateFormat
import java.util.Locale

class HomeworkAdapter : ListAdapter<HomeworkModel, HomeworkAdapter.HomeworkViewHolder>(HomeworkDiffCallback()) {

    private var onHomeworkClickListener: ((HomeworkModel) -> Unit)? = null

    fun setOnHomeworkClickListener(listener: (HomeworkModel) -> Unit) {
        onHomeworkClickListener = listener
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HomeworkViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_homework, parent, false)
        return HomeworkViewHolder(view, onHomeworkClickListener)
    }

    override fun onBindViewHolder(holder: HomeworkViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class HomeworkViewHolder(
        itemView: View,
        private val onHomeworkClickListener: ((HomeworkModel) -> Unit)?
    ) : RecyclerView.ViewHolder(itemView) {
        private val subjectText: TextView = itemView.findViewById(R.id.subjectText)
        private val titleText: TextView = itemView.findViewById(R.id.titleText)
        private val descriptionText: TextView = itemView.findViewById(R.id.descriptionText)
        private val dueDateText: TextView = itemView.findViewById(R.id.dueDateText)
        private val teacherText: TextView = itemView.findViewById(R.id.teacherText)

        fun bind(homework: HomeworkModel) {
            subjectText.text = homework.subject
            titleText.text = homework.title
            descriptionText.text = homework.description
            
            // Форматируем дату
            val inputFormat = SimpleDateFormat("dd.MM.yyyy", Locale("ru"))
            val outputFormat = SimpleDateFormat("d MMMM yyyy", Locale("ru"))
            try {
                val date = inputFormat.parse(homework.dueDate)
                if (date != null) {
                    dueDateText.text = outputFormat.format(date)
                } else {
                    dueDateText.text = homework.dueDate
                }
            } catch (e: Exception) {
                dueDateText.text = homework.dueDate
            }

            teacherText.text = homework.teacher

            itemView.setOnClickListener {
                onHomeworkClickListener?.invoke(homework)
            }
        }
    }

    private class HomeworkDiffCallback : DiffUtil.ItemCallback<HomeworkModel>() {
        override fun areItemsTheSame(oldItem: HomeworkModel, newItem: HomeworkModel): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: HomeworkModel, newItem: HomeworkModel): Boolean {
            return oldItem == newItem
        }
    }
} 