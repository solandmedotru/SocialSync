package ru.devsoland.socialsync.data.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow
import ru.devsoland.socialsync.data.model.Contact

@Dao
interface ContactDao {

    @Insert(onConflict = OnConflictStrategy.Companion.REPLACE)
    suspend fun insert(contact: Contact): Long

    @Update
    suspend fun update(contact: Contact)

    @Delete
    suspend fun delete(contact: Contact) // Этот метод можно использовать, если есть объект Contact

    @Query("DELETE FROM contacts WHERE id = :contactId")
    suspend fun deleteContactById(contactId: Long) // Новый метод для удаления по ID

    @Query("SELECT * FROM contacts ORDER BY lastName ASC, firstName ASC")
    fun getAllContacts(): Flow<List<Contact>>

    @Query("SELECT * FROM contacts WHERE id = :contactId")
    fun getContactById(contactId: Long): Flow<Contact?>

    @Query("SELECT * FROM contacts WHERE deviceContactId = :deviceContactId LIMIT 1")
    suspend fun getContactByDeviceContactId(deviceContactId: String): Contact?
}