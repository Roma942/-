package com.example.myapplication

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class LeaderboardDbHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "leaderboard.db"
        private const val DATABASE_VERSION = 1
        private const val TABLE_NAME = "leaderboard"
        private const val COLUMN_ID = "id"
        private const val COLUMN_PLAYER_NAME = "player_name"
        private const val COLUMN_SCORE = "score"
    }

    override fun onCreate(db: SQLiteDatabase) {
        val createTable = ("CREATE TABLE $TABLE_NAME ("
                + "$COLUMN_ID INTEGER PRIMARY KEY AUTOINCREMENT,"
                + "$COLUMN_PLAYER_NAME TEXT,"
                + "$COLUMN_SCORE INTEGER)")
        db.execSQL(createTable)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABLE_NAME")
        onCreate(db)
    }

    fun addEntry(playerName: String, score: Int) {
        val db = this.writableDatabase
        val values = ContentValues()
        values.put(COLUMN_PLAYER_NAME, playerName)
        values.put(COLUMN_SCORE, score)
        db.insert(TABLE_NAME, null, values)
        db.close()
    }

    fun getLeaderboardEntries(): List<LeaderboardEntry> {
        val entries = mutableListOf<LeaderboardEntry>()
        val db = this.readableDatabase
        val cursor: Cursor = db.rawQuery("SELECT * FROM $TABLE_NAME ORDER BY $COLUMN_SCORE ASC", null)

        if (cursor.moveToFirst()) {
            do {
                // Получаем индексы столбцов
                val playerNameIndex = cursor.getColumnIndex(COLUMN_PLAYER_NAME)
                val scoreIndex = cursor.getColumnIndex(COLUMN_SCORE)

                // Проверяем, что индексы столбцов найдены
                if (playerNameIndex >= 0 && scoreIndex >= 0) {
                    val playerName = cursor.getString(playerNameIndex)
                    val score = cursor.getInt(scoreIndex)
                    entries.add(LeaderboardEntry(playerName, score))
                }
            } while (cursor.moveToNext())
        }
        cursor.close()
        db.close()
        return entries
    }
}