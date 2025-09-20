package ru.devsoland.socialsync.ui.events

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border // Импорт для Modifier.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.* 
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed 
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.kizitonwose.calendar.compose.HorizontalCalendar
import com.kizitonwose.calendar.compose.rememberCalendarState
import com.kizitonwose.calendar.core.CalendarDay
import com.kizitonwose.calendar.core.CalendarMonth
import com.kizitonwose.calendar.core.DayPosition
import com.kizitonwose.calendar.core.daysOfWeek
import com.kizitonwose.calendar.core.firstDayOfWeekFromLocale
import ru.devsoland.socialsync.R
import ru.devsoland.socialsync.ui.AppDestinations
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.MonthDay
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import java.time.format.TextStyle
import java.util.Locale

fun parseContactBirthDateToMonthDay(birthDateString: String?): MonthDay? {
    if (birthDateString.isNullOrBlank()) return null
    return try {
        val fullDate = LocalDate.parse(birthDateString, DateTimeFormatter.ISO_LOCAL_DATE)
        MonthDay.from(fullDate)
    } catch (e: DateTimeParseException) {
        if (birthDateString.startsWith("--") && birthDateString.length == 7) {
            try {
                val monthDayPart = birthDateString.substring(2) 
                MonthDay.parse(monthDayPart, DateTimeFormatter.ofPattern("MM-dd"))
            } catch (e2: DateTimeParseException) {
                null
            }
        } else {
            null
        }
    }
}

@Composable
fun EventsScreen(
    navController: NavController, 
    viewModel: EventsViewModel = hiltViewModel()
) {
    val upcomingEvents by viewModel.upcomingEvents.collectAsStateWithLifecycle()
    val allBirthMonthDays by viewModel.allBirthMonthDays.collectAsStateWithLifecycle()

    val currentMonth = remember { YearMonth.now() }
    val startMonth = remember { currentMonth.minusMonths(100) }
    val endMonth = remember { currentMonth.plusMonths(100) }
    val firstDayOfWeek = remember { firstDayOfWeekFromLocale() }

    val calendarState = rememberCalendarState(
        startMonth = startMonth,
        endMonth = endMonth,
        firstVisibleMonth = currentMonth,
        firstDayOfWeek = firstDayOfWeek
    )

    var selectedDate by remember { mutableStateOf<LocalDate?>(null) }

    Column(modifier = Modifier.fillMaxSize()) {
        MonthHeader(calendarMonth = calendarState.firstVisibleMonth)
        DaysOfWeekHeader(daysOfWeek = daysOfWeek(firstDayOfWeek = firstDayOfWeek))

        HorizontalCalendar(
            state = calendarState,
            modifier = Modifier.padding(horizontal = 16.dp),
            dayContent = { day ->
                val dayMonthDay = MonthDay.from(day.date)
                val hasEvent = allBirthMonthDays.contains(dayMonthDay)

                DayView(
                    day = day, 
                    isSelected = selectedDate == day.date, 
                    hasEvent = hasEvent
                ) {
                    selectedDate = if (selectedDate == day.date) null else day.date
                    if (hasEvent && day.position == DayPosition.MonthDate) { 
                        val eventForDay = upcomingEvents.find { uiEvent ->
                            val contactMonthDay = parseContactBirthDateToMonthDay(uiEvent.contact.birthDate)
                            contactMonthDay == dayMonthDay
                        }
                        eventForDay?.let {
                            navController.navigate(AppDestinations.eventDetailRoute(it.contact.id))
                        }
                    }
                }
            }
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = stringResource(R.string.upcoming_events_title),
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 8.dp)
        )

        if (upcomingEvents.isEmpty()) {
            Box(
                modifier = Modifier
                    .weight(1f) 
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(stringResource(R.string.no_upcoming_events))
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f), 
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp) 
            ) {
                itemsIndexed(upcomingEvents, key = { _, event -> event.contact.id }) { index, event ->
                    AnimatedVisibility(
                        visible = true, 
                        enter = slideInVertically(
                            initialOffsetY = { it / 2 },
                            animationSpec = tween(durationMillis = 300, delayMillis = index * 50)
                        ) + fadeIn(animationSpec = tween(durationMillis = 300, delayMillis = index * 50)),
                        exit = slideOutVertically(targetOffsetY = { -it / 2 }) + fadeOut(animationSpec = tween(durationMillis = 150))
                    ) {
                        Column { 
                            UpcomingEventItem(
                                event = event,
                                onClick = {
                                    navController.navigate(AppDestinations.eventDetailRoute(event.contact.id))
                                }
                            )
                            HorizontalDivider() 
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DayView(
    day: CalendarDay, 
    isSelected: Boolean, 
    hasEvent: Boolean, 
    onClick: (CalendarDay) -> Unit
) {
    val today = LocalDate.now()
    val isToday = day.date == today && day.position == DayPosition.MonthDate

    val backgroundColor = when {
        isSelected -> MaterialTheme.colorScheme.primaryContainer
        hasEvent && day.position == DayPosition.MonthDate -> MaterialTheme.colorScheme.tertiaryContainer
        // Если "сегодня" и не выбрано и не имеет события, можно задать другой фон, но рамка лучше
        // isToday && !isSelected && !hasEvent -> MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f) 
        day.position == DayPosition.MonthDate -> MaterialTheme.colorScheme.surface
        else -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
    }

    val textColor = when {
        isSelected -> MaterialTheme.colorScheme.onPrimaryContainer
        hasEvent && day.position == DayPosition.MonthDate -> MaterialTheme.colorScheme.onTertiaryContainer
        isToday && !isSelected -> MaterialTheme.colorScheme.primary // Можно выделить текст сегодняшней даты, если она не выбрана
        day.position == DayPosition.MonthDate -> MaterialTheme.colorScheme.onSurface
        else -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
    }

    var dayModifier = Modifier
        .aspectRatio(1f) 
        .padding(1.dp) 
        .background(color = backgroundColor)
    
    if (isToday) {
        dayModifier = dayModifier.border(2.dp, MaterialTheme.colorScheme.primary)
    }

    dayModifier = dayModifier.clickable(enabled = day.position == DayPosition.MonthDate) {
        onClick(day)
    }

    Box(
        modifier = dayModifier,
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = day.date.dayOfMonth.toString(),
            color = textColor 
        )
    }
}

@Composable
fun MonthHeader(calendarMonth: CalendarMonth) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp, horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = "${calendarMonth.yearMonth.month.getDisplayName(TextStyle.FULL, Locale.getDefault())} ${calendarMonth.yearMonth.year}",
            style = MaterialTheme.typography.titleMedium,
            textAlign = TextAlign.Center,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
fun DaysOfWeekHeader(daysOfWeek: List<DayOfWeek>) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp, horizontal = 16.dp)
    ) {
        for (dayOfWeek in daysOfWeek) {
            Text(
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.Center,
                text = dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.getDefault()),
                style = MaterialTheme.typography.bodySmall 
            )
        }
    }
}

@Composable
fun UpcomingEventItem(
    event: UiUpcomingEvent,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() } 
            .padding(vertical = 8.dp) 
        ,verticalAlignment = Alignment.CenterVertically
    ) {
        AsyncImage(
            model = event.contact.photoUri,
            contentDescription = "Аватар контакта ${event.contact.firstName}",
            placeholder = painterResource(id = R.drawable.ic_contact_placeholder_avatar),
            error = painterResource(id = R.drawable.ic_contact_placeholder_avatar),
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.secondaryContainer),
            contentScale = androidx.compose.ui.layout.ContentScale.Crop
        )

        Spacer(modifier = Modifier.width(16.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "${event.contact.firstName} ${event.contact.lastName}".trim(),
                style = MaterialTheme.typography.titleMedium 
            )
            Text(
                text = event.eventName,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Spacer(modifier = Modifier.width(8.dp))

        Column(horizontalAlignment = Alignment.End) {
            Text(
                text = event.dateText,
                style = MaterialTheme.typography.bodyMedium 
            )
            if (event.daysUntilText.isNotBlank()) {
                Text(
                    text = event.daysUntilText,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}
