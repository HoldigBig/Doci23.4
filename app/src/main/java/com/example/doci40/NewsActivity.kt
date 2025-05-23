package com.example.doci40

import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.view.View
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
import com.example.doci40.news.adapter.NewsAdapter
import com.example.doci40.news.model.NewsItem
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query


class NewsActivity : AppCompatActivity() {

    private lateinit var backButton: ImageButton
    private lateinit var newsList: RecyclerView
    private lateinit var progressBar: ProgressBar
    private lateinit var errorText: TextView
    private lateinit var retryButton: ImageButton
    private lateinit var emptyText: TextView

    private lateinit var newsAdapter: NewsAdapter
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        setContentView(R.layout.activity_news)

        // Устанавливаем цвет иконок статус бара на черный
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
            window.statusBarColor = Color.TRANSPARENT // Опционально, если нужен прозрачный статус бар
        }

        initializeViews()
        setupRecyclerView()
        setupListeners()
        loadNews()
    }

    private fun initializeViews() {
        backButton = findViewById(R.id.backButton)
        newsList = findViewById(R.id.newsList)
        progressBar = findViewById(R.id.progressBar)
        errorText = findViewById(R.id.errorText)
        retryButton = findViewById(R.id.retryButton)
        emptyText = findViewById(R.id.emptyText)
    }

    private fun setupRecyclerView() {
        newsAdapter = NewsAdapter { newsItem ->
            shareNews(newsItem)
        }
        newsList.apply {
            layoutManager = LinearLayoutManager(this@NewsActivity)
            adapter = newsAdapter
        }
    }

    private fun setupListeners() {
        backButton.setOnClickListener { onBackPressed() }
        retryButton.setOnClickListener { loadNews() }
    }

    private fun loadNews() {
        showLoading()
        
        val currentUser = auth.currentUser
        if (currentUser == null) {
            showError("Необходимо войти в аккаунт")
            return
        }

        db.collection("news")
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    showError("Ошибка загрузки новостей")
                    return@addSnapshotListener
                }

                val news = mutableListOf<NewsItem>()
                val newsItems = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(NewsItem::class.java)?.apply {
                        id = doc.id // Устанавливаем ID документа
                    }
                } ?: emptyList()

                if (newsItems.isEmpty()) {
                    updateNewsList(emptyList())
                    return@addSnapshotListener
                }

                var processedCount = 0
                newsItems.forEach { newsItem ->
                    db.collection("news")
                        .document(newsItem.id)
                        .collection("likes")
                        .document(currentUser.uid)
                        .get()
                        .addOnSuccessListener { likeDoc ->
                            newsItem.isLiked = likeDoc.exists()
                            news.add(newsItem)
                            processedCount++
                            
                            if (processedCount == newsItems.size) {
                                updateNewsList(news.sortedByDescending { it.timestamp })
                            }
                        }
                        .addOnFailureListener {
                            processedCount++
                            if (processedCount == newsItems.size) {
                                updateNewsList(news.sortedByDescending { it.timestamp })
                            }
                        }
                }
            }
    }

    private fun updateNewsList(news: List<NewsItem>) {
        if (news.isEmpty()) {
            showEmpty()
        } else {
            showContent()
            newsAdapter.submitList(news)
        }
    }

    private fun shareNews(newsItem: NewsItem) {
        val shareIntent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, "${newsItem.name}\n\n${newsItem.desc}")
            type = "text/plain"
        }
        startActivity(Intent.createChooser(shareIntent, "Поделиться новостью"))
    }

    private fun showLoading() {
        progressBar.visibility = View.VISIBLE
        newsList.visibility = View.GONE
        errorText.visibility = View.GONE
        retryButton.visibility = View.GONE
        emptyText.visibility = View.GONE
    }

    private fun showContent() {
        progressBar.visibility = View.GONE
        newsList.visibility = View.VISIBLE
        errorText.visibility = View.GONE
        retryButton.visibility = View.GONE
        emptyText.visibility = View.GONE
    }

    private fun showError(message: String) {
        progressBar.visibility = View.GONE
        newsList.visibility = View.GONE
        errorText.visibility = View.VISIBLE
        retryButton.visibility = View.VISIBLE
        emptyText.visibility = View.GONE
        errorText.text = message
    }

    private fun showEmpty() {
        progressBar.visibility = View.GONE
        newsList.visibility = View.GONE
        errorText.visibility = View.GONE
        retryButton.visibility = View.GONE
        emptyText.visibility = View.VISIBLE
    }
}