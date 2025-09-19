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
    entities = [Contact::class, Event::class], // Наши таблицы
    version = 1, // Начальная версия БД. Увеличивать при изменении схемы
    exportSchema = false // Для простоты пока отключаем экспорт схемы
)
@TypeConverters(Converters::class) // Регистрируем наши конвертеры типов
abstract class AppDatabase : RoomDatabase() {

    abstract fun contactDao(): ContactDao
    abstract fun eventDao(): EventDao

    companion object {
        // @Volatile гарантирует, что значение INSTANCE всегда актуально
        // и одинаково для всех потоков исполнения.
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            // synchronized гарантирует, что только один поток может
            // одновременно выполнять этот блок кода,
            // предотвращая создание нескольких экземпляров БД.
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "social_sync_database" // Имя файла БД на устройстве
                )
                // .fallbackToDestructiveMigration() // Для миграций, пока не нужно
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}