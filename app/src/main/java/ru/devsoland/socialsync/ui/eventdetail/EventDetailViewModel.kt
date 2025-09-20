package ru.devsoland.socialsync.ui.eventdetail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import ru.devsoland.socialsync.data.model.Contact
// Заменяем ContactRepository на SocialSyncRepository
import ru.devsoland.socialsync.data.repository.SocialSyncRepository 
import ru.devsoland.socialsync.ui.AppDestinations
import javax.inject.Inject

@HiltViewModel
class EventDetailViewModel @Inject constructor(
    // Заменяем ContactRepository на SocialSyncRepository
    private val socialSyncRepository: SocialSyncRepository, 
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val contactId: StateFlow<Long> = savedStateHandle.getStateFlow(AppDestinations.EVENT_DETAIL_CONTACT_ID_ARG, 0L)

    val contact: StateFlow<Contact?> = contactId.flatMapLatest { id ->
        // Заменяем getContactByIdStream на getContactById
        socialSyncRepository.getContactById(id) 
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = null
    )

    // TODO: Добавить логику для загрузки типа события и поздравлений, если они не являются частью объекта Contact
}
