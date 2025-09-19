package ru.devsoland.socialsync.data.repository

import kotlinx.coroutines.flow.Flow
import ru.devsoland.socialsync.data.dao.ContactDao
import ru.devsoland.socialsync.data.dao.EventDao
import ru.devsoland.socialsync.data.model.Contact
import ru.devsoland.socialsync.data.model.Event
import javax.inject.Inject // Стандартная аннотация для DI

// @Inject в конструкторе говорит Hilt (или другому DI фреймворку),
// как создать экземпляр этого класса.
// Hilt должен будет знать, как предоставить ContactDao и EventDao.
class SocialSyncRepositoryImpl @Inject constructor(
    private val contactDao: ContactDao,
    private val eventDao: EventDao
) : SocialSyncRepository {

    // --- Контакты ---
    override fun getAllContacts(): Flow<List<Contact>> {
        return contactDao.getAllContacts()
    }

    override fun getContactById(contactId: Long): Flow<Contact?> {
        return contactDao.getContactById(contactId)
    }

    override suspend fun insertContact(contact: Contact): Long {
        return contactDao.insert(contact)
    }

    override suspend fun updateContact(contact: Contact) {
        contactDao.update(contact)
    }

    override suspend fun deleteContact(contact: Contact) {
        contactDao.delete(contact)
    }

    // --- События ---
    override fun getAllEvents(): Flow<List<Event>> {
        return eventDao.getAllEvents()
    }

    override fun getEventsForContact(contactId: Long): Flow<List<Event>> {
        return eventDao.getEventsForContact(contactId)
    }

    override fun getEventById(eventId: Long): Flow<Event?> {
        return eventDao.getEventById(eventId)
    }

    override suspend fun insertEvent(event: Event): Long {
        return eventDao.insert(event)
    }

    override suspend fun updateEvent(event: Event) {
        eventDao.update(event)
    }

    override suspend fun deleteEvent(event: Event) {
        eventDao.delete(event)
    }
}