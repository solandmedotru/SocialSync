package ru.devsoland.socialsync.ui.addeditcontact

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import ru.devsoland.socialsync.data.model.Contact
import ru.devsoland.socialsync.data.repository.SocialSyncRepository
import ru.devsoland.socialsync.ui.AppDestinations
import javax.inject.Inject

@HiltViewModel
class AddEditContactViewModel @Inject constructor(
    private val repository: SocialSyncRepository,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    val currentContactId: Long = savedStateHandle.get<Long>(AppDestinations.CONTACT_ID_ARG) 
        ?: AppDestinations.DEFAULT_NEW_CONTACT_ID

    private val _contactState = MutableStateFlow<Contact?>(null)
    val contactState: StateFlow<Contact?> = _contactState.asStateFlow()

    val firstName = MutableStateFlow("")
    val lastName = MutableStateFlow("")
    val phoneNumber = MutableStateFlow("")
    val birthDate = MutableStateFlow<String?>("") // Хранит YYYY-MM-DD или null/пусто

    private val _tags = MutableStateFlow<List<String>>(emptyList())
    val tags: StateFlow<List<String>> = _tags.asStateFlow()

    val suggestedTags: List<String> = listOf(
        "коллега", "начальник", "супруг", "супруга", "дочь", "сын", "брат", "сестра",
        "мама", "папа", "друг", "подруга", "любимый", "любимая", "бабушка", "дедушка",
        "сосед", "соседка", "родственник", "знакомый"
    )

    private val _saveSuccessEvent = MutableSharedFlow<Unit>()
    val saveSuccessEvent: SharedFlow<Unit> = _saveSuccessEvent.asSharedFlow()

    init {
        if (currentContactId != AppDestinations.DEFAULT_NEW_CONTACT_ID) {
            loadContact(currentContactId)
        } else {
            // Режим добавления
        }
    }

    private fun loadContact(contactId: Long) {
        viewModelScope.launch {
            repository.getContactById(contactId).collectLatest { contact ->
                _contactState.value = contact
                contact?.let {
                    firstName.value = it.firstName ?: ""
                    lastName.value = it.lastName ?: ""
                    phoneNumber.value = it.phoneNumber ?: ""
                    birthDate.value = it.birthDate // Загружаем дату как есть
                    _tags.value = it.tags
                }
            }
        }
    }

    fun addTag(tag: String) {
        val trimmedTag = tag.trim()
        if (trimmedTag.isNotBlank() && !_tags.value.contains(trimmedTag)) {
            _tags.value = _tags.value + trimmedTag
        }
    }

    fun removeTag(tag: String) {
        _tags.value = _tags.value - tag
    }

    fun saveContact() {
        val contactIdForSaveOperation = currentContactId

        val firstNameValue = firstName.value.trim()
        val lastNameValue = lastName.value.trim().ifEmpty { null }
        val phoneNumberValue = phoneNumber.value.trim()
        // birthDate.value уже хранит YYYY-MM-DD или null/пустую строку из DatePicker
        val birthDateValue = birthDate.value?.trim()?.ifEmpty { null } 
        val photoUriValue = _contactState.value?.photoUri // Берем из загруженного контакта, если есть

        if (firstNameValue.isEmpty()) {
            // TODO: Показать ошибку пользователю (например, через Snackbar)
            return
        }

        val contactToSave = Contact(
            id = if (contactIdForSaveOperation != AppDestinations.DEFAULT_NEW_CONTACT_ID) contactIdForSaveOperation else AppDestinations.DEFAULT_NEW_CONTACT_ID,
            firstName = firstNameValue,
            lastName = lastNameValue,
            phoneNumber = phoneNumberValue,
            birthDate = birthDateValue, // Сохраняем дату в формате YYYY-MM-DD или null
            tags = _tags.value,
            photoUri = photoUriValue,
            deviceContactId = _contactState.value?.deviceContactId // Берем из загруженного, если есть
        )

        viewModelScope.launch {
            val savedContactId: Long
            if (contactToSave.id != AppDestinations.DEFAULT_NEW_CONTACT_ID) {
                repository.updateContact(contactToSave)
                savedContactId = contactToSave.id
            } else {
                savedContactId = repository.insertContact(contactToSave)
            }

            // Управляем событием "День рождения"
            repository.manageBirthdayEventForContact(
                contactId = savedContactId,
                birthDate = birthDateValue, // Передаем YYYY-MM-DD или null
                contactFirstName = firstNameValue
            )
            
            _saveSuccessEvent.emit(Unit) 
        }
    }
}
