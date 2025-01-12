package com.example.myapplication

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import android.widget.EditText

class MainActivity : AppCompatActivity() {

    private lateinit var gameView: GameView
    private lateinit var restartButton: Button
    private lateinit var leaderboardButton: Button
    private lateinit var timerTextView: TextView
    private lateinit var minesTextView: TextView
    private lateinit var resultTextView: TextView

    private var handler: Handler = Handler()
    private var runnable: Runnable? = null
    private var startTime: Long = 0
    private var elapsedTime: Long = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Инициализация GameView, кнопок и TextView
        gameView = findViewById(R.id.gameView)
        restartButton = findViewById(R.id.restartButton)
        leaderboardButton = findViewById(R.id.leaderboardButton)
        timerTextView = findViewById(R.id.timerTextView)
        minesTextView = findViewById(R.id.minesTextView)
        resultTextView = findViewById(R.id.resultTextView)

        // Начальная настройка игры
        restartGame()

        // Обработчик нажатия на кнопку "Рестарт"
        restartButton.setOnClickListener {
            restartGame()
        }

        // Обработчик нажатия на кнопку "Таблица лидеров"
        leaderboardButton.setOnClickListener {
            val intent = Intent(this, LeaderboardActivity::class.java)
            startActivity(intent)
        }

        // Обработчики событий игры
        gameView.onGameOver = {
            stopTimer() // Останавливаем таймер
            resultTextView.text = "Поражение"
            resultTextView.setTextColor(ContextCompat.getColor(this, android.R.color.holo_red_dark))
            resultTextView.visibility = View.VISIBLE // Показываем сообщение
        }
        gameView.onGameWon = {
            stopTimer() // Останавливаем таймер
            resultTextView.text = "Победа!"
            resultTextView.setTextColor(ContextCompat.getColor(this, android.R.color.holo_green_dark))
            resultTextView.visibility = View.VISIBLE // Показываем сообщение

            // Показываем диалог для ввода имени
            showNameInputDialog()
        }
        gameView.onMinesUpdated = { minesLeft ->
            // Обновляем TextView с количеством мин
            minesTextView.text = "Мины: $minesLeft"
        }
    }

    private fun showNameInputDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Победа!")
        builder.setMessage("Введите ваше имя:")

        val input = EditText(this)
        builder.setView(input)

        builder.setPositiveButton("Сохранить") { dialog, _ ->
            val playerName = input.text.toString()
            if (playerName.isNotEmpty()) {
                saveLeaderboardEntry(playerName, elapsedTime.toInt())
            }
            dialog.dismiss()
        }

        builder.setNegativeButton("Отмена") { dialog, _ ->
            dialog.cancel()
        }

        builder.show()
    }

    private fun saveLeaderboardEntry(playerName: String, score: Int) {
        val dbHelper = LeaderboardDbHelper(this)
        dbHelper.addEntry(playerName, score)
    }

    private fun restartGame() {
        // Останавливаем таймер и скрываем сообщение
        stopTimer()
        resultTextView.visibility = View.GONE

        // Создаем новое игровое поле и передаем его в GameView
        val gameBoard = GameBoard(5) // 10x10 поле, количество мин рассчитается автоматически
        gameView.setGameBoard(gameBoard)
        gameView.invalidate() // Перерисовываем GameView

        // Запускаем таймер
        startTimer()

        // Обновляем количество мин
        minesTextView.text = "Мины: ${gameBoard.totalMines}"
    }

    private fun startTimer() {
        startTime = System.currentTimeMillis()
        runnable = object : Runnable {
            override fun run() {
                elapsedTime = System.currentTimeMillis() - startTime
                val seconds = (elapsedTime / 1000).toInt()
                timerTextView.text = "Время: $seconds сек"
                handler.postDelayed(this, 1000) // Обновляем каждую секунду
            }
        }
        handler.post(runnable!!)
    }

    private fun stopTimer() {
        handler.removeCallbacks(runnable ?: return)
        runnable = null
    }

    override fun onDestroy() {
        super.onDestroy()
        stopTimer() // Останавливаем таймер при уничтожении активности
    }
}