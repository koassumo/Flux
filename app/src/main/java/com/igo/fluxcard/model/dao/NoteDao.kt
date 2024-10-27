package com.igo.fluxcard.model.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.igo.fluxcard.model.entity.Note

@Dao
interface NoteDao {

    // Добавить запись в таблицу, стратегия конфликта - перезапись
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(note: Note)

    // Получить все записи из таблицы
    @Query("SELECT * FROM Note")
    suspend fun getAllNotes(): List<Note>

    // Получить одну запись по ID
    @Query("SELECT * FROM Note WHERE id = :id")
    suspend fun getNoteById(id: Int): Note?

    // Обновить запись в таблице
    @Update
    suspend fun update(note: Note)
}
