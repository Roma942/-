package com.example.myapplication

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import android.widget.Button

class LeaderboardActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: LeaderboardAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_leaderboard)

        // Инициализация RecyclerView
        recyclerView = findViewById(R.id.leaderboardRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)

        // Получение данных из базы данных
        val leaderboardData = getLeaderboardData()

        // Установка адаптера
        adapter = LeaderboardAdapter(leaderboardData)
        recyclerView.adapter = adapter

        // Инициализация кнопки "Назад"
        val backButton: Button = findViewById(R.id.backButton)
        backButton.setOnClickListener {
            Log.d("LeaderboardActivity", "Кнопка 'Назад' нажата")
            finish() // Завершаем текущую активность
        }
    }

    // Метод для получения данных из базы данных
    private fun getLeaderboardData(): List<LeaderboardEntry> {
        val dbHelper = LeaderboardDbHelper(this)
        return dbHelper.getLeaderboardEntries()
    }
}