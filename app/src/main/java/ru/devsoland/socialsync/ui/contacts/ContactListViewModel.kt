package ru.devsoland.socialsync.ui.contacts

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import ru.devsoland.socialsync.data.model.Contact
import ru.devsoland.socialsync.data.repository.SocialSyncRepository
import javax.inject.Inject

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
                println("ViewModel: Запрос на загрузку контактов с устройства...")
                val deviceContacts = repository.fetchDeviceContacts() // Они уже содержат deviceContactId и birthDate как String?
                println("ViewModel: Получено ${deviceContacts.size} контактов с устройства (по новым критериям).")

                if (deviceContacts.isNotEmpty()) {
                    var insertedCount = 0
                    var updatedCount = 0

                    deviceContacts.forEach { deviceContact ->
                        // deviceContactId не должен быть null, так как мы его присваиваем в fetchDeviceContacts
                        val currentDeviceContactId = deviceContact.deviceContactId
                        if (currentDeviceContactId != null) {
                            val existingContact = repository.getContactByDeviceContactId(currentDeviceContactId)

                            if (existingContact != null) {
                                // Контакт найден в нашей базе, обновляем его
                                // Важно: используем ID из существующего контакта в Room,
                                // а остальные данные берем из deviceContact.
                                val contactToUpdate = existingContact.copy(
                                    // id = existingContact.id, // это уже есть в existingContact
                                    // deviceContactId = deviceContact.deviceContactId, // это тоже
                                    lastName = deviceContact.lastName,
                                    firstName = deviceContact.firstName,
                                    middleName = deviceContact.middleName,
                                    birthDate = deviceContact.birthDate, // Теперь это String?
                                    phoneNumber = deviceContact.phoneNumber,
                                    notes = deviceContact.notes // Если мы его получаем, иначе оставить existingContact.notes
                                )
                                repository.updateContact(contactToUpdate)
                                updatedCount++
                                println("ViewModel: Обновлен контакт: ${contactToUpdate.firstName} ${contactToUpdate.lastName} (Device ID: $currentDeviceContactId, Room ID: ${existingContact.id})")
                            } else {
                                // Контакт не найден в нашей базе, вставляем новый
                                repository.insertContact(deviceContact)
                                insertedCount++
                                println("ViewModel: Вставлен новый контакт: ${deviceContact.firstName} ${deviceContact.lastName} (Device ID: $currentDeviceContactId)")
                            }
                        } else {
                            // Этот случай не должен происходить, если fetchDeviceContacts работает правильно
                            println("ViewModel: Пропущен контакт без deviceContactId: ${deviceContact.firstName} ${deviceContact.lastName}")
                        }
                    }
                    println("ViewModel: Синхронизация завершена. Вставлено: $insertedCount, Обновлено: $updatedCount.")

                } else {
                    println("ViewModel: С устройства не загружено ни одного контакта (или не найдено контактов с именем и какой-либо датой рождения).")
                }
            } catch (e: Exception) {
                println("ViewModel: Ошибка при загрузке/синхронизации контактов с устройства: ${e.message}")
                e.printStackTrace()
                // TODO: Улучшить обработку ошибок (например, показать сообщение пользователю)
            }
        }
    }
}
