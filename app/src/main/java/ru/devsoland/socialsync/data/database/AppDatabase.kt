package ru.devsoland.socialsync.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import ru.devsoland.socialsync.data.dao.ContactDao
import ru.devsoland.socialsync.data.dao.EventDao
import ru.devsoland.socialsync.data.model.Contact
import ru.devsoland.socialsync.data.model.Event
import ru.devsoland.socialsync.di.ApplicationScope
import javax.inject.Provider

@Database(entities = [Contact::class, Event::class], version = 2, exportSchema = false)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun contactDao(): ContactDao
    abstract fun eventDao(): EventDao

    companion object {
        // Миграция с версии 1 на версию 2: добавление столбца tags
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // Добавляем новый столбец 'tags' в таблицу 'contacts'
                // Тип TEXT, так как List<String> будет храниться как объединенная строка.
                // Значение по умолчанию - пустая строка, что соответствует emptyList() для тегов.
                db.execSQL("ALTER TABLE contacts ADD COLUMN tags TEXT NOT NULL DEFAULT ''")
            }
        }

        // Этот метод больше не нужен здесь, если используется Hilt-модуль для предоставления БД
        // @Volatile
        // private var INSTANCE: AppDatabase? = null
        // 
        // fun getDatabase(
        // context: Context, 
        // contactDaoProvider: Provider<ContactDao>, 
        // applicationScope: CoroutineScope
        // ): AppDatabase {
        // return INSTANCE ?: synchronized(this) {
        // val instance = Room.databaseBuilder(
        // context.applicationContext,
        // AppDatabase::class.java,
        // "social_sync_database"
        // )
        // .addMigrations(MIGRATION_1_2) // <-- ДОБАВЛЯЕМ МИГРАЦИЮ ЗДЕСЬ
        // .addCallback(AppDatabaseCallback(contactDaoProvider, applicationScope))
        // .build()
        // INSTANCE = instance
        // instance
        // }
        // }
    }
}

// Callback остается таким же, но теперь он будет вызываться только при ПЕРВОМ создании БД,
// а не при каждой миграции. Если нужно что-то делать при миграции, это делается в MIGRATION_X_Y.
// class AppDatabaseCallback(
// private val contactDaoProvider: Provider<ContactDao>,
// @ApplicationScope private val applicationScope: CoroutineScope
// ) : RoomDatabase.Callback() {
// override fun onCreate(db: SupportSQLiteDatabase) {
// super.onCreate(db)
// applicationScope.launch {
// val contactDao = contactDaoProvider.get()
// Prepopulate database
// contactDao.insert(Contact(firstName = "Иван", lastName = "Петров", phoneNumber = "123-45-67", birthDate = "1990-05-15"))
// ... другие контакты ...
// }
// }
// }

