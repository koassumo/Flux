package com.igo.fluxcard.ui.card

import RepImage
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
import java.util.concurrent.TimeUnit

class CardViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: RepNote
    private val imageRepository: RepImage

    // Заметка, которая будет отображаться в текущий момент
    val note = MutableLiveData<Note>()

    // Список всех заметок, загруженных из базы данных
    val noteList = MutableLiveData<List<Note>>()

    private var currentNoteIndex = 0

    // Собственный IO Scope для работы с базой данных
    private val ioScope = CoroutineScope(Dispatchers.IO)

    val imageUrl = MutableLiveData<String?>()

    init {
        // ViewModel использует параметр Application,
        // чтобы получить экземпляр БД через AppDatabase.getDatabase(application).
        // Далее, из базы данных (AppDatabase) получаем noteDao() - это объект DAO,
        // т.е интерфейс, который содержит методы для работы с БД.
        val noteDao = AppDatabase.getDatabase(application).noteDao()
        // создаем Репу и отдаем ей noteDao, пусть она ковыряется с работой с БД
        repository = RepNote(noteDao)
        imageRepository = RepImage()

        // Загружаем все заметки из базы данных один раз при инициализации
        val initVMJob_0 = viewModelScope.launch {
            Log.d("CardViewModel", "Загрузка фейковых данных в базу данных")
            val loadFireJob_1 = launch {
                repository.loadNotesFromFirebase()
            }
            loadFireJob_1.join()
            restartCycle()
        }
    }

    private suspend fun restartCycle() {
        val loadLocalJob_2 = CoroutineScope(Dispatchers.IO).launch {
            loadAllNotesFromLocal() // Загружаем все заметки заново
        }
        loadLocalJob_2.join()
        runNoteCycle()
    }

    // Загрузка всех заметок из БД
    private suspend fun loadAllNotesFromLocal() {
        val notes = repository.getAllNotes()
        if (notes.isNotEmpty()) {
            withContext(Dispatchers.Main) {//LiveData можно обновить только отсюда
                noteList.value = notes
                currentNoteIndex = 0
                Log.d("CardViewModel", "Все заметки успешно загружены")
            }
        } else {
            Log.d("CardViewModel", "Нет доступных заметок для загрузки")
        }
    }

    // Основной цикл работы с заметками
    private suspend fun runNoteCycle() {
        if (noteList.value.isNullOrEmpty()) {
            Log.d("CardViewModel", "Нет доступных заметок для работы")
            return
        }
        displayCurrentNote()
        Log.d(
            "CardViewModel",
            "Ожидание действия пользователя для заметки с индексом: $currentNoteIndex"
        )
        // Ждём завершения обработки текущей заметки (пользователь нажимает кнопку)
    }

    // Вывод текущей заметки по индексу на экран
    private fun displayCurrentNote() {
        noteList.value?.let {
            if (currentNoteIndex in it.indices) {
                note.value = it[currentNoteIndex]
                Log.d("CardViewModel", "Загрузка заметки с индексом: $currentNoteIndex")
//                searchImage(note.value?.origin ?: "") // Добавляем вызов функции поиска изображения
            } else {
                Log.d("CardViewModel", "Индекс вне диапазона: $currentNoteIndex")
            }
        }
    }

    // Обработка нажатия кнопки "Запомнил" или "Не запомнил"
    fun nextBtnClick(isRemembered: Boolean) {
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
                    // После записи продолжаем обработку, иначе не успевает записать последнюю в цикле
                    withContext(Dispatchers.Main) {
                        moveToNextNote()
                    }
                }
            }
        } else {
            // Пока ничего не делаем, оставлено для будущей обработки
            // 3. К следующей заметке, не дожидаясь отработки корутины
            moveToNextNote()
        }

    }

    // Переход к следующей заметке
    private fun moveToNextNote() {
        noteList.value?.let {
            if (currentNoteIndex + 1 < it.size) {
                currentNoteIndex++
                viewModelScope.launch {
                    runNoteCycle() // Загружаем следующую заметку через основной цикл
                }
            } else {
                Log.d("CardViewModel", "Все заметки просмотрены. Перезапуск цикла.")
                viewModelScope.launch {
                    restartCycle() // Перезапускаем цикл
                }
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

    fun searchImage(query: String) {
        viewModelScope.launch {
            val imageResult = imageRepository.getImage(query)
            if (imageResult != null) {
                imageUrl.value = imageResult.urls.regular
            } else {
                Log.d("CardViewModel", "Не удалось получить изображение")
                imageUrl.value = null
            }
        }
    }


}
