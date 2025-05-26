package com.example.doci40.notifivation.adapters

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.doci40.*
import com.example.doci40.notifivation.model.NotificationModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class NotificationsAdapter(
    private var notifications: MutableList<NotificationModel>,
    private val onDeleteClick: (NotificationModel) -> Unit,
    private val onMarkAsReadClick: (NotificationModel) -> Unit
) : RecyclerView.Adapter<NotificationsAdapter.NotificationViewHolder>() {

    class NotificationViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val titleTextView: TextView = view.findViewById(R.id.notificationTitle)
        val messageTextView: TextView = view.findViewById(R.id.notificationMessage)
        val timeTextView: TextView = view.findViewById(R.id.notificationTime)
        val deleteButton: ImageButton = view.findViewById(R.id.deleteButton)
        val markAsReadButton: ImageButton = view.findViewById(R.id.markAsReadButton)
        val container: View = view.findViewById(R.id.notificationContainer)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NotificationViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_notification, parent, false)
        return NotificationViewHolder(view)
    }

    override fun onBindViewHolder(holder: NotificationViewHolder, position: Int) {
        val notification = notifications[position]
        val context = holder.itemView.context

        // Устанавливаем текст
        holder.titleTextView.text = notification.title
        holder.messageTextView.text = notification.message
        
        // Форматируем и устанавливаем время
        val dateFormat = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault())
        holder.timeTextView.text = dateFormat.format(Date(notification.timestamp))

        // Устанавливаем фон в зависимости от статуса прочтения
        if (notification.isRead) {
            holder.container.setBackgroundColor(ContextCompat.getColor(context, R.color.notification_read_background))
            holder.markAsReadButton.setImageResource(R.drawable.ic_mark_as_unread)
        } else {
            holder.container.setBackgroundColor(ContextCompat.getColor(context, R.color.notification_unread_background))
            holder.markAsReadButton.setImageResource(R.drawable.ic_mark_as_read)
        }

        // Обработчик удаления
        holder.deleteButton.setOnClickListener {
            onDeleteClick(notification)
        }

        // Обработчик отметки о прочтении
        holder.markAsReadButton.setOnClickListener {
            onMarkAsReadClick(notification)
        }

        // Обработчик нажатия на уведомление
        holder.container.setOnClickListener {
            val intent = when {
                notification.type.startsWith("exam_") -> {
                    Intent(context, ExamsActivity::class.java)
                }
                notification.type.startsWith("homework_") -> {
                    Intent(context, HomeworkActivity::class.java)
                }
                notification.type.startsWith("news_") -> {
                    Intent(context, NewsActivity::class.java)
                }
                notification.type.startsWith("result_") -> {
                    Intent(context, ExamsResultActivity::class.java)
                }
                notification.type.startsWith("menu_") -> {
                    Intent(context, FoodActivity::class.java)
                }
                notification.type.startsWith("calendar_") -> {
                    Intent(context, CalendarActivity::class.java)
                }
                else -> null
            }

            intent?.let { context.startActivity(it) }
        }
    }

    override fun getItemCount() = notifications.size

    fun updateNotifications(newNotifications: List<NotificationModel>) {
        notifications.clear()
        notifications.addAll(newNotifications)
        notifyDataSetChanged()
    }
}