package ru.devsoland.socialsync.di

import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import ru.devsoland.socialsync.data.dao.ContactDao
import ru.devsoland.socialsync.data.dao.EventDao
// Убедитесь, что MIGRATION_1_2 и MIGRATION_2_3 доступны через AppDatabase.Companion
import ru.devsoland.socialsync.data.database.AppDatabase 
import ru.devsoland.socialsync.data.model.Contact
import ru.devsoland.socialsync.data.repository.SocialSyncRepository
import ru.devsoland.socialsync.data.repository.SocialSyncRepositoryImpl
import javax.inject.Provider
import javax.inject.Qualifier
import javax.inject.Singleton

@Retention(AnnotationRetention.BINARY)
@Qualifier
annotation class ApplicationScope

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    @ApplicationScope
    fun provideApplicationScope(): CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    @Provides
    @Singleton
    fun provideAppDatabase(
        @ApplicationContext appContext: Context,
        contactDaoProvider: Provider<ContactDao>, 
        @ApplicationScope applicationScope: CoroutineScope
    ): AppDatabase {
        return Room.databaseBuilder(
            appContext,
            AppDatabase::class.java,
            "social_sync_database"
        )
        .addCallback(object : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                applicationScope.launch {
                    val contactDao = contactDaoProvider.get() 
                    contactDao.insert(Contact(firstName = "Иван", lastName = "Петров", phoneNumber = "123-45-67", birthDate = "1990-05-15"))
                    contactDao.insert(Contact(firstName = "Мария", lastName = "Сидорова", phoneNumber = "987-65-43", birthDate = "1992-10-20"))
                    contactDao.insert(Contact(firstName = "Алексей", lastName = "Смирнов", phoneNumber = "555-12-34", birthDate = "1985-02-01"))
                    contactDao.insert(Contact(firstName = "Елена", lastName = "Волкова", phoneNumber = "111-22-33", birthDate = "1995-12-08"))
                    contactDao.insert(Contact(firstName = "Дмитрий", lastName = "Зайцев", phoneNumber = "444-55-66", birthDate = "1988-07-23"))
                }
            }
        })
        // Регистрируем ОБЕ миграции в правильном порядке
        .addMigrations(AppDatabase.MIGRATION_1_2, AppDatabase.MIGRATION_2_3) 
        .build()
    }

    @Provides
    fun provideContactDao(appDatabase: AppDatabase): ContactDao {
        return appDatabase.contactDao()
    }

    @Provides
    fun provideEventDao(appDatabase: AppDatabase): EventDao {
        return appDatabase.eventDao()
    }

    @Provides
    @Singleton
    fun provideSocialSyncRepository(
        @ApplicationContext context: Context, // Контекст может быть не нужен здесь, если репозиторий его не использует
        contactDao: ContactDao,
        eventDao: EventDao
    ): SocialSyncRepository {
        return SocialSyncRepositoryImpl(context, contactDao, eventDao)
    }
}
