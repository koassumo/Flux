package com.igo.fluxcard.ui.card

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.igo.fluxcard.model.dao.AppDatabase
import com.igo.fluxcard.model.entity.Note
import com.igo.fluxcard.model.repository.RepNote
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class CardViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: RepNote

    // Заметка, которая будет отображаться в текущий момент
    val note = MutableLiveData<Note>()
    // Список всех заметок, загруженных из базы данных
    val noteList = MutableLiveData<List<Note>>()

    private var currentNoteIndex = 0

    // Собственный IO Scope для работы с базой данных
    private val ioScope = CoroutineScope(Dispatchers.IO)

    init {
        // ViewModel использует параметр Application,
        // чтобы получить экземпляр БД через AppDatabase.getDatabase(application).
        // Далее, из базы данных (AppDatabase) получаем noteDao() - это объект DAO,
        // т.е интерфейс, который содержит методы для работы с БД.
        val noteDao = AppDatabase.getDatabase(application).noteDao()
        // создаем Репу и отдаем ей noteDao, пусть она ковыряется с работой с БД
        repository = RepNote(noteDao)

        // Загружаем все заметки из базы данных один раз при инициализации
        viewModelScope.launch {
            Log.d("CardViewModel", "Загрузка фейковых данных в базу данных")
            repository.insertFakeNotes() // Добавим фейковые данные в базу
            restartCycle() // Запускаем цикл обработки заметок
        }
    }

    // Загрузка всех заметок из БД
    private suspend fun loadAllNotes() {
        val notes = repository.getAllNotes()
        if (notes.isNotEmpty()) {
            noteList.value = notes
            currentNoteIndex = 0
            Log.d("CardViewModel", "Все заметки успешно загружены")
        } else {
            Log.d("CardViewModel", "Нет доступных заметок для загрузки")
        }
    }

    // Основной цикл работы с заметками
    private fun runNoteCycle() {
        if (noteList.value.isNullOrEmpty()) {
            Log.d("CardViewModel", "Нет доступных заметок для работы")
            return
        }
        loadCurrentNote() // Загружаем текущую заметку
        Log.d("CardViewModel", "Ожидание действия пользователя для заметки с индексом: $currentNoteIndex")
        // Ждём завершения обработки текущей заметки (пользователь нажимает кнопку)
    }

    // Загрузка текущей заметки по индексу
    private fun loadCurrentNote() {
        noteList.value?.let {
            if (currentNoteIndex in it.indices) {
                note.value = it[currentNoteIndex]
                Log.d("CardViewModel", "Загрузка заметки с индексом: $currentNoteIndex")
            } else {
                Log.d("CardViewModel", "Индекс вне диапазона: $currentNoteIndex")
            }
        }
    }

    // Обработка нажатия кнопки "Запомнил" или "Не запомнил"
    fun rememberBtnClick(isRemembered: Boolean) {
        if (isRemembered) {
            note.value?.let { currentNote ->
                // 1. Создаем копию текущей заметки
                val noteCopy = currentNote.copy(correctStreak = currentNote.correctStreak + 1)

                // 2. Запускаем запись в базу данных в отдельной корутине с использованием ioScope
                ioScope.launch {
                    Log.d("CardViewModel", "Обновлена заметка: ${noteCopy.id}, Запомнена: $isRemembered")
                    repository.insertNote(noteCopy)
                }
            }
        } else {
            // Пока ничего не делаем, оставлено для будущей обработки
        }
        // 3. К следующей заметке, не дожидаясь отработки корутины
        moveToNextNote()
    }

    // Переход к следующей заметке
    private fun moveToNextNote() {
        noteList.value?.let {
            if (currentNoteIndex + 1 < it.size) {
                currentNoteIndex++
                runNoteCycle() // Загружаем следующую заметку через основной цикл
            } else {
                Log.d("CardViewModel", "Все заметки просмотрены. Перезапуск цикла.")
                viewModelScope.launch {
                    restartCycle() // Перезапускаем цикл
                }
            }
        }
    }

    // Метод для перезапуска цикла обработки заметок
    private suspend fun restartCycle() {
        loadAllNotes() // Загружаем все заметки заново
        runNoteCycle() // Запускаем основной цикл обработки
    }
}
