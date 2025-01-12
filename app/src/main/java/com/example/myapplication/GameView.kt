package com.example.myapplication

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View

class GameView(context: Context, attrs: AttributeSet? = null) : View(context, attrs) {
    private val paint = Paint()
    private var cellSize: Int = 0 // Размер клетки будет рассчитываться динамически
    private lateinit var gameBoard: GameBoard

    // Callback-функции для уведомления о событиях игры
    var onGameOver: (() -> Unit)? = null
    var onGameWon: (() -> Unit)? = null
    var onMinesUpdated: ((minesLeft: Int) -> Unit)? = null

    // Загрузка и масштабирование изображений
    private lateinit var cellClosed: Bitmap
    private lateinit var cellOpen: Bitmap
    private lateinit var mine: Bitmap
    private lateinit var flag: Bitmap
    private lateinit var numbers: List<Bitmap>

    init {
        // Загружаем изображения
        loadBitmaps()
    }

    private fun loadBitmaps() {
        // Загружаем изображения и масштабируем их до текущего размера клетки
        cellClosed = BitmapFactory.decodeResource(resources, R.drawable.cell_closed)
        cellOpen = BitmapFactory.decodeResource(resources, R.drawable.cell_open)
        mine = BitmapFactory.decodeResource(resources, R.drawable.mine)
        flag = BitmapFactory.decodeResource(resources, R.drawable.flag)
        numbers = listOf(
            BitmapFactory.decodeResource(resources, R.drawable.number_1),
            BitmapFactory.decodeResource(resources, R.drawable.number_2),
            BitmapFactory.decodeResource(resources, R.drawable.number_3),
            BitmapFactory.decodeResource(resources, R.drawable.number_4)
        )
    }

    private fun scaleBitmaps() {
        // Масштабируем изображения до текущего размера клетки
        cellClosed = Bitmap.createScaledBitmap(cellClosed, cellSize, cellSize, true)
        cellOpen = Bitmap.createScaledBitmap(cellOpen, cellSize, cellSize, true)
        mine = Bitmap.createScaledBitmap(mine, cellSize, cellSize, true)
        flag = Bitmap.createScaledBitmap(flag, cellSize, cellSize, true)
        numbers = numbers.map { Bitmap.createScaledBitmap(it, cellSize, cellSize, true) }
    }

    fun setGameBoard(gameBoard: GameBoard) {
        this.gameBoard = gameBoard
        // Передаем callback-функции в GameBoard
        gameBoard.onGameWon = {
            onGameWon?.invoke()
        }
        gameBoard.onMinesUpdated = { minesLeft ->
            onMinesUpdated?.invoke(minesLeft)
        }
        requestLayout() // Пересчитываем размеры
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)

        // Получаем доступные размеры экрана
        val screenWidth = MeasureSpec.getSize(widthMeasureSpec)
        val screenHeight = MeasureSpec.getSize(heightMeasureSpec)

        // Рассчитываем размер клетки так, чтобы все поле влезло на экран
        val maxCellWidth = screenWidth / gameBoard.size
        val maxCellHeight = screenHeight / gameBoard.size
        cellSize = minOf(maxCellWidth, maxCellHeight)

        // Масштабируем изображения под новый размер клетки
        scaleBitmaps()

        // Устанавливаем размеры GameView
        val width = cellSize * gameBoard.size
        val height = cellSize * gameBoard.size
        setMeasuredDimension(width, height)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        for (i in 0 until gameBoard.size) {
            for (j in 0 until gameBoard.size) {
                val cell = gameBoard.board[i][j]
                drawCell(canvas, i, j, cell)
            }
        }
    }

    private fun drawCell(canvas: Canvas, x: Int, y: Int, cell: Cell) {
        val left = x * cellSize
        val top = y * cellSize

        if (cell.isRevealed) {
            canvas.drawBitmap(cellOpen, left.toFloat(), top.toFloat(), paint)
            if (cell.isMine) {
                canvas.drawBitmap(mine, left.toFloat(), top.toFloat(), paint)
            } else if (cell.minesAround > 0) {
                val numberBitmap = numbers[cell.minesAround - 1]
                canvas.drawBitmap(numberBitmap, left.toFloat(), top.toFloat(), paint)
            }
        } else {
            canvas.drawBitmap(cellClosed, left.toFloat(), top.toFloat(), paint)
            if (cell.isFlagged) {
                canvas.drawBitmap(flag, left.toFloat(), top.toFloat(), paint)
            }
        }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (gameBoard.isGameOver() || gameBoard.isGameWon()) {
            return true // Блокируем взаимодействие, если игра завершена
        }

        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                val x = (event.x / cellSize).toInt()
                val y = (event.y / cellSize).toInt()

                if (x in 0 until gameBoard.size && y in 0 until gameBoard.size) {
                    val cell = gameBoard.board[x][y]
                    if (!cell.isRevealed) {
                        gameBoard.toggleFlag(x, y)
                        invalidate()
                    }
                }
            }
            MotionEvent.ACTION_UP -> {
                val x = (event.x / cellSize).toInt()
                val y = (event.y / cellSize).toInt()

                if (x in 0 until gameBoard.size && y in 0 until gameBoard.size) {
                    if (gameBoard.revealCell(x, y)) {
                        // Игра окончена (нажата мина)
                        onGameOver?.invoke()
                        invalidate()
                    } else {
                        invalidate()
                    }
                }
            }
        }
        return true
    }
}