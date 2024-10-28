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
                        id = 0,  // или оставить autoGenerate, если это не используется
                        origin = card.title,
                        translate = card.description
                    )
                }

                // Добавляем все заметки в базу данных
                notes.forEach { note ->
                    noteDao.insert(note)
                    Log.d("RepNote", "Заметка из Firebase добавлена: ${note.origin}, ${note.translate}")
                }

            } catch (e: Exception) {
                Log.e("RepNote", "Ошибка загрузки данных из Firebase: ${e.message}")
            }
        }
    }


    //fake for test
    suspend fun insertFakeNotes() {
        withContext(Dispatchers.IO) {
            val fakeNotes = getInitListNote()
            fakeNotes.forEach { note ->
                noteDao.insert(note)
                Log.d("RepNote", "Фейковая заметка добавлена: ${note.origin}, ${note.translate}")
            }
        }
    }

    fun getInitListNote(): List<Note> {
        return listOf(
            Note(1, "Hello", "Привет"),
            Note(2, "World", "Мир"),
            Note(3, "Sun", "Солнце")
        )
    }


}
