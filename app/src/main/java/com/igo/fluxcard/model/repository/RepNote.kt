package com.igo.fluxcard.model.repository

import android.util.Log
import com.igo.fluxcard.model.dao.NoteDao
import com.igo.fluxcard.model.entity.Note
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class RepNote(private val noteDao: NoteDao) {

    suspend fun insertNote(note: Note) {
        withContext(Dispatchers.IO) {
            noteDao.insert(note)
            Log.d("RepNote", "Заметка добавлена: ${note.origin}, ${note.translate}")
        }
    }

    suspend fun getNoteById(id: Int): Note? {
        return withContext(Dispatchers.IO) {
            val note = noteDao.getNoteById(id)
            Log.d("RepNote", "Запрос заметки с ID: $id, результат: ${note?.origin}, ${note?.translate}")
            note
        }
    }

    suspend fun getAllNotes(): List<Note> {
        return withContext(Dispatchers.IO) {
            val notes = noteDao.getAllNotes()
            Log.d("RepNote", "Все заметки: ${notes.size}")
            notes
        }
    }

    fun getInitListNote(): List<Note> {
        return listOf(
            Note(1, "Hello", "Привет"),
            Note(2, "World", "Мир"),
            Note(3, "Sun", "Солнце")
        )
    }

    suspend fun insertFakeNotes() {
        withContext(Dispatchers.IO) {
            val fakeNotes = getInitListNote()
            fakeNotes.forEach { note ->
                noteDao.insert(note)
                Log.d("RepNote", "Фейковая заметка добавлена: ${note.origin}, ${note.translate}")
            }
        }
    }

    suspend fun updateNote(note: Note) {
        withContext(Dispatchers.IO) {
            noteDao.update(note)
            Log.d("RepNote", "Заметка обновлена: ${note.id}, ${note.origin}, ${note.translate}")
        }
    }

}
