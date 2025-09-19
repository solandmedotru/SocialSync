package ru.devsoland.socialsync.ui.contacts

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
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
            started = SharingStarted.WhileSubscribed(5000), // Начинать сбор, когда есть подписчики, с задержкой остановки
            initialValue = emptyList() // Начальное значение - пустой список
        )

    // В будущем здесь могут быть методы для добавления, удаления, поиска контактов,
    // обработки UI-событий и т.д.
}