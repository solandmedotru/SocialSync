package ru.devsoland.socialsync.data.repository

import kotlinx.coroutines.flow.Flow
import ru.devsoland.socialsync.data.model.Contact
import ru.devsoland.socialsync.data.model.Event

interface SocialSyncRepository {

    // --- Контакты ---
    fun getAllContacts(): Flow<List<Contact>>
    fun getContactById(contactId: Long): Flow<Contact?>
    suspend fun insertContact(contact: Contact): Long
    suspend fun updateContact(contact: Contact)
    suspend fun deleteContact(contact: Contact)
    suspend fun fetchDeviceContacts(): List<Contact>
    suspend fun getContactByDeviceContactId(deviceContactId: String): Contact? 

    // --- События ---
    fun getAllEvents(): Flow<List<Event>>
    fun getEventsForContact(contactId: Long): Flow<List<Event>>
    fun getEventById(eventId: Long): Flow<Event?>
    suspend fun insertEvent(event: Event): Long
    suspend fun updateEvent(event: Event)
    suspend fun deleteEvent(event: Event)
    suspend fun updateEventGeneratedGreetings(eventId: Long, greetings: List<String>?)

    // Новый метод для получения события по ID контакта и типу
    fun getEventByContactIdAndType(contactId: Long, eventType: String): Flow<Event?>
}
