package com.example.doci40.notifivation.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.doci40.R
import com.example.doci40.notifivation.model.NotificationModel

class NotificationsAdapter(
    private var notifications: MutableList<NotificationModel>
) : RecyclerView.Adapter<NotificationsAdapter.NotificationViewHolder>() {

    class NotificationViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val titleTextView: TextView = view.findViewById(R.id.notificationTitle)
        val messageTextView: TextView = view.findViewById(R.id.notificationMessage)
        val timeTextView: TextView = view.findViewById(R.id.notificationTime)
        val actionButton: Button = view.findViewById(R.id.actionButton)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NotificationViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_notification, parent, false)
        return NotificationViewHolder(view)
    }

    override fun onBindViewHolder(holder: NotificationViewHolder, position: Int) {
        val notification = notifications[position]

        holder.titleTextView.text = notification.title
        holder.messageTextView.text = notification.message
        holder.timeTextView.text = notification.time

        if (notification.actionButtonText.isNotEmpty()) {
            holder.actionButton.visibility = View.VISIBLE
            holder.actionButton.text = notification.actionButtonText
            holder.actionButton.setOnClickListener {
                // Обработка нажатия на кнопку действия
                when (notification.type) {
                    "meeting" -> {
                        // Логика для присоединения к встрече
                    }
                    "homework" -> {
                        // Логика для перехода к домашнему заданию
                    }
                }
            }
        } else {
            holder.actionButton.visibility = View.GONE
        }
    }

    override fun getItemCount() = notifications.size

    fun updateNotifications(newNotifications: List<NotificationModel>) {
        notifications.clear()
        notifications.addAll(newNotifications)
        notifyDataSetChanged()
    }
}