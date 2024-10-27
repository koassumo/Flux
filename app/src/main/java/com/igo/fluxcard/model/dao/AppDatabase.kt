package com.igo.fluxcard.model.dao

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import android.content.Context
import com.igo.fluxcard.model.entity.Note
import com.igo.fluxcard.model.repository.RepNote

@Database(entities = [Note::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {

    abstract fun noteDao(): NoteDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "fluxcard_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}

//object RepositoryProvider {
//    @Volatile
//    private var repository: RepNote? = null
//
//    fun getRepository(context: Context): RepNote {
//        return repository ?: synchronized(this) {
//            val instance = RepNote(AppDatabase.getDatabase(context).noteDao())
//            repository = instance
//            instance
//        }
//    }
//}
