package com.example.doci40

import android.content.Context
import android.graphics.Color
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.doci40.notifivation.adapters.NotificationsAdapter
import com.example.doci40.notifivation.model.NotificationModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query

class NotificationsActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var emptyView: LinearLayout
    private lateinit var backButton: ImageButton
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    
    private var resultsListener: ListenerRegistration? = null
    private var menuListener: ListenerRegistration? = null
    private var homeworkListener: ListenerRegistration? = null
    private var examsListener: ListenerRegistration? = null
    private var notificationsListener: ListenerRegistration? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        WindowCompat.setDecorFitsSystemWindows(window, false)
        setContentView(R.layout.activity_notifications)

        // Устанавливаем цвет иконок статус бара на черный
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
            window.statusBarColor = Color.TRANSPARENT
        }

        ViewCompat.setOnApplyWindowInsetsListener(window.decorView.findViewById(android.R.id.content)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        try {
            auth = FirebaseAuth.getInstance()
            db = FirebaseFirestore.getInstance()

            if (auth.currentUser == null) {
                Toast.makeText(this, "Необходимо войти в аккаунт", Toast.LENGTH_LONG).show()
                finish()
                return
            }

            initViews()
            setupBackButton()
            checkAndLoadData()
        } catch (e: Exception) {
            Toast.makeText(this, "Ошибка инициализации: ${e.message}", Toast.LENGTH_LONG).show()
            finish()
        }
    }

    private fun checkAndLoadData() {
        if (!isNetworkAvailable()) {
            Toast.makeText(this, "Отсутствует подключение к интернету", Toast.LENGTH_LONG).show()
            showEmptyState()
            return
        }

        // Проверяем подключение к Firebase
        db.collection("test").document("test")
            .get()
            .addOnSuccessListener {
                // Firebase доступен, загружаем данные
                loadInitialData()
                setupNotificationsListener()
                setupAllListeners()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Ошибка подключения к серверу: ${e.message}", Toast.LENGTH_LONG).show()
                showEmptyState()
            }
    }

    private fun isNetworkAvailable(): Boolean {
        val connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val network = connectivityManager.activeNetwork ?: return false
            val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
            return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
        } else {
            val networkInfo = connectivityManager.activeNetworkInfo
            return networkInfo != null && networkInfo.isConnected
        }
    }

    private fun setupAllListeners() {
        setupResultsListener()
        setupMenuListener()
        setupHomeworkListener()
        setupExamsListener()
    }

    private fun initViews() {
        recyclerView = findViewById(R.id.notificationsRecyclerView)
        emptyView = findViewById(R.id.emptyNotificationsView)
        backButton = findViewById(R.id.backButton)

        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = NotificationsAdapter(
            notifications = mutableListOf(),
            onDeleteClick = { notification -> deleteNotification(notification) },
            onMarkAsReadClick = { notification -> markNotificationAsRead(notification) }
        )
    }

    private fun setupBackButton() {
        backButton.setOnClickListener {
            finish()
        }
    }

    private fun setupNotificationsListener() {
        val currentUser = auth.currentUser ?: return

        notificationsListener = db.collection("users")
            .document(currentUser.uid)
            .collection("notifications")
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshots, error ->
                if (error != null) {
                    showEmptyState()
                    return@addSnapshotListener
                }

                if (snapshots != null) {
                    val notifications = mutableListOf<NotificationModel>()
                    for (document in snapshots.documents) {
                        val notification = document.toObject(NotificationModel::class.java)
                        if (notification != null) {
                            notifications.add(notification)
                        }
                    }

                    if (notifications.isEmpty()) {
                        showEmptyState()
                    } else {
                        recyclerView.visibility = View.VISIBLE
                        emptyView.visibility = View.GONE
                        (recyclerView.adapter as NotificationsAdapter).updateNotifications(notifications)
                    }
                    
                    // Скрываем индикатор загрузки после получения данных
                    hideLoadingState()
                } else {
                    showEmptyState()
                }
            }
    }

    private fun showEmptyState() {
        recyclerView.visibility = View.GONE
        emptyView.visibility = View.VISIBLE
    }

    private fun deleteNotification(notification: NotificationModel) {
        val currentUser = auth.currentUser ?: return
        
        db.collection("users")
            .document(currentUser.uid)
            .collection("notifications")
            .document(notification.id)
            .delete()
            .addOnSuccessListener {
                Toast.makeText(this, "Уведомление удалено", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Ошибка при удалении уведомления", Toast.LENGTH_SHORT).show()
            }
    }

    private fun markNotificationAsRead(notification: NotificationModel) {
        val currentUser = auth.currentUser ?: return
        
        db.collection("users")
            .document(currentUser.uid)
            .collection("notifications")
            .document(notification.id)
            .update("isRead", true)
            .addOnSuccessListener {
                Toast.makeText(this, "Уведомление отмечено как прочитанное", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Ошибка при обновлении уведомления", Toast.LENGTH_SHORT).show()
            }
    }

    private fun createNotification(title: String, message: String, type: String) {
        val currentUser = auth.currentUser ?: return
        
        val docRef = db.collection("users")
            .document(currentUser.uid)
            .collection("notifications")
            .document()
            
        val notification = NotificationModel(
            id = docRef.id,
            title = title,
            message = message,
            type = type,
            timestamp = System.currentTimeMillis(),
            isRead = false
        )

        docRef.set(notification)
            .addOnFailureListener {
                Toast.makeText(this, "Ошибка при создании уведомления", Toast.LENGTH_SHORT).show()
            }
    }

    private fun setupResultsListener() {
        val currentUser = auth.currentUser ?: return

        resultsListener = db.collection("users")
            .document(currentUser.uid)
            .collection("results")
            .addSnapshotListener { snapshots, error ->
                if (error != null) return@addSnapshotListener

                snapshots?.documentChanges?.forEach { change ->
                    val result = change.document.data
                    val subject = result["subject"] as? String ?: return@forEach
                    val score = result["score"] as? Number ?: return@forEach
                    
                    when (change.type) {
                        com.google.firebase.firestore.DocumentChange.Type.ADDED, 
                        com.google.firebase.firestore.DocumentChange.Type.MODIFIED -> {
                            createNotification(
                                "📊 Новая оценка",
                                "Выставлена оценка\nПредмет: $subject\nОценка: ${score.toInt()}",
                                "result_updated"
                            )
                        }
                        else -> { }
                    }
                }
            }
    }

    private fun setupMenuListener() {
        menuListener = db.collection("menu")
            .addSnapshotListener { snapshots, error ->
                if (error != null) return@addSnapshotListener

                snapshots?.documentChanges?.forEach { change ->
                    val menu = change.document.data
                    val date = menu["date"] as? String ?: return@forEach
                    
                    when (change.type) {
                        com.google.firebase.firestore.DocumentChange.Type.ADDED -> {
                            createNotification(
                                "🍽️ Меню питания",
                                "Опубликовано меню на $date",
                                "menu_added"
                            )
                        }
                        com.google.firebase.firestore.DocumentChange.Type.MODIFIED -> {
                            createNotification(
                                "🔄 Обновление меню",
                                "Обновлено меню на $date",
                                "menu_updated"
                            )
                        }
                        else -> { }
                    }
                }
            }
    }

    private fun setupHomeworkListener() {
        val currentUser = auth.currentUser ?: return

        homeworkListener = db.collection("users")
            .document(currentUser.uid)
            .collection("homework")
            .addSnapshotListener { snapshots, error ->
                if (error != null) return@addSnapshotListener

                snapshots?.documentChanges?.forEach { change ->
                    val homework = change.document.toObject(com.example.doci40.homework.models.HomeworkModel::class.java)
                    
                    when (change.type) {
                        com.google.firebase.firestore.DocumentChange.Type.ADDED -> {
                            createNotification(
                                "📚 Домашнее задание",
                                "Добавлено новое задание\nПредмет: ${homework.subject}\nЗадание: ${homework.title}\nСдать до: ${homework.dueDate}",
                                "homework_added"
                            )
                        }
                        com.google.firebase.firestore.DocumentChange.Type.MODIFIED -> {
                            createNotification(
                                "📝 Изменение задания",
                                "Обновлено задание\nПредмет: ${homework.subject}\nЗадание: ${homework.title}\nСдать до: ${homework.dueDate}",
                                "homework_updated"
                            )
                        }
                        com.google.firebase.firestore.DocumentChange.Type.REMOVED -> {
                            createNotification(
                                "🗑️ Удаление задания",
                                "Удалено задание\nПредмет: ${homework.subject}\nЗадание: ${homework.title}",
                                "homework_deleted"
                            )
                        }
                    }
                }
            }
    }

    private fun setupExamsListener() {
        val currentUser = auth.currentUser ?: return

        examsListener = db.collection("users")
            .document(currentUser.uid)
            .collection("exams")
            .addSnapshotListener { snapshots, error ->
                if (error != null) return@addSnapshotListener

                snapshots?.documentChanges?.forEach { change ->
                    val exam = change.document.toObject(com.example.doci40.exams.models.ExamModel::class.java)
                    
                    when (change.type) {
                        com.google.firebase.firestore.DocumentChange.Type.ADDED -> {
                            createNotification(
                                "📅 Новый экзамен",
                                "Назначен экзамен\nПредмет: ${exam.subject}\nДата: ${exam.formattedDate}\nВремя: ${exam.time}\nАудитория: ${exam.location}",
                                "exam_added"
                            )
                        }
                        com.google.firebase.firestore.DocumentChange.Type.MODIFIED -> {
                            createNotification(
                                "🔄 Изменение экзамена",
                                "Изменена информация\nПредмет: ${exam.subject}\nДата: ${exam.formattedDate}\nВремя: ${exam.time}\nАудитория: ${exam.location}",
                                "exam_updated"
                            )
                        }
                        com.google.firebase.firestore.DocumentChange.Type.REMOVED -> {
                            createNotification(
                                "❌ Отмена экзамена",
                                "Отменен экзамен\nПредмет: ${exam.subject}\nДата: ${exam.formattedDate}",
                                "exam_deleted"
                            )
                        }
                    }
                }
            }
    }

    private fun loadInitialData() {
        val currentUser = auth.currentUser ?: return
        showLoadingState()

        var completedRequests = 0
        val totalRequests = 4 // экзамены, домашние задания, оценки, меню

        // Загружаем экзамены на ближайшие 7 дней
        db.collection("users")
            .document(currentUser.uid)
            .collection("exams")
            .get()
            .addOnSuccessListener { snapshots ->
                for (doc in snapshots.documents) {
                    val exam = doc.toObject(com.example.doci40.exams.models.ExamModel::class.java)
                    if (exam != null) {
                        // Преобразуем дату экзамена в timestamp
                        val dateFormat = java.text.SimpleDateFormat("dd.MM.yyyy", java.util.Locale.getDefault())
                        try {
                            val examDate = dateFormat.parse(exam.formattedDate)?.time ?: continue
                            val sevenDaysFromNow = System.currentTimeMillis() + (7 * 24 * 60 * 60 * 1000)
                            
                            if (examDate > System.currentTimeMillis() && examDate < sevenDaysFromNow) {
                                val daysUntil = getDaysUntil(examDate)
                                createNotification(
                                    "📅 Предстоящий экзамен",
                                    "Экзамен через $daysUntil дней\nПредмет: ${exam.subject}\nДата: ${exam.formattedDate}\nВремя: ${exam.time}\nАудитория: ${exam.location}",
                                    "exam_upcoming"
                                )
                            }
                        } catch (e: Exception) {
                            continue
                        }
                    }
                }
                completedRequests++
                checkLoadingComplete(completedRequests, totalRequests)
            }
            .addOnFailureListener {
                completedRequests++
                checkLoadingComplete(completedRequests, totalRequests)
            }

        // Загружаем домашние задания
        db.collection("users")
            .document(currentUser.uid)
            .collection("homework")
            .get()
            .addOnSuccessListener { snapshots ->
                for (doc in snapshots.documents) {
                    val homework = doc.toObject(com.example.doci40.homework.models.HomeworkModel::class.java)
                    if (homework != null) {
                        val dateFormat = java.text.SimpleDateFormat("dd.MM.yyyy", java.util.Locale.getDefault())
                        try {
                            val dueDate = dateFormat.parse(homework.dueDate)?.time ?: continue
                            if (dueDate > System.currentTimeMillis()) {
                                val daysUntil = getDaysUntil(dueDate)
                                createNotification(
                                    "📚 Домашнее задание",
                                    "Срок сдачи через $daysUntil дней\nПредмет: ${homework.subject}\nЗадание: ${homework.title}\nСдать до: ${homework.dueDate}",
                                    "homework_active"
                                )
                            }
                        } catch (e: Exception) {
                            continue
                        }
                    }
                }
                completedRequests++
                checkLoadingComplete(completedRequests, totalRequests)
            }
            .addOnFailureListener {
                completedRequests++
                checkLoadingComplete(completedRequests, totalRequests)
            }

        // Загружаем результаты
        db.collection("users")
            .document(currentUser.uid)
            .collection("results")
            .whereGreaterThan("timestamp", System.currentTimeMillis() - (24 * 60 * 60 * 1000))
            .get()
            .addOnSuccessListener { snapshots ->
                for (doc in snapshots.documents) {
                    val result = doc.data
                    val subject = result?.get("subject") as? String ?: continue
                    val score = result["score"] as? Number ?: continue
                    
                    createNotification(
                        "📊 Новая оценка",
                        "Выставлена оценка\nПредмет: $subject\nОценка: ${score.toInt()}",
                        "result_new"
                    )
                }
                completedRequests++
                checkLoadingComplete(completedRequests, totalRequests)
            }
            .addOnFailureListener {
                completedRequests++
                checkLoadingComplete(completedRequests, totalRequests)
            }

        // Загружаем меню
        val today = java.time.LocalDate.now().toString()
        db.collection("menu")
            .whereEqualTo("date", today)
            .get()
            .addOnSuccessListener { snapshots ->
                for (doc in snapshots.documents) {
                    val menu = doc.data
                    val date = menu?.get("date") as? String ?: continue
                    
                    createNotification(
                        "🍽️ Меню на сегодня",
                        "Меню питания на $date",
                        "menu_today"
                    )
                }
                completedRequests++
                checkLoadingComplete(completedRequests, totalRequests)
            }
            .addOnFailureListener {
                completedRequests++
                checkLoadingComplete(completedRequests, totalRequests)
            }
    }

    private fun getDaysUntil(timestamp: Long): Int {
        val diff = timestamp - System.currentTimeMillis()
        return (diff / (24 * 60 * 60 * 1000)).toInt()
    }

    private fun showLoadingState() {
        findViewById<View>(R.id.loadingView)?.visibility = View.VISIBLE
        recyclerView.visibility = View.GONE
        emptyView.visibility = View.GONE
    }

    private fun hideLoadingState() {
        findViewById<View>(R.id.loadingView)?.visibility = View.GONE
    }

    private fun checkLoadingComplete(completed: Int, total: Int) {
        if (completed >= total) {
            hideLoadingState()
            // Проверяем, есть ли уведомления
            val adapter = recyclerView.adapter as NotificationsAdapter
            if (adapter.itemCount == 0) {
                recyclerView.visibility = View.GONE
                emptyView.visibility = View.VISIBLE
            } else {
                recyclerView.visibility = View.VISIBLE
                emptyView.visibility = View.GONE
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        resultsListener?.remove()
        menuListener?.remove()
        homeworkListener?.remove()
        examsListener?.remove()
        notificationsListener?.remove()
    }
}