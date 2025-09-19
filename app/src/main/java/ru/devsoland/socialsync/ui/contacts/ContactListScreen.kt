package ru.devsoland.socialsync.ui.contacts

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource // Для плейсхолдера в AsyncImage
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage // <-- ИМПОРТ COIL
import ru.devsoland.socialsync.R
import ru.devsoland.socialsync.data.model.Contact
import ru.devsoland.socialsync.ui.theme.SocialSyncTheme
import java.time.LocalDate
import java.time.MonthDay
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import java.time.temporal.ChronoUnit
import java.util.Locale

// formatBirthDate и formatBirthDateWithDaysUntil, getDaysWord остаются без изменений

@Composable
fun formatBirthDateWithDaysUntil(birthDateString: String?): String {
    if (birthDateString.isNullOrBlank()) {
        return "Дата рождения не указана"
    }

    val today = LocalDate.now()
    var parsedDate: LocalDate? = null
    var isYearKnown = true

    try {
        val fullDate = LocalDate.parse(birthDateString, DateTimeFormatter.ISO_LOCAL_DATE)
        if (fullDate.year >= 1800) {
            parsedDate = fullDate
        } else {
            parsedDate = LocalDate.of(today.year, fullDate.month, fullDate.dayOfMonth)
            isYearKnown = false
        }
    } catch (e: DateTimeParseException) {
        if (birthDateString.startsWith("--") && birthDateString.length == 7) {
            try {
                val monthDayPart = birthDateString.substring(2)
                val monthDay = MonthDay.parse(monthDayPart, DateTimeFormatter.ofPattern("MM-dd"))
                parsedDate = monthDay.atYear(today.year)
                isYearKnown = false
            } catch (e2: DateTimeParseException) {
                return birthDateString
            }
        } else {
            return birthDateString
        }
    }

    if (parsedDate == null) return birthDateString

    val birthDateFormatted = if (isYearKnown) {
        parsedDate.format(DateTimeFormatter.ofPattern("d MMMM yyyy 'г.'", Locale("ru")))
    } else {
        parsedDate.format(DateTimeFormatter.ofPattern("d MMMM", Locale("ru")))
    }

    var nextBirthday = parsedDate.withYear(today.year)
    if (nextBirthday.isBefore(today)) {
        nextBirthday = nextBirthday.plusYears(1)
    }

    val daysUntil = ChronoUnit.DAYS.between(today, nextBirthday)

    val daysUntilString = when {
        daysUntil == 0L -> "(Сегодня!)"
        daysUntil > 0L -> "(через $daysUntil ${getDaysWord(daysUntil)})"
        else -> {
            val daysPassed = ChronoUnit.DAYS.between(nextBirthday.minusYears(1), today)
            "(прошел $daysPassed ${getDaysWord(daysPassed)} назад)"
        }
    }
    return "$birthDateFormatted $daysUntilString"
}

fun getDaysWord(days: Long): String {
    val absDays = Math.abs(days)
    val lastDigit = absDays % 10
    val lastTwoDigits = absDays % 100
    if (lastTwoDigits in 11..14) return "дней"
    return when (lastDigit) {
        1L -> "день"
        in 2..4 -> "дня"
        else -> "дней"
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContactListScreen(
    viewModel: ContactListViewModel = hiltViewModel()
) {
    val contacts by viewModel.contacts.collectAsStateWithLifecycle()
    val context = LocalContext.current

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            viewModel.loadContactsFromDevice()
        } else {
            // TODO: Показать Snackbar или Toast
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(id = R.string.contact_list_title)) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.primary
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            Button( // Кнопка осталась для удобства тестирования загрузки
                onClick = {
                    when (ContextCompat.checkSelfPermission(
                        context,
                        Manifest.permission.READ_CONTACTS
                    )) {
                        PackageManager.PERMISSION_GRANTED -> {
                            viewModel.loadContactsFromDevice()
                        }
                        else -> {
                            permissionLauncher.launch(Manifest.permission.READ_CONTACTS)
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Text("Загрузить контакты с устройства")
            }

            if (contacts.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize().padding(16.dp), contentAlignment = Alignment.Center) {
                    Text(
                        text = "Список контактов пуст. Нажмите кнопку выше, чтобы загрузить контакты.",
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    items(contacts, key = { contact -> contact.id }) { contact ->
                        ContactListItem(
                            contact = contact,
                            onContactClick = { /* TODO: Навигация */ },
                            onEditClick = { /* TODO: Навигация */ }
                        )
                        Divider()
                    }
                }
            }
        }
    }
}

@Composable
fun ContactListItem(
    contact: Contact,
    onContactClick: () -> Unit,
    onEditClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onContactClick() }
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AsyncImage(
            model = contact.photoUri,
            contentDescription = "Аватар контакта ${contact.firstName}",
            placeholder = painterResource(id = R.drawable.ic_contact_placeholder_avatar), // <-- ЗАГОТОВЛЕННЫЙ ПЛЕЙСХОЛДЕР
            error = painterResource(id = R.drawable.ic_contact_placeholder_avatar), // <-- ПЛЕЙСХОЛДЕР ПРИ ОШИБКЕ
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.secondaryContainer), // Фон для плейсхолдера, если фото не загрузится
            contentScale = ContentScale.Crop
        )

        Spacer(modifier = Modifier.width(16.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "${contact.firstName ?: ""} ${contact.lastName ?: ""}".trim(),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = formatBirthDateWithDaysUntil(contact.birthDate),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Spacer(modifier = Modifier.width(16.dp))

        IconButton(onClick = onEditClick) {
            Icon(
                imageVector = Icons.Filled.Edit,
                contentDescription = "Редактировать контакт",
                tint = MaterialTheme.colorScheme.primary
            )
        }
    }
}


@Preview(showBackground = true)
@Composable
fun ContactListScreenPreview() {
    SocialSyncTheme {
        ContactListScreen()
    }
}

@Preview(showBackground = true)
@Composable
fun ContactListItemPreview() {
    val sampleContactWithPhoto = Contact(
        id = 1, firstName = "Елена", lastName = "Петрова", birthDate = "1990-10-15",
        photoUri = "content://com.android.contacts/contacts/1/photo" // Пример URI, работать не будет в превью
    )
    val sampleContactNoPhoto = Contact(
        id = 2, firstName = "Игорь", lastName = "Максимов", birthDate = "--12-25",
        photoUri = null
    )
    SocialSyncTheme {
        Column {
            ContactListItem(contact = sampleContactWithPhoto, onContactClick = {}, onEditClick = {})
            Divider()
            ContactListItem(contact = sampleContactNoPhoto, onContactClick = {}, onEditClick = {})
        }
    }
}
