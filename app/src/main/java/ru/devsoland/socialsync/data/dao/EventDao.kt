package ru.devsoland.socialsync.data.dao

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

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(event: Event): Long

    @Update
    suspend fun update(event: Event)

    @Delete
    suspend fun delete(event: Event)

    @Query("SELECT * FROM events ORDER BY date ASC")
    fun getAllEvents(): Flow<List<Event>>

    @Query("SELECT * FROM events WHERE contactId = :contactId ORDER BY date ASC")
    fun getEventsForContact(contactId: Long): Flow<List<Event>>

    @Query("SELECT * FROM events WHERE id = :eventId")
    fun getEventById(eventId: Long): Flow<Event?>
}