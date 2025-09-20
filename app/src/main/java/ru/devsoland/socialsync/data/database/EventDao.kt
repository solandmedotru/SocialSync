package ru.devsoland.socialsync.data.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow
import ru.devsoland.socialsync.data.model.Event

@Dao
interface EventDao {

    companion object {
        const val DEFAULT_BIRTHDAY_EVENT_TYPE = "День рождения"
    }

    @Query("SELECT * FROM events")
    fun getAllEvents(): Flow<List<Event>>

    @Query("SELECT * FROM events WHERE contactId = :contactId")
    fun getEventsForContact(contactId: Long): Flow<List<Event>>

    @Query("SELECT * FROM events WHERE id = :eventId")
    fun getEventById(eventId: Long): Flow<Event?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEvent(event: Event): Long

    @Update
    suspend fun updateEvent(event: Event)

    @Delete
    suspend fun deleteEvent(event: Event)

    @Query("UPDATE events SET generatedGreetings = :greetings WHERE id = :eventId")
    suspend fun updateEventGeneratedGreetings(eventId: Long, greetings: List<String>?)

    // Новый метод для получения события по ID контакта и типу (например, день рождения)
    @Query("SELECT * FROM events WHERE contactId = :contactId AND eventType = :eventType LIMIT 1")
    fun getEventByContactIdAndType(contactId: Long, eventType: String): Flow<Event?>

    // Новый метод для получения ОДНОГО события (не Flow) дня рождения по ID контакта
    @Query("SELECT * FROM events WHERE contactId = :contactId AND eventType = :eventType LIMIT 1")
    suspend fun getBirthdayEventForContactSuspend(contactId: Long, eventType: String = DEFAULT_BIRTHDAY_EVENT_TYPE): Event?

    // Новый метод для удаления событий по ID контакта и типу (например, все дни рождения для контакта)
    // Возвращает количество удаленных строк, если это важно.
    @Query("DELETE FROM events WHERE contactId = :contactId AND eventType = :eventType")
    suspend fun deleteEventsByContactIdAndType(contactId: Long, eventType: String = DEFAULT_BIRTHDAY_EVENT_TYPE): Int
}
