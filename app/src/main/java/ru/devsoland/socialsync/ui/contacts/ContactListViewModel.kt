package ru.devsoland.socialsync.ui.contacts

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import ru.devsoland.socialsync.data.model.Contact
import ru.devsoland.socialsync.data.model.Event
import ru.devsoland.socialsync.data.repository.SocialSyncRepository
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import javax.inject.Inject

const val BIRTHDAY_EVENT_TYPE_STRING_CONTACT_VM = "День рождения" // TODO: Вынести

@HiltViewModel
class ContactListViewModel @Inject constructor(
    private val repository: SocialSyncRepository
) : ViewModel() {

    val contacts: StateFlow<List<Contact>> = repository.getAllContacts()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun loadContactsFromDevice() {
        viewModelScope.launch {
            try {
                println("ContactListVM: Запрос на загрузку контактов с устройства...")
                val deviceContacts = repository.fetchDeviceContacts()
                println("ContactListVM: Получено ${deviceContacts.size} контактов с устройства.")

                if (deviceContacts.isNotEmpty()) {
                    var insertedCount = 0
                    var updatedCount = 0

                    deviceContacts.forEach { deviceContact ->
                        val currentDeviceContactId = deviceContact.deviceContactId
                        if (currentDeviceContactId != null) {
                            val existingContact = repository.getContactByDeviceContactId(currentDeviceContactId)

                            if (existingContact != null) {
                                val contactToUpdate = existingContact.copy(
                                    lastName = deviceContact.lastName,
                                    firstName = deviceContact.firstName,
                                    middleName = deviceContact.middleName,
                                    birthDate = deviceContact.birthDate, // birthDate здесь все еще String
                                    phoneNumber = deviceContact.phoneNumber,
                                    photoUri = deviceContact.photoUri,
                                    notes = deviceContact.notes
                                )
                                repository.updateContact(contactToUpdate)
                                updatedCount++
                                println("ContactListVM: Обновлен контакт: ${contactToUpdate.firstName} ${contactToUpdate.lastName} (Room ID: ${existingContact.id})")
                                createOrUpdateBirthdayEventIfNeeded(contactToUpdate, existingContact.id)
                            } else {
                                val newContactId = repository.insertContact(deviceContact)
                                insertedCount++
                                println("ContactListVM: Вставлен новый контакт: ${deviceContact.firstName} ${deviceContact.lastName} (Device ID: $currentDeviceContactId, New Room ID: $newContactId)")
                                createOrUpdateBirthdayEventIfNeeded(deviceContact, newContactId)
                            }
                        } else {
                            println("ContactListVM: Пропущен контакт без deviceContactId: ${deviceContact.firstName} ${deviceContact.lastName}")
                        }
                    }
                    println("ContactListVM: Синхронизация завершена. Вставлено: $insertedCount, Обновлено: $updatedCount.")
                } else {
                    println("ContactListVM: С устройства не загружено ни одного контакта.")
                }
            } catch (e: Exception) {
                println("ContactListVM: Ошибка при загрузке/синхронизации контактов: ${e.message}")
                e.printStackTrace()
            }
        }
    }

    private suspend fun createOrUpdateBirthdayEventIfNeeded(contactDetails: Contact, localContactId: Long) {
        if (contactDetails.birthDate.isNullOrBlank()) {
            println("ContactListVM: У контакта ${contactDetails.firstName} ${contactDetails.lastName} (ID: $localContactId) нет даты рождения, событие ДР не создается/обновляется.")
            return
        }

        println("ContactListVM: Обработка ДР для контакта ${contactDetails.firstName} ${contactDetails.lastName} (ID: $localContactId). Исходная строка даты: '${contactDetails.birthDate}'")

        var dateStringToParse = contactDetails.birthDate!! // Уже проверили на isNullOrBlank
        if (dateStringToParse.startsWith("--")) {
            // Ожидаемый формат "--MM-DD"
            if (dateStringToParse.length == 7 && dateStringToParse[4] == '-') { // Простая проверка формата --MM-DD
                val currentYear = LocalDate.now().year
                val month = dateStringToParse.substring(2, 4)
                val day = dateStringToParse.substring(5, 7)
                dateStringToParse = "$currentYear-$month-$day"
                println("ContactListVM: Дата без года преобразована в '$dateStringToParse' для контакта ${contactDetails.firstName}")
            } else {
                println("ContactListVM: Некорректный формат даты без года '${contactDetails.birthDate}' для контакта ${contactDetails.firstName}. Ожидался '--MM-DD'.")
                return // Пропускаем, если формат не соответствует --MM-DD
            }
        }

        val parsedBirthDate: LocalDate
        try {
            parsedBirthDate = LocalDate.parse(dateStringToParse, DateTimeFormatter.ISO_LOCAL_DATE)
        } catch (e: DateTimeParseException) {
            println("ContactListVM: Не удалось распарсить дату рождения '$dateStringToParse' (исходная: '${contactDetails.birthDate}') для контакта ${contactDetails.firstName} ${contactDetails.lastName} (ID: $localContactId). Ошибка: ${e.message}")
            return 
        }

        val existingEvent = repository.getEventByContactIdAndType(localContactId, BIRTHDAY_EVENT_TYPE_STRING_CONTACT_VM)
            .firstOrNull()

        val eventName = "День рождения ${contactDetails.firstName ?: ""} ${contactDetails.lastName ?: ""}".trim()

        if (existingEvent == null) {
            val newBirthdayEvent = Event(
                contactId = localContactId,
                name = eventName, 
                date = parsedBirthDate, 
                eventType = BIRTHDAY_EVENT_TYPE_STRING_CONTACT_VM,
                generatedGreetings = null
            )
            val newEventId = repository.insertEvent(newBirthdayEvent)
            println("ContactListVM: Создано новое событие '${newBirthdayEvent.name}' (ID: $newEventId) для контакта ${contactDetails.firstName} (ID: $localContactId) с датой $parsedBirthDate")
        } else {
            if (existingEvent.date != parsedBirthDate || existingEvent.name != eventName) {
                val eventToUpdate = existingEvent.copy(
                    name = eventName,
                    date = parsedBirthDate
                )
                repository.updateEvent(eventToUpdate)
                println("ContactListVM: Обновлено событие '${eventToUpdate.name}' (ID: ${existingEvent.id}) для контакта ${contactDetails.firstName} (ID: $localContactId) на дату $parsedBirthDate")
            } else {
                println("ContactListVM: Существующее событие '${existingEvent.name}' (ID: ${existingEvent.id}) для контакта ${contactDetails.firstName} (ID: $localContactId) не требует обновления.")
            }
        }
    }
}
