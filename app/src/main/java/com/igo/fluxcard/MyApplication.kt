package com.igo.fluxcard

import android.app.Application
import com.igo.fluxcard.di.appModule
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin

class MyApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        // Запуск Koin
        startKoin {
            androidLogger() // Логирование Koin (опционально)
            androidContext(this@MyApplication) // Контекст приложения
            modules(appModule) // Регистрируем модуль Koin
        }
    }
}

