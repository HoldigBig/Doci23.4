package com.example.doci40.homework.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.doci40.databinding.ItemHomeworkBinding
import com.example.doci40.homework.models.HomeworkModel

class HomeworkAdapter : RecyclerView.Adapter<HomeworkAdapter.HomeworkViewHolder>() {
    private var homeworkList: List<HomeworkModel> = emptyList()

    fun updateHomework(newHomework: List<HomeworkModel>) {
        homeworkList = newHomework
        notifyDataSetChanged()
    }

    // Алиас для совместимости с HomeworkActivity
    fun setData(newHomework: List<HomeworkModel>) {
        updateHomework(newHomework)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HomeworkViewHolder {
        val binding = ItemHomeworkBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return HomeworkViewHolder(binding)
    }

    override fun onBindViewHolder(holder: HomeworkViewHolder, position: Int) {
        holder.bind(homeworkList[position])
    }

    override fun getItemCount() = homeworkList.size

    class HomeworkViewHolder(private val binding: ItemHomeworkBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(homework: HomeworkModel) {
            binding.apply {
                textSubject.text = homework.subject
                textTitle.text = homework.title
                textDescription.text = homework.description
                textTeacher.text = homework.teacher
                textDueDate.text = homework.dueDate
            }
        }
    }
}