package ru.devsoland.socialsync.ui.events

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.* 
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Divider // Оставляем, если используется
import androidx.compose.material3.MaterialTheme // Оставляем для стилей
import androidx.compose.material3.Text // Оставляем для текста
// ExperimentalMaterial3Api больше не нужен здесь, если TopAppBar удален
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.kizitonwose.calendar.compose.HorizontalCalendar
import com.kizitonwose.calendar.compose.rememberCalendarState
import com.kizitonwose.calendar.core.CalendarDay
import com.kizitonwose.calendar.core.CalendarMonth
import com.kizitonwose.calendar.core.DayPosition
import com.kizitonwose.calendar.core.daysOfWeek
import com.kizitonwose.calendar.core.firstDayOfWeekFromLocale
import ru.devsoland.socialsync.R
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.TextStyle
import java.util.Locale

// OptIn(ExperimentalMaterial3Api::class) // Больше не нужен здесь
@Composable
fun EventsScreen(
    viewModel: EventsViewModel = hiltViewModel()
) {
    val upcomingEvents by viewModel.upcomingEvents.collectAsStateWithLifecycle()

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

    // TopAppBar удален отсюда, управляется из MainActivity
    Column(modifier = Modifier.fillMaxSize()) {
        // Содержимое экрана ниже TopAppBar, который теперь в MainActivity
        MonthHeader(calendarMonth = calendarState.firstVisibleMonth)
        DaysOfWeekHeader(daysOfWeek = daysOfWeek(firstDayOfWeek = firstDayOfWeek))

        HorizontalCalendar(
            state = calendarState,
            modifier = Modifier.padding(horizontal = 16.dp),
            dayContent = { day ->
                DayView(day = day, isSelected = selectedDate == day.date) {
                    selectedDate = if (selectedDate == day.date) null else day.date
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
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 16.dp), // Добавляем отступ снизу
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(upcomingEvents, key = { it.contact.id }) { event ->
                    UpcomingEventItem(event = event)
                    Divider()
                }
            }
        }
    }
}

@Composable
fun DayView(day: CalendarDay, isSelected: Boolean, onClick: (CalendarDay) -> Unit) {
    Box(
        modifier = Modifier
            .aspectRatio(1f)
            .padding(1.dp)
            .background(
                color = if (isSelected) MaterialTheme.colorScheme.primaryContainer
                else if (day.position == DayPosition.MonthDate) MaterialTheme.colorScheme.surface
                else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
            )
            .clickable(enabled = day.position == DayPosition.MonthDate) {
                onClick(day)
            },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = day.date.dayOfMonth.toString(),
            color = if (day.position == DayPosition.MonthDate)
                        if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer
                        else MaterialTheme.colorScheme.onSurface
                    else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
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
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
fun UpcomingEventItem(event: UiUpcomingEvent) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
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
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
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
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold
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
