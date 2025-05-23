package com.example.doci40.news.adapter

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.doci40.R
import com.example.doci40.news.CommentsActivity
import com.example.doci40.news.model.NewsItem
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import com.google.firebase.Timestamp
import androidx.core.content.ContextCompat
import android.graphics.PorterDuff
import com.google.firebase.storage.FirebaseStorage

class NewsAdapter(
    private val onShareClick: (NewsItem) -> Unit
) : RecyclerView.Adapter<NewsAdapter.NewsViewHolder>() {

    private val items = mutableListOf<NewsItem>()
    private val dateFormat = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault())
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NewsViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_news, parent, false)
        return NewsViewHolder(view)
    }

    override fun onBindViewHolder(holder: NewsViewHolder, position: Int) {
        val item = items[position]
        holder.bind(item)
        
        // Предварительная загрузка следующего элемента
        if (position < items.size - 1) {
            val nextItem = items[position + 1]
            holder.preloadNextItem(nextItem)
        }
    }

    override fun getItemCount(): Int = items.size

    fun submitList(newItems: List<NewsItem>) {
        items.clear()
        items.addAll(newItems)
        notifyDataSetChanged()
    }

    inner class NewsViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val schoolLogo: ImageView = itemView.findViewById(R.id.schoolLogo)
        private val schoolName: TextView = itemView.findViewById(R.id.schoolName)
        private val publishDate: TextView = itemView.findViewById(R.id.publishDate)
        private val newsImage: ImageView = itemView.findViewById(R.id.newsImage)
        private val newsTitle: TextView = itemView.findViewById(R.id.newsTitle)
        private val newsText: TextView = itemView.findViewById(R.id.newsText)
        private val likeButton: ImageButton = itemView.findViewById(R.id.likeButton)
        private val likesCount: TextView = itemView.findViewById(R.id.likesCount)
        private val commentButton: ImageButton = itemView.findViewById(R.id.commentButton)
        private val commentsCount: TextView = itemView.findViewById(R.id.commentsCount)
        private val shareButton: ImageButton = itemView.findViewById(R.id.shareButton)

        fun preloadNextItem(item: NewsItem) {
            if (item.schoolLogo.isNotEmpty()) {
                Glide.with(itemView.context)
                    .load(item.schoolLogo)
                    .preload()
            }
        }

        fun bind(item: NewsItem) {
            schoolName.text = item.schoolName
            publishDate.text = dateFormat.format(Date(item.timestamp.seconds * 1000))
            newsTitle.text = item.name
            newsText.text = item.desc
            likesCount.text = item.likesCount
            commentsCount.text = item.commentsCount

            // Загрузка логотипа школы
            if (item.schoolLogo.isNotEmpty()) {
                Glide.with(itemView.context)
                    .load(item.schoolLogo)
                    .placeholder(R.drawable.placeholder_school)
                    .error(R.drawable.placeholder_school)
                    .circleCrop()
                    .into(schoolLogo)
            } else {
                schoolLogo.setImageResource(R.drawable.placeholder_school)
            }

            // Загрузка изображения новости
            if (item.img.isNotEmpty()) {
                newsImage.visibility = View.VISIBLE
                Glide.with(itemView.context)
                    .load(item.img)
                    .placeholder(R.drawable.placeholder_news)
                    .error(R.drawable.placeholder_news)
                    .into(newsImage)
            } else {
                newsImage.visibility = View.GONE
            }

            // Обработка лайков
            likeButton.setImageResource(
                if (item.isLiked) R.drawable.ic_heart_filled
                else R.drawable.ic_heart_outline
            )
            // Устанавливаем цвет сердечка в зависимости от состояния лайка
            val heartColor = if (item.isLiked) R.color.error else R.color.gray
            likeButton.setColorFilter(ContextCompat.getColor(itemView.context, heartColor), PorterDuff.Mode.SRC_IN)

            likeButton.setOnClickListener {
                val currentUser = auth.currentUser
                if (currentUser == null) {
                    Toast.makeText(itemView.context, "Необходимо войти в аккаунт", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }

                val newsRef = db.collection("news").document(item.id)
                val userLikeRef = newsRef.collection("likes").document(currentUser.uid)

                // Проверяем текущее состояние лайка
                userLikeRef.get().addOnSuccessListener { document ->
                    if (document.exists()) {
                        // Убираем лайк
                        userLikeRef.delete().addOnSuccessListener {
                            val newLikesCount = (item.getLikesCountInt() - 1).toString()
                            newsRef.update("likesCount", newLikesCount)
                            // Обновляем UI
                            item.isLiked = false
                            likesCount.text = newLikesCount
                            likeButton.setImageResource(R.drawable.ic_heart_outline)
                            likeButton.setColorFilter(ContextCompat.getColor(itemView.context, R.color.gray), PorterDuff.Mode.SRC_IN) // Устанавливаем серый цвет
                        }
                    } else {
                        // Добавляем лайк
                        userLikeRef.set(mapOf("timestamp" to Timestamp.now())).addOnSuccessListener {
                            val newLikesCount = (item.getLikesCountInt() + 1).toString()
                            newsRef.update("likesCount", newLikesCount)
                            // Обновляем UI
                            item.isLiked = true
                            likesCount.text = newLikesCount
                            likeButton.setImageResource(R.drawable.ic_heart_filled)
                            likeButton.setColorFilter(ContextCompat.getColor(itemView.context, R.color.error), PorterDuff.Mode.SRC_IN) // Устанавливаем красный цвет
                        }
                    }
                }.addOnFailureListener {
                    Toast.makeText(itemView.context, "Ошибка при обновлении лайка", Toast.LENGTH_SHORT).show()
                }
            }

            // Обработка комментариев
            commentButton.setOnClickListener {
                val intent = Intent(itemView.context, CommentsActivity::class.java).apply {
                    putExtra("news_id", item.id)
                }
                itemView.context.startActivity(intent)
            }

            // Обработка кнопки поделиться
            shareButton.setOnClickListener {
                onShareClick(item)
            }
        }
    }
}