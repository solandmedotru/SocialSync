package ru.devsoland.socialsync.ui.editcontact

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import ru.devsoland.socialsync.data.model.Contact
import ru.devsoland.socialsync.data.repository.SocialSyncRepository
import ru.devsoland.socialsync.ui.AppDestinations
import javax.inject.Inject

@HiltViewModel
class EditContactViewModel @Inject constructor(
    private val repository: SocialSyncRepository,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _contactState = MutableStateFlow<Contact?>(null)
    val contactState: StateFlow<Contact?> = _contactState.asStateFlow()

    // StateFlows для каждого редактируемого поля
    val firstName = MutableStateFlow("")
    val lastName = MutableStateFlow("")
    val phoneNumber = MutableStateFlow("")
    val birthDate = MutableStateFlow<String?>("")
    // ... другие поля ...

    // StateFlow для тегов
    private val _tags = MutableStateFlow<List<String>>(emptyList())
    val tags: StateFlow<List<String>> = _tags.asStateFlow()

    // Список предложенных тегов
    val suggestedTags: List<String> = listOf(
        "коллега", "начальник", "супруг", "супруга", "дочь", "сын", "брат", "сестра",
        "мама", "папа", "друг", "подруга", "любимый", "любимая", "бабушка", "дедушка",
        "сосед", "соседка", "родственник", "знакомый"
    ) // Добавил пару общих

    init {
        savedStateHandle.get<Long>(AppDestinations.EDIT_CONTACT_ID_ARG)?.let {
            if (it != 0L) {
                loadContact(it)
            }
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
                    _tags.value = it.tags // Загружаем теги
                    // ... обновить другие StateFlow полей
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
        val currentContact = _contactState.value
        // Убедимся, что lastName является nullable и может быть пустым
        val lastNameValue = lastName.value.trim().ifEmpty { null }

        val contactToSave = currentContact?.copy(
            firstName = firstName.value.trim(),
            lastName = lastNameValue, // Используем обработанное значение
            phoneNumber = phoneNumber.value.trim(),
            birthDate = birthDate.value?.trim()?.ifEmpty { null },
            tags = _tags.value // Сохраняем теги
            // ... обновить другие поля контакта
        ) ?: Contact(
            id = 0, 
            firstName = firstName.value.trim(),
            lastName = lastNameValue,
            phoneNumber = phoneNumber.value.trim(),
            birthDate = birthDate.value?.trim()?.ifEmpty { null },
            tags = _tags.value
        )

        viewModelScope.launch {
            if (contactToSave.id != 0L || currentContact != null) { 
                repository.updateContact(contactToSave)
            } 
            // Логика для insertContact не нужна в EditViewModel
        }
    }
}
