package ru.devsoland.socialsync.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import ru.devsoland.socialsync.data.dao.ContactDao
import ru.devsoland.socialsync.data.dao.EventDao
import ru.devsoland.socialsync.data.model.Contact
import ru.devsoland.socialsync.data.model.Event

@Database(
    entities = [Contact::class, Event::class],
    version = 2, // <-- ВЕРСИЯ УВЕЛИЧЕНА
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun contactDao(): ContactDao
    abstract fun eventDao(): EventDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "social_sync_database"
                )
                .fallbackToDestructiveMigration() // <-- ДОБАВЛЕНО
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
