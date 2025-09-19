package ru.devsoland.socialsync.di

import android.content.Context
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import ru.devsoland.socialsync.data.dao.ContactDao
import ru.devsoland.socialsync.data.dao.EventDao
import ru.devsoland.socialsync.data.database.AppDatabase
import ru.devsoland.socialsync.data.repository.SocialSyncRepository
import ru.devsoland.socialsync.data.repository.SocialSyncRepositoryImpl
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class) // Зависимости будут жить столько же, сколько приложение
object AppModule {

    @Provides
    @Singleton // Гарантирует, что будет только один экземпляр AppDatabase
    fun provideAppDatabase(@ApplicationContext appContext: Context): AppDatabase {
        return Room.databaseBuilder(
            appContext,
            AppDatabase::class.java,
            "social_sync_database"
        ).build() // Мы убрали fallbackToDestructiveMigration из AppDatabase.Companion,
                  // здесь можно его добавить при необходимости для разработки
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
    @Singleton // Репозиторий также обычно Singleton
    fun provideSocialSyncRepository(
        contactDao: ContactDao,
        eventDao: EventDao
    ): SocialSyncRepository {
        // Hilt знает, как создать ContactDao и EventDao благодаря функциям выше.
        // Hilt также знает, как создать SocialSyncRepositoryImpl,
        // но здесь мы явно создаем его, чтобы показать процесс.
        // Альтернативно, можно было бы сделать SocialSyncRepositoryImpl
        // параметром конструктора и использовать @Binds (см. альтернативу ниже)
        return SocialSyncRepositoryImpl(contactDao, eventDao)
    }
}

/*
// Альтернативный (и часто предпочитаемый) способ предоставления реализации интерфейса:
@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindSocialSyncRepository(
        socialSyncRepositoryImpl: SocialSyncRepositoryImpl
    ): SocialSyncRepository
}
// Если выбрать этот способ, то provideSocialSyncRepository из AppModule выше не нужен,
// а SocialSyncRepositoryImpl будет внедряться благодаря @Inject в его конструкторе,
// а Hilt будет знать, как предоставить ContactDao и EventDao.
// Этот подход более "чистый" для связывания интерфейсов с реализациями.
// Можно использовать один из подходов или даже оба (для разных вещей).
// Для начала оставим вариант с @Provides в AppModule, он более явный.
*/
