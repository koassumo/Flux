package com.igo.fluxcard.ui.card

import RepImage
import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.igo.fluxcard.model.dao.AppDatabase
import com.igo.fluxcard.model.entity.Note
import com.igo.fluxcard.model.repository.RepNote
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.core.component.KoinComponent
import java.util.concurrent.TimeUnit

class CardViewModel(
    private val repository: RepNote,
    private val imageRepository: RepImage
) : ViewModel(), KoinComponent {


    // Заметка, которая будет отображаться в текущий момент
    val note = MutableLiveData<Note>()

    // Список всех заметок, загруженных из базы данных
    val noteList = mutableListOf<Note>()

    private var currentNoteIndex = 0

    // Собственный IO Scope для работы с базой данных
    private val ioScope = CoroutineScope(Dispatchers.IO)
    private var readLocalbaseJob_2: Job? = null
    private var currentJob_3: Job? = null


    val imageUrl = MutableLiveData<String?>()

    init {
        // Загружаем все заметки из базы данных один раз при инициализации
        viewModelScope.launch {
            Log.d("CardViewModel", "Загрузка фейковых данных в базу данных")
            repository.loadNotesFromFirebase()
            restartCycle()
        }
    }

    private suspend fun restartCycle() {
        withContext(Dispatchers.IO) {
            readLocalbaseAllNotes()
        }
        withContext(Dispatchers.Main) {
            runEveryNoteCycle()
        }
    }


    // Загрузка всех заметок из БД
    private suspend fun readLocalbaseAllNotes() {
        val notesRowFromFirebase = repository.getAllNotes()
        Log.d("CardViewModel", "Размер скаченного списка из БД: ${noteList.size}")
        if (notesRowFromFirebase.isNotEmpty()) {
            noteList.clear()
            Log.d("CardViewModel", "Список очищен, текущий размер: ${noteList.size}")
            noteList.addAll(notesRowFromFirebase)
            currentNoteIndex = 0
            Log.d("CardViewModel", "Все заметки успешно загружены, размер списка: ${noteList.size}")
        } else {
            Log.d("CardViewModel", "Нет доступных заметок для загрузки")
        }
    }

    private fun runEveryNoteCycle() {
        // вывод на экран (liveData), включение слушателей во view
        note.value = noteList[currentNoteIndex]
        Log.d("CardViewModel", "Жду юзера для заметки с индексом: $currentNoteIndex")
    }


    // Обработка нажатия кнопки "Запомнил" или "Не запомнил"
    fun writeLocalbase(isRemembered: Boolean) {
        if (isRemembered) {
            note.value?.let { currentNote ->
                // 1. Создаем копию текущей заметки
                val nextShowTime = getNextShowTime(currentNote.correctStreak)
                val noteCopy = currentNote.copy(
                    correctStreak = currentNote.correctStreak + 1,
                    showFirstTimestamp = nextShowTime
                )
                // 2. Запускаем запись в базу данных в отдельной корутине с использованием ioScope
                ioScope.launch {
                    Log.d(
                        "CardViewModel",
                        "Обновлена заметка: ${noteCopy.id}, Запомнена: $isRemembered"
                    )
                    repository.insertNote(noteCopy)
                }
            }
        } else {
            // Пока ничего не делаем, оставлено для будущей обработки
        }
    }


    // Переход к следующей заметке
    fun moveToNextNote() {
        viewModelScope.launch {
            if (currentNoteIndex + 1 < noteList.size) {
                currentNoteIndex++
                runEveryNoteCycle() // Загружаем следующую заметку через основной цикл
            } else {
                Log.d("CardViewModel", "Все заметки просмотрены. Перезапуск цикла.")
                restartCycle() // Перезапускаем цикл
            }
        }
    }

    // Функция для определения времени следующего показа заметки
    fun getNextShowTime(correctStreak: Int): Long {
        val currentTime = System.currentTimeMillis()
        return when (correctStreak) {
            0 -> currentTime + TimeUnit.SECONDS.toMillis(5)
            1 -> currentTime + TimeUnit.SECONDS.toMillis(15)
            2 -> currentTime + TimeUnit.MINUTES.toMillis(10)
            3 -> currentTime + TimeUnit.HOURS.toMillis(1)
            4 -> currentTime + TimeUnit.DAYS.toMillis(1)
            5 -> currentTime + TimeUnit.DAYS.toMillis(30)
            else -> currentTime + TimeUnit.DAYS.toMillis(365)
        }
    }

    fun searchImageUrl() {
        viewModelScope.launch {
            val imageResult = imageRepository.getImage(noteList[currentNoteIndex].origin)
            if (imageResult != null) {
                imageUrl.value = imageResult.urls.regular
            } else {
                Log.d("CardViewModel", "Не удалось получить изображение")
                imageUrl.value = null
            }
        }
    }


}
