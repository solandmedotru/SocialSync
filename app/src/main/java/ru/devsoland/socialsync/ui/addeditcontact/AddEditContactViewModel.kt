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
import ru.devsoland.socialsync.util.AppConstants // Импортируем наш новый объект
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
    val birthDate = MutableStateFlow<String?>("") 

    private val _tags = MutableStateFlow<List<String>>(emptyList())
    val tags: StateFlow<List<String>> = _tags.asStateFlow()

    val suggestedTags: List<String> = AppConstants.MASTER_TAG_LIST

    private val _saveSuccessEvent = MutableSharedFlow<Unit>()
    val saveSuccessEvent: SharedFlow<Unit> = _saveSuccessEvent.asSharedFlow()

    init {
        if (currentContactId != AppDestinations.DEFAULT_NEW_CONTACT_ID) {
            loadContact(currentContactId)
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
                    birthDate.value = it.birthDate
                    _tags.value = it.tags 
                }
            }
        }
    }

    fun addTag(tag: String) {
        val trimmedTag = tag.trim()
        if (trimmedTag.isBlank()) return

        val canonicalTag = AppConstants.MASTER_TAG_LIST.find { it.equals(trimmedTag, ignoreCase = true) }
        val tagToAdd = canonicalTag ?: trimmedTag

        if (!_tags.value.contains(tagToAdd)) {
            _tags.value = _tags.value + tagToAdd
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
        val birthDateValue = birthDate.value?.trim()?.ifEmpty { null } 
        val photoUriValue = _contactState.value?.photoUri

        if (firstNameValue.isEmpty()) {
            return
        }
        
        val contactToSave = Contact(
            id = if (contactIdForSaveOperation != AppDestinations.DEFAULT_NEW_CONTACT_ID) contactIdForSaveOperation else AppDestinations.DEFAULT_NEW_CONTACT_ID,
            firstName = firstNameValue,
            lastName = lastNameValue,
            phoneNumber = phoneNumberValue,
            birthDate = birthDateValue,
            tags = _tags.value, 
            photoUri = photoUriValue,
            deviceContactId = _contactState.value?.deviceContactId
        )
        // Логирование перед сохранением
        println("SAVE_CONTACT_DEBUG: Saving contact with tags: ${contactToSave.tags}")

        viewModelScope.launch {
            val savedContactId: Long
            if (contactToSave.id != AppDestinations.DEFAULT_NEW_CONTACT_ID) {
                repository.updateContact(contactToSave)
                savedContactId = contactToSave.id
            } else {
                savedContactId = repository.insertContact(contactToSave)
            }

            repository.manageBirthdayEventForContact(
                contactId = savedContactId,
                birthDate = birthDateValue, 
                contactFirstName = firstNameValue
            )
            
            _saveSuccessEvent.emit(Unit) 
        }
    }
}
