package com.example.myapplication

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import android.widget.Button
import android.os.Handler
import android.view.View
import android.widget.TextView
import androidx.core.content.ContextCompat

class MainActivity : AppCompatActivity() {

    private lateinit var gameView: GameView
    private lateinit var restartButton: Button
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

        // Инициализация GameView, кнопки и TextView
        gameView = findViewById(R.id.gameView)
        restartButton = findViewById(R.id.restartButton)
        timerTextView = findViewById(R.id.timerTextView)
        minesTextView = findViewById(R.id.minesTextView)
        resultTextView = findViewById(R.id.resultTextView)

        // Начальная настройка игры
        restartGame()

        // Обработчик нажатия на кнопку "Рестарт"
        restartButton.setOnClickListener {
            restartGame()
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
            resultTextView.text = "Победа"
            resultTextView.setTextColor(ContextCompat.getColor(this, android.R.color.holo_green_dark))
            resultTextView.visibility = View.VISIBLE // Показываем сообщение
        }
        gameView.onMinesUpdated = { minesLeft ->
            // Обновляем TextView с количеством мин
            minesTextView.text = "Мины: $minesLeft"
        }
    }

    private fun restartGame() {
        // Останавливаем таймер и скрываем сообщение
        stopTimer()
        resultTextView.visibility = View.GONE

        // Создаем новое игровое поле и передаем его в GameView
        val gameBoard = GameBoard(10) // 10x10 поле, количество мин рассчитается автоматически
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