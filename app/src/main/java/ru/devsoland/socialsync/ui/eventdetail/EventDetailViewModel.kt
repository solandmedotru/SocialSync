package ru.devsoland.socialsync.ui.eventdetail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import ru.devsoland.socialsync.data.model.Contact
import ru.devsoland.socialsync.data.model.Event 
import ru.devsoland.socialsync.data.repository.SocialSyncRepository
import ru.devsoland.socialsync.ui.AppDestinations
import javax.inject.Inject
import kotlinx.coroutines.flow.flowOf

const val BIRTHDAY_EVENT_TYPE_STRING = "День рождения"

@HiltViewModel
class EventDetailViewModel @Inject constructor(
    private val socialSyncRepository: SocialSyncRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val contactIdFlow: StateFlow<Long> = savedStateHandle.getStateFlow(AppDestinations.EVENT_DETAIL_CONTACT_ID_ARG, 0L)

    val contact: StateFlow<Contact?> = contactIdFlow.flatMapLatest { id ->
        println("EventDetailViewModel: contact flatMapLatest called with contactId: $id")
        if (id != 0L) {
            socialSyncRepository.getContactById(id)
        } else {
            flowOf(null)
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = null
    )

    val birthdayEventId: StateFlow<Long?> = contactIdFlow.flatMapLatest { contactIdVal ->
        println("EventDetailViewModel: birthdayEventId flatMapLatest for contactId: $contactIdVal")
        if (contactIdVal != 0L) {
            socialSyncRepository.getEventByContactIdAndType(contactIdVal, BIRTHDAY_EVENT_TYPE_STRING)
                .map { event ->
                    println("EventDetailViewModel: Fetched event for birthday ID check: $event")
                    event?.id
                }
        } else {
            println("EventDetailViewModel: contactIdVal is 0, returning flowOf(null) for birthdayEventId")
            flowOf(null) 
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = null.also { println("EventDetailViewModel: birthdayEventId initialValue set to null") }
    )

    @OptIn(ExperimentalCoroutinesApi::class)
    val birthdayEventFlow: StateFlow<Event?> = birthdayEventId.flatMapLatest { eventId ->
        if (eventId != null && eventId != 0L) {
            println("EventDetailViewModel: birthdayEventId is $eventId, fetching full event object.")
            socialSyncRepository.getEventById(eventId) 
        } else {
            println("EventDetailViewModel: birthdayEventId is null or 0, birthdayEventFlow will be null.")
            flowOf(null) 
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = null
    )

    fun deleteGreeting(index: Int) {
        viewModelScope.launch {
            val currentEvent = birthdayEventFlow.value
            if (currentEvent != null && currentEvent.generatedGreetings != null && index >= 0 && index < currentEvent.generatedGreetings.size) {
                val updatedGreetings = currentEvent.generatedGreetings.toMutableList()
                updatedGreetings.removeAt(index)
                val updatedEvent = currentEvent.copy(generatedGreetings = updatedGreetings)
                socialSyncRepository.updateEvent(updatedEvent)
                println("EventDetailViewModel: Deleted greeting at index $index. New count: ${updatedGreetings.size}")
            } else {
                println("EventDetailViewModel: Could not delete greeting. Event or greetings list is null, or index is out of bounds.")
            }
        }
    }

    fun updateGreeting(index: Int, newText: String) {
        viewModelScope.launch {
            val currentEvent = birthdayEventFlow.value
            if (currentEvent != null && currentEvent.generatedGreetings != null && index >= 0 && index < currentEvent.generatedGreetings.size) {
                val updatedGreetings = currentEvent.generatedGreetings.toMutableList()
                updatedGreetings[index] = newText
                val updatedEvent = currentEvent.copy(generatedGreetings = updatedGreetings)
                socialSyncRepository.updateEvent(updatedEvent)
                println("EventDetailViewModel: Updated greeting at index $index with text: '$newText'")
            } else {
                println("EventDetailViewModel: Could not update greeting. Event or greetings list is null, or index is out of bounds.")
            }
        }
    }


    init {
        viewModelScope.launch { 
            contactIdFlow.collect { id ->
                println("EventDetailViewModel: contactIdFlow emitted: $id")
            }
        }
        viewModelScope.launch {
            birthdayEventFlow.collect { event ->
                println("EventDetailViewModel: birthdayEventFlow collected: ${event?.name}, Greetings: ${event?.generatedGreetings?.size}")
            }
        }
         viewModelScope.launch {
            contact.collect { c ->
                println("EventDetailViewModel: contact collected: ${c?.firstName}")
            }
        }
        viewModelScope.launch {
            birthdayEventId.collect { id ->
                println("EventDetailViewModel: birthdayEventId collected: $id")
            }
        }
    }
}
