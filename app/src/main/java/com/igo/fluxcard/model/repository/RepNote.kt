package com.igo.fluxcard.model.repository

import android.util.Log
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import com.google.gson.Gson
import com.igo.fluxcard.model.dao.NoteDao
import com.igo.fluxcard.model.entity.CardSet
import com.igo.fluxcard.model.entity.Note
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.nio.charset.Charset

class RepNote(private val noteDao: NoteDao) {

    suspend fun getAllNotes(): List<Note> {
        return withContext(Dispatchers.IO) {
            val notes = noteDao.getAllNotes()
            Log.d("RepNote", "Все заметки: ${notes.size}")
            notes
        }
    }

    suspend fun getNoteById(id: Int): Note? {
        return withContext(Dispatchers.IO) {
            val note = noteDao.getNoteById(id)
            Log.d("RepNote", "Запрос заметки с ID: $id, результат: ${note?.origin}, ${note?.translate}")
            note
        }
    }

    suspend fun insertNote(note: Note) {
        withContext(Dispatchers.IO) {
            noteDao.insert(note)
            Log.d("RepNote", "Заметка добавлена: ${note.origin}, ${note.translate}")
        }
    }

    suspend fun updateNote(note: Note) {
        withContext(Dispatchers.IO) {
            noteDao.update(note)
            Log.d("RepNote", "Заметка обновлена: ${note.id}, ${note.origin}, ${note.translate}")
        }
    }

    // Настройка Firebase 5 из 5.
    suspend fun loadNotesFromFirebase() {
        withContext(Dispatchers.IO) {
            try {
                val storage = Firebase.storage
                val storageRef = storage.reference.child("start.json")

                // Загружаем JSON-данные
                val bytes = storageRef.getBytes(Long.MAX_VALUE).await()
                val jsonString = String(bytes, Charset.defaultCharset())

                // Парсим JSON в объект CardSet
                val gson = Gson()
                val cardSet = gson.fromJson(jsonString, CardSet::class.java)

                // Преобразуем Card в Note и добавляем в базу данных
                val notes = cardSet.cards.map { card ->
                    Note(
                        id = card.id, // Используем ID из Firebase вместо 0
                        origin = card.title,
                        translate = card.description
                    )
                }

                // Добавляем все заметки в базу данных только если их ещё нет
                notes.forEach { note ->
                    val existingNote = noteDao.getNoteById(note.id) // Проверяем наличие по ID
                    if (existingNote == null) {
                        noteDao.insert(note)
                        Log.d("RepNote", "Заметка из Firebase добавлена: ${note.origin}, ${note.translate}")
                    } else {
                        Log.d("RepNote", "Заметка с ID: ${note.id} уже существует и не была добавлена")
                    }
                }

            } catch (e: Exception) {
                Log.e("RepNote", "Ошибка загрузки данных из Firebase: ${e.message}")
            }
        }
    }

}
