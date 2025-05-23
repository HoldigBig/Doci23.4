package com.example.doci40.news

import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.doci40.R
import com.example.doci40.news.adapter.CommentsAdapter
import com.example.doci40.news.model.Comment
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import java.util.*

class CommentsActivity : AppCompatActivity() {

    private lateinit var backButton: ImageButton
    private lateinit var commentsList: RecyclerView
    private lateinit var commentInput: EditText
    private lateinit var sendButton: ImageButton
    private lateinit var progressBar: ProgressBar
    private lateinit var errorText: TextView
    private lateinit var retryButton: ImageButton
    private lateinit var emptyText: TextView

    private lateinit var commentsAdapter: CommentsAdapter
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private var newsId: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        setContentView(R.layout.activity_comments)

        // Устанавливаем цвет иконок статус бара на черный
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
            window.statusBarColor = Color.TRANSPARENT // Опционально, если нужен прозрачный статус бар
        }

        newsId = intent.getStringExtra("news_id") ?: run {
            Toast.makeText(this, "Ошибка: не указан ID новости", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        initializeViews()
        setupRecyclerView()
        setupListeners()
        loadComments()
    }

    private fun initializeViews() {
        backButton = findViewById(R.id.backButton)
        commentsList = findViewById(R.id.commentsList)
        commentInput = findViewById(R.id.commentInput)
        sendButton = findViewById(R.id.sendButton)
        progressBar = findViewById(R.id.progressBar)
        errorText = findViewById(R.id.errorText)
        retryButton = findViewById(R.id.retryButton)
        emptyText = findViewById(R.id.emptyText)
    }

    private fun setupRecyclerView() {
        commentsAdapter = CommentsAdapter()
        commentsList.apply {
            layoutManager = LinearLayoutManager(this@CommentsActivity)
            adapter = commentsAdapter
        }
    }

    private fun setupListeners() {
        backButton.setOnClickListener { onBackPressed() }
        retryButton.setOnClickListener { loadComments() }

        sendButton.setOnClickListener {
            val commentText = commentInput.text?.toString()?.trim()
            if (!commentText.isNullOrEmpty()) {
                sendComment(commentText)
            }
        }
    }

    private fun loadComments() {
        showLoading()

        db.collection("news")
            .document(newsId)
            .collection("comments")
            .orderBy("timestamp", com.google.firebase.firestore.Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    showError("Ошибка загрузки комментариев: ${e.message}")
                    return@addSnapshotListener
                }

                if (snapshot == null) {
                    showError("Не удалось загрузить комментарии")
                    return@addSnapshotListener
                }

                val comments = mutableListOf<Comment>()
                snapshot.documents.forEach { doc ->
                    doc.toObject(Comment::class.java)?.let { comment ->
                        comment.id = doc.id // Устанавливаем ID из документа
                        comments.add(comment)
                    }
                }
                
                if (comments.isEmpty()) {
                    showEmpty()
                } else {
                    showContent()
                    commentsAdapter.submitList(comments)
                }
            }
    }

    private fun sendComment(text: String) {
        val currentUser = auth.currentUser
        if (currentUser == null) {
            Toast.makeText(this, "Необходимо войти в аккаунт", Toast.LENGTH_SHORT).show()
            return
        }

        // Проверяем, что текст не пустой
        if (text.trim().isEmpty()) {
            Toast.makeText(this, "Комментарий не может быть пустым", Toast.LENGTH_SHORT).show()
            return
        }

        // Показываем индикатор загрузки
        progressBar.visibility = View.VISIBLE
        commentInput.isEnabled = false
        sendButton.isEnabled = false

        val comment = Comment(
            id = UUID.randomUUID().toString(),
            newsId = newsId,
            userId = currentUser.uid,
            userName = currentUser.displayName ?: "Пользователь",
            userImageUrl = currentUser.photoUrl?.toString() ?: "",
            text = text.trim(),
            timestamp = Timestamp.now()
        )

        // Используем транзакцию для атомарного обновления
        db.runTransaction { transaction ->
            // Проверяем существование новости
            val newsRef = db.collection("news").document(newsId)
            val newsDoc = transaction.get(newsRef)
            if (!newsDoc.exists()) {
                throw FirebaseFirestoreException(
                    "Новость не найдена",
                    FirebaseFirestoreException.Code.NOT_FOUND
                )
            }

            // Добавляем комментарий
            val commentRef = newsRef.collection("comments").document(comment.id)
            transaction.set(commentRef, comment)

            // Обновляем счетчик комментариев
            val currentCount = newsDoc.getString("commentsCount")?.toIntOrNull() ?: 0
            transaction.update(newsRef, "commentsCount", (currentCount + 1).toString())
        }.addOnSuccessListener {
            // Очищаем поле ввода и скрываем индикатор
            commentInput.text?.clear()
            progressBar.visibility = View.GONE
            commentInput.isEnabled = true
            sendButton.isEnabled = true
            Toast.makeText(this, "Комментарий добавлен", Toast.LENGTH_SHORT).show()
        }.addOnFailureListener { e ->
            // Показываем ошибку и восстанавливаем UI
            progressBar.visibility = View.GONE
            commentInput.isEnabled = true
            sendButton.isEnabled = true
            val errorMessage = when (e) {
                is FirebaseFirestoreException -> when (e.code) {
                    FirebaseFirestoreException.Code.NOT_FOUND -> "Новость не найдена"
                    FirebaseFirestoreException.Code.PERMISSION_DENIED -> "Нет прав для отправки комментария"
                    else -> "Ошибка отправки комментария: ${e.message}"
                }
                else -> "Ошибка отправки комментария: ${e.message}"
            }
            Toast.makeText(this, errorMessage, Toast.LENGTH_SHORT).show()
        }
    }

    private fun showLoading() {
        progressBar.visibility = View.VISIBLE
        commentsList.visibility = View.GONE
        errorText.visibility = View.GONE
        retryButton.visibility = View.GONE
        emptyText.visibility = View.GONE
    }

    private fun showContent() {
        progressBar.visibility = View.GONE
        commentsList.visibility = View.VISIBLE
        errorText.visibility = View.GONE
        retryButton.visibility = View.GONE
        emptyText.visibility = View.GONE
    }

    private fun showError(message: String) {
        progressBar.visibility = View.GONE
        commentsList.visibility = View.GONE
        errorText.visibility = View.VISIBLE
        retryButton.visibility = View.VISIBLE
        emptyText.visibility = View.GONE
        errorText.text = message
    }

    private fun showEmpty() {
        progressBar.visibility = View.GONE
        commentsList.visibility = View.GONE
        errorText.visibility = View.GONE
        retryButton.visibility = View.GONE
        emptyText.visibility = View.VISIBLE
    }
} 