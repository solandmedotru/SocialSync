package ru.devsoland.socialsync.data.repository

import android.annotation.SuppressLint
import android.content.Context
import android.provider.ContactsContract
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import ru.devsoland.socialsync.data.dao.ContactDao
import ru.devsoland.socialsync.data.dao.EventDao
import ru.devsoland.socialsync.data.model.Contact
import ru.devsoland.socialsync.data.model.Event
import javax.inject.Inject

class SocialSyncRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val contactDao: ContactDao,
    private val eventDao: EventDao
) : SocialSyncRepository {

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

    override suspend fun deleteContactById(contactId: Long) {
        contactDao.deleteContactById(contactId)
    }

    @SuppressLint("Range")
    override suspend fun fetchDeviceContacts(): List<Contact> = withContext(Dispatchers.IO) {
        val deviceContacts = mutableListOf<Contact>()
        val contentResolver = context.contentResolver

        val contactProjection = arrayOf(
            ContactsContract.Contacts._ID,
            ContactsContract.Contacts.DISPLAY_NAME_PRIMARY,
            ContactsContract.Contacts.PHOTO_THUMBNAIL_URI
        )

        val contactCursor = contentResolver.query(
            ContactsContract.Contacts.CONTENT_URI,
            contactProjection,
            null,
            null,
            ContactsContract.Contacts.DISPLAY_NAME_PRIMARY + " ASC"
        )

        contactCursor?.use { cursor ->
            if (cursor.moveToFirst()) {
                do {
                    val contactIdStr = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts._ID))
                    val displayName = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME_PRIMARY))
                    val photoUri = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.PHOTO_THUMBNAIL_URI))

                    var phoneNumber: String? = null
                    var birthDateString: String? = null

                    val phoneCursor = contentResolver.query(
                        ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                        arrayOf(ContactsContract.CommonDataKinds.Phone.NUMBER),
                        ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?",
                        arrayOf(contactIdStr),
                        null
                    )
                    phoneCursor?.use { pCursor ->
                        if (pCursor.moveToFirst()) {
                            phoneNumber = pCursor.getString(pCursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER))
                        }
                    }
                    phoneCursor?.close()

                    val eventSelection = (
                        ContactsContract.Data.CONTACT_ID + " = ? AND " +
                        ContactsContract.Data.MIMETYPE + " = ? AND " +
                        ContactsContract.CommonDataKinds.Event.TYPE + " = ?"
                    )
                    val eventSelectionArgs = arrayOf(
                        contactIdStr,
                        ContactsContract.CommonDataKinds.Event.CONTENT_ITEM_TYPE,
                        ContactsContract.CommonDataKinds.Event.TYPE_BIRTHDAY.toString()
                    )
                    val eventProjection = arrayOf(ContactsContract.CommonDataKinds.Event.START_DATE)

                    val eventCursor = contentResolver.query(
                        ContactsContract.Data.CONTENT_URI,
                        eventProjection,
                        eventSelection,
                        eventSelectionArgs,
                        null
                    )
                    eventCursor?.use { eCursor ->
                        if (eCursor.moveToFirst()) {
                            birthDateString = eCursor.getString(eCursor.getColumnIndex(ContactsContract.CommonDataKinds.Event.START_DATE))
                        }
                    }
                    eventCursor?.close()

                    val nameParts = displayName?.split(" ", limit = 2) ?: listOf()
                    val firstName = nameParts.getOrNull(0) ?: ""
                    val lastName = nameParts.getOrNull(1) ?: ""

                    if (firstName.isNotBlank() && birthDateString != null) {
                         deviceContacts.add(
                            Contact(
                                deviceContactId = contactIdStr,
                                firstName = firstName,
                                lastName = lastName,
                                phoneNumber = phoneNumber,
                                birthDate = birthDateString,
                                photoUri = photoUri
                            )
                        )
                    } else if (firstName.isNotBlank() && birthDateString == null) {
                         println("Контакт $displayName ($contactIdStr) не имеет даты рождения в ContactsContract или она null.")
                    }

                } while (cursor.moveToNext())
            }
        }
        contactCursor?.close()
        println("fetchDeviceContacts: Найдено на устройстве (с непустым именем и непустой строкой ДР): ${deviceContacts.size}")
        return@withContext deviceContacts
    }

    override suspend fun getContactByDeviceContactId(deviceContactId: String): Contact? {
        return contactDao.getContactByDeviceContactId(deviceContactId)
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

    override suspend fun updateEventGeneratedGreetings(eventId: Long, greetings: List<String>?) {
        eventDao.updateGeneratedGreetings(eventId, greetings)
    }

    // Реализация нового метода
    override fun getEventByContactIdAndType(contactId: Long, eventType: String): Flow<Event?> {
        return eventDao.getEventByContactIdAndType(contactId, eventType)
    }
}
