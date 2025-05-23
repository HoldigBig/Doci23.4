package com.example.doci40

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.viewpager2.widget.ViewPager2
import com.example.doci40.exams.adapters.ExamsPagerAdapter
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator

class ExamsActivity : AppCompatActivity() {

    private lateinit var viewPager: ViewPager2
    private lateinit var tabLayout: TabLayout
    private lateinit var backButton: ImageButton
    private lateinit var toolbar: Toolbar
    private lateinit var addExamButton: FloatingActionButton
    private var currentPage: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setupWindowDecorations()
        setContentView(R.layout.activity_exams)
        
        // Восстанавливаем состояние, если оно есть
        savedInstanceState?.let {
            currentPage = it.getInt(KEY_CURRENT_PAGE, 0)
        }

        initViews()
        setupToolbar()
        setupViewPager()
        setupBackButton()
        setupAddExamButton()
        setupWindowInsets()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putInt(KEY_CURRENT_PAGE, viewPager.currentItem)
    }

    private fun setupWindowDecorations() {
        // Настраиваем edge-to-edge отображение
        WindowCompat.setDecorFitsSystemWindows(window, false)
        window.statusBarColor = Color.TRANSPARENT
        window.navigationBarColor = Color.TRANSPARENT

        // Устанавливаем светлые иконки для тёмного фона статус бара
        WindowCompat.getInsetsController(window, window.decorView).apply {
            isAppearanceLightStatusBars = true
            isAppearanceLightNavigationBars = true
        }
    }

    private fun setupWindowInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { view, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    private fun initViews() {
        viewPager = findViewById(R.id.viewPager)
        tabLayout = findViewById(R.id.tabLayout)
        backButton = findViewById(R.id.backButton)
        toolbar = findViewById(R.id.toolbar)
        addExamButton = findViewById(R.id.addExamButton)
    }

    private fun setupToolbar() {
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)
    }

    private fun setupViewPager() {
        val adapter = ExamsPagerAdapter(this)
        viewPager.apply {
            this.adapter = adapter
            // Устанавливаем лимит страниц для предзагрузки
            offscreenPageLimit = 1
            // Восстанавливаем позицию после поворота экрана
            setCurrentItem(currentPage, false)
        }

        TabLayoutMediator(tabLayout, viewPager) { tab, position ->
            tab.text = getString(R.string.semester_number, position + 1)
        }.attach()

        // Сохраняем текущую страницу при переключении
        viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                currentPage = position
            }
        })
    }

    private fun setupBackButton() {
        backButton.setOnClickListener {
            // Используем finish() вместо onBackPressed(), так как onBackPressed() устарел
            finish()
        }
    }

    private fun setupAddExamButton() {
        addExamButton.setOnClickListener {
            startActivity(Intent(this, AddExamActivity::class.java).apply {
                putExtra(AddExamActivity.EXTRA_SEMESTER, viewPager.currentItem + 1)
            })
        }
    }

    companion object {
        private const val KEY_CURRENT_PAGE = "current_page"
    }
}