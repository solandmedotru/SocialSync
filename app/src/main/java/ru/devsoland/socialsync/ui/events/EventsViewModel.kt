package ru.devsoland.socialsync.ui.events

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
// import android.util.Log // Для отладки, если понадобится
import ru.devsoland.socialsync.data.model.Contact
import ru.devsoland.socialsync.data.repository.SocialSyncRepository
import java.time.LocalDate
import java.time.MonthDay
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import java.time.temporal.ChronoUnit
import javax.inject.Inject

data class UiUpcomingEvent(
    val contact: Contact,
    val eventName: String,
    val dateText: String,
    val daysUntilText: String,
    val originalNextOccurrence: LocalDate
)

@HiltViewModel
class EventsViewModel @Inject constructor(
    private val repository: SocialSyncRepository
) : ViewModel() {

    private val _upcomingEvents = MutableStateFlow<List<UiUpcomingEvent>>(emptyList())
    val upcomingEvents: StateFlow<List<UiUpcomingEvent>> = _upcomingEvents.asStateFlow()

    init {
        loadUpcomingEvents()
    }

    private fun loadUpcomingEvents() {
        viewModelScope.launch {
            repository.getAllContacts()
                .map { contacts ->
                    val today = LocalDate.now()
                    contacts
                        .filter { !it.birthDate.isNullOrBlank() }
                        .mapNotNull { contact ->
                            parseBirthDateToEvent(contact, today)
                        }
                        .sortedBy { it.originalNextOccurrence }
                }
                .collect { events ->
                    _upcomingEvents.value = events
                }
        }
    }

    private fun parseBirthDateToEvent(contact: Contact, today: LocalDate): UiUpcomingEvent? {
        if (contact.birthDate.isNullOrBlank()) return null

        val birthDateString = contact.birthDate
        val parsedMonthDay: MonthDay? = try {
            // Попытка 1: Распарсить как полную дату YYYY-MM-DD (включая "0001-03-15")
            val fullDate = LocalDate.parse(birthDateString, DateTimeFormatter.ISO_LOCAL_DATE)
            MonthDay.from(fullDate)
        } catch (e: DateTimeParseException) {
            // Попытка 2: Если не получилось, попробовать как формат без года --MM-DD
            if (birthDateString.startsWith("--") && birthDateString.length == 7) {
                try {
                    val monthDayPart = birthDateString.substring(2) // Убираем "--"
                    MonthDay.parse(monthDayPart, DateTimeFormatter.ofPattern("MM-dd"))
                } catch (e2: DateTimeParseException) {
                    null // Ошибка парсинга части MM-dd
                }
            } else {
                null // Не соответствует формату --MM-DD
            }
        }

        if (parsedMonthDay == null) {
            // Log.d("EventsViewModel", "Не удалось распознать дату: $birthDateString для контакта ${contact.id}")
            return null
        }

        // Вычисляем следующее наступление события, используя parsedMonthDay и текущий год
        var nextOccurrence = parsedMonthDay.atYear(today.year)

        // Если эта дата уже прошла в текущем году, переносим на следующий год
        if (nextOccurrence.isBefore(today)) {
            nextOccurrence = nextOccurrence.plusYears(1)
        }
        // Примеры:
        // today = 2024-01-15
        // parsedMonthDay = 01-10 -> nextOccurrence = 2024-01-10. isBefore(today) -> true. nextOccurrence = 2025-01-10.
        // parsedMonthDay = 01-15 -> nextOccurrence = 2024-01-15. isBefore(today) -> false. nextOccurrence = 2024-01-15.
        // parsedMonthDay = 01-20 -> nextOccurrence = 2024-01-20. isBefore(today) -> false. nextOccurrence = 2024-01-20.

        val daysUntil = ChronoUnit.DAYS.between(today, nextOccurrence)
        val dateText = nextOccurrence.format(DateTimeFormatter.ofPattern("d MMMM"))

        val daysUntilText = when {
            daysUntil == 0L -> "(Сегодня!)"
            daysUntil == 1L -> "(Завтра!)"
            daysUntil > 0L -> "(через $daysUntil ${getDaysWord(daysUntil)})"
            else -> "" // Эта ветка не должна достигаться, если nextOccurrence всегда >= today
        }

        return UiUpcomingEvent(
            contact = contact,
            eventName = "День рождения",
            dateText = dateText,
            daysUntilText = daysUntilText,
            originalNextOccurrence = nextOccurrence // Это дата для сортировки
        )
    }

    private fun getDaysWord(days: Long): String {
        val absDays = Math.abs(days)
        val lastDigit = absDays % 10
        val lastTwoDigits = absDays % 100

        if (lastTwoDigits in 11L..19L) return "дней" // 11-19 дней
        return when (lastDigit) {
            1L -> "день"    // 1 день, 21 день, 31 день ...
            in 2L..4L -> "дня" // 2-4 дня, 22-24 дня ...
            else -> "дней"    // 0, 5-9 дней, 10 дней ...
        }
    }
}
