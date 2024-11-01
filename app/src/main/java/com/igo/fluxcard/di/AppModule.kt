package com.igo.fluxcard.di

import RepImage
import com.igo.fluxcard.model.dao.AppDatabase
import com.igo.fluxcard.model.repository.RepNote
import com.igo.fluxcard.ui.card.CardViewModel
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val appModule = module {

    // Определение базы данных как Singleton
    single { AppDatabase.getDatabase(androidContext()) } // Передаем context

    // Определение DAO для работы с заметками
    single { get<AppDatabase>().noteDao() }

    // Определение репозиториев
    single { RepNote(get()) } // NoteDao передается в RepNote
    single { RepImage() } // Репозиторий для работы с изображениями

    // Определение ViewModel
    viewModel { CardViewModel(get(), get()) } // Передаем Application и репозитории в ViewModel
}
