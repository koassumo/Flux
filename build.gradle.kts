// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    id("com.android.application") version "8.1.1" apply false
    id("org.jetbrains.kotlin.android") version "1.9.0" apply false

    // Настройка Firebase 2 из 5. Важно настройка 1 - это google-services.json в app/ проекта
    id("com.google.gms.google-services") version "4.4.2" apply false

    //id("com.google.devtools.ksp") version "1.8.10-1.0.9" apply false

    id("androidx.navigation.safeargs") version "2.8.3" apply false
}

