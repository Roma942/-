package com.example.myapplication

import java.util.Random

class GameBoard(val size: Int) {
    val board: Array<Array<Cell>> = Array(size) { Array(size) { Cell() } }
    private var gameOver: Boolean = false
    private var gameWon: Boolean = false
    private var isFirstMove: Boolean = true

    // Количество мин (15% от общего числа клеток)
    val totalMines: Int = (size * size * 0.15).toInt()
    var minesLeft: Int = totalMines
        private set

    // Callback для обновления количества мин
    var onMinesUpdated: ((minesLeft: Int) -> Unit)? = null

    // Callback для уведомления о победе
    var onGameWon: (() -> Unit)? = null

    init {
        placeMines()
        calculateMinesAround()
    }

    private fun placeMines() {
        var minesPlaced = 0
        val random = Random()

        while (minesPlaced < totalMines) {
            val x = random.nextInt(size)
            val y = random.nextInt(size)

            if (!board[x][y].isMine) {
                board[x][y].isMine = true
                minesPlaced++
            }
        }
    }

    private fun calculateMinesAround() {
        for (i in 0 until size) {
            for (j in 0 until size) {
                if (!board[i][j].isMine) {
                    var minesAround = 0
                    for (x in -1..1) {
                        for (y in -1..1) {
                            if (i + x in 0 until size && j + y in 0 until size && board[i + x][j + y].isMine) {
                                minesAround++
                            }
                        }
                    }
                    board[i][j].minesAround = minesAround
                }
            }
        }
    }

    fun revealCell(x: Int, y: Int): Boolean {
        if (gameOver || gameWon || board[x][y].isFlagged) return false

        if (board[x][y].isMine) {
            gameOver = true
            revealAllMines()
            return true
        }

        if (!board[x][y].isRevealed) {
            board[x][y].isRevealed = true

            // Рекурсивное открытие пустых клеток
            if (board[x][y].minesAround == 0) {
                for (i in -1..1) {
                    for (j in -1..1) {
                        if (x + i in 0 until size && y + j in 0 until size) {
                            revealCell(x + i, y + j)
                        }
                    }
                }
            }

            checkWin() // Проверяем победу после открытия клетки
        }

        return false
    }

    fun toggleFlag(x: Int, y: Int) {
        if (!gameOver && !gameWon && !board[x][y].isRevealed) {
            board[x][y].isFlagged = !board[x][y].isFlagged
            minesLeft += if (board[x][y].isFlagged) -1 else 1 // Обновляем количество мин
            onMinesUpdated?.invoke(minesLeft) // Уведомляем об изменении количества мин
        }
    }

    private fun moveMine(x: Int, y: Int) {
        val random = Random()
        var newX: Int
        var newY: Int

        // Ищем новое место для мины
        do {
            newX = random.nextInt(size)
            newY = random.nextInt(size)
        } while (board[newX][newY].isMine || (newX == x && newY == y))

        // Перемещаем мину
        board[x][y].isMine = false
        board[newX][newY].isMine = true
    }

    private fun revealAllMines() {
        for (i in 0 until size) {
            for (j in 0 until size) {
                if (board[i][j].isMine) {
                    board[i][j].isRevealed = true // Открываем все мины
                }
            }
        }
    }

    private fun checkWin() {
        var allSafeCellsRevealed = true

        for (i in 0 until size) {
            for (j in 0 until size) {
                // Проверяем только клетки, которые не являются минами
                if (!board[i][j].isMine && !board[i][j].isRevealed) {
                    allSafeCellsRevealed = false
                    break
                }
            }
        }

        if (allSafeCellsRevealed) {
            gameWon = true
            onGameWon?.invoke() // Уведомляем о победе
        }
    }

    fun isGameOver(): Boolean = gameOver
    fun isGameWon(): Boolean = gameWon
}