package ru.devsoland.socialsync.data.database

// import android.content.Context // Не используется, если getDatabase закомментирован
import androidx.room.Database
// import androidx.room.Room // Не используется, если getDatabase закомментирован
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
// import kotlinx.coroutines.CoroutineScope // Не используется, если AppDatabaseCallback закомментирован
// import kotlinx.coroutines.launch // Не используется, если AppDatabaseCallback закомментирован
import ru.devsoland.socialsync.data.database.ContactDao // ИСПРАВЛЕННЫЙ ИМПОРТ
import ru.devsoland.socialsync.data.database.EventDao   // ИСПРАВЛЕННЫЙ ИМПОРТ
import ru.devsoland.socialsync.data.model.Contact
import ru.devsoland.socialsync.data.model.Event
// import ru.devsoland.socialsync.di.ApplicationScope // Не используется, если AppDatabaseCallback закомментирован
// import javax.inject.Provider // Не используется, если AppDatabaseCallback закомментирован

@Database(entities = [Contact::class, Event::class], version = 3, exportSchema = false) // <-- ВЕРСИЯ УВЕЛИЧЕНА ДО 3
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun contactDao(): ContactDao // Теперь будет использовать правильный тип
    abstract fun eventDao(): EventDao     // Теперь будет использовать правильный тип

    companion object {
        // Миграция с версии 1 на версию 2: добавление столбца tags в contacts
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE contacts ADD COLUMN tags TEXT NOT NULL DEFAULT ''")
            }
        }

        // Миграция с версии 2 на версию 3: добавление столбца generatedGreetings в events
        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // Добавляем новый столбец 'generatedGreetings' в таблицу 'events'
                // Тип TEXT, так как List<String> будет храниться как JSON строка.
                // Может быть NULLABLE, так как поле в Event.kt тоже nullable.
                db.execSQL("ALTER TABLE events ADD COLUMN generatedGreetings TEXT DEFAULT NULL")
            }
        }

        // Закомментированный статический метод getDatabase и AppDatabaseCallback 
        // остаются закомментированными, так как предполагается использование Hilt.
    }
}
