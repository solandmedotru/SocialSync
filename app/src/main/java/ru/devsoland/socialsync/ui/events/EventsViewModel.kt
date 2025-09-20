package ru.devsoland.socialsync.ui.events

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
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

    private val _allBirthMonthDays = MutableStateFlow<Set<MonthDay>>(emptySet())
    val allBirthMonthDays: StateFlow<Set<MonthDay>> = _allBirthMonthDays.asStateFlow()

    init {
        loadEventData()
    }

    private fun loadEventData() {
        viewModelScope.launch {
            repository.getAllContacts()
                .map { contacts ->
                    val today = LocalDate.now()
                    val birthMonthDays = mutableSetOf<MonthDay>()
                    val upcoming = contacts
                        .filter { !it.birthDate.isNullOrBlank() }
                        .mapNotNull { contact ->
                            val parsedEventData = parseBirthDate(contact.birthDate, today)
                            if (parsedEventData?.monthDay != null) {
                                birthMonthDays.add(parsedEventData.monthDay)
                            }
                            // Создаем UiUpcomingEvent, если дата рождения валидна
                            parsedEventData?.nextOccurrence?.let {
                                val daysUntil = ChronoUnit.DAYS.between(today, it)
                                val dateText = it.format(DateTimeFormatter.ofPattern("d MMMM"))
                                val daysUntilText = when {
                                    daysUntil == 0L -> "(Сегодня!)"
                                    daysUntil == 1L -> "(Завтра!)"
                                    daysUntil > 0L -> "(через $daysUntil ${getDaysWord(daysUntil)})"
                                    else -> ""
                                }
                                UiUpcomingEvent(
                                    contact = contact,
                                    eventName = "День рождения",
                                    dateText = dateText,
                                    daysUntilText = daysUntilText,
                                    originalNextOccurrence = it
                                )
                            }
                        }
                        .sortedBy { it.originalNextOccurrence }
                    Pair(upcoming, birthMonthDays) // Возвращаем пару: список ближайших и набор MonthDay
                }
                .collect { (upcoming, monthDays) ->
                    _upcomingEvents.value = upcoming
                    _allBirthMonthDays.value = monthDays
                }
        }
    }

    // Вспомогательная data class для возврата из parseBirthDate
    private data class ParsedEventDetails(val monthDay: MonthDay, val nextOccurrence: LocalDate)

    // Обновленная функция парсинга, возвращает MonthDay и следующую дату наступления
    private fun parseBirthDate(birthDateString: String?, today: LocalDate): ParsedEventDetails? {
        if (birthDateString.isNullOrBlank()) return null

        val parsedMonthDay: MonthDay? = try {
            val fullDate = LocalDate.parse(birthDateString, DateTimeFormatter.ISO_LOCAL_DATE)
            MonthDay.from(fullDate)
        } catch (e: DateTimeParseException) {
            if (birthDateString.startsWith("--") && birthDateString.length == 7) {
                try {
                    val monthDayPart = birthDateString.substring(2)
                    MonthDay.parse(monthDayPart, DateTimeFormatter.ofPattern("MM-dd"))
                } catch (e2: DateTimeParseException) { null }
            } else { null }
        }

        if (parsedMonthDay == null) return null

        var nextOccurrence = parsedMonthDay.atYear(today.year)
        if (nextOccurrence.isBefore(today)) {
            nextOccurrence = nextOccurrence.plusYears(1)
        }
        return ParsedEventDetails(parsedMonthDay, nextOccurrence)
    }

    private fun getDaysWord(days: Long): String {
        val absDays = Math.abs(days)
        val lastDigit = absDays % 10
        val lastTwoDigits = absDays % 100

        if (lastTwoDigits in 11L..19L) return "дней"
        return when (lastDigit) {
            1L -> "день"
            in 2L..4L -> "дня"
            else -> "дней"
        }
    }
}
