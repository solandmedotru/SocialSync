package ru.devsoland.socialsync.ui.contacts

import android.Manifest
import android.content.pm.PackageManager
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Button
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import coil.compose.AsyncImage
import ru.devsoland.socialsync.R
import ru.devsoland.socialsync.data.model.Contact
import ru.devsoland.socialsync.ui.AppDestinations
import ru.devsoland.socialsync.ui.theme.SocialSyncTheme
import java.time.LocalDate
import java.time.MonthDay
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import java.time.temporal.ChronoUnit
import java.util.Locale

private const val DEBUG_TAG = "FormatBirthDate"

@Composable
fun formatBirthDateWithDaysUntil(birthDateString: String?): String {
    Log.d(DEBUG_TAG, "Input birthDateString: '$birthDateString'")
    if (birthDateString.isNullOrBlank()) {
        Log.d(DEBUG_TAG, "Date is null or blank, returning 'not specified'.")
        return stringResource(R.string.birth_date_not_specified)
    }

    val today = LocalDate.now()
    var parsedDate: LocalDate? = null
    var isYearKnown = true

    try {
        val fullDate = LocalDate.parse(birthDateString, DateTimeFormatter.ISO_LOCAL_DATE)
        Log.d(DEBUG_TAG, "Successfully parsed as full ISO_LOCAL_DATE: $fullDate")
        if (fullDate.year >= 1800) { 
            parsedDate = fullDate
            Log.d(DEBUG_TAG, "Year is known (>=1800). parsedDate: $parsedDate")
        } else { 
            parsedDate = LocalDate.of(today.year, fullDate.month, fullDate.dayOfMonth)
            isYearKnown = false
            Log.d(DEBUG_TAG, "Year is unknown or invalid (<1800), using current year. parsedDate: $parsedDate")
        }
    } catch (e: DateTimeParseException) {
        Log.d(DEBUG_TAG, "Failed to parse as full ISO_LOCAL_DATE. Error: ${e.message}. Trying --MM-DD format.")
        if (birthDateString.startsWith("--") && birthDateString.length == 7) {
            try {
                val monthDayPart = birthDateString.substring(2)
                val monthDay = MonthDay.parse(monthDayPart, DateTimeFormatter.ofPattern("MM-dd"))
                parsedDate = monthDay.atYear(today.year)
                isYearKnown = false
                Log.d(DEBUG_TAG, "Successfully parsed as --MM-DD. monthDay: $monthDay, parsedDate (current year): $parsedDate")
            } catch (e2: DateTimeParseException) {
                Log.d(DEBUG_TAG, "Failed to parse as --MM-DD. Error: ${e2.message}. Returning original string.")
                return birthDateString 
            }
        } else {
            Log.d(DEBUG_TAG, "Not YYYY-MM-DD and not starting with '--'. Returning original string.")
            return birthDateString 
        }
    }

    if (parsedDate == null) { // Should not happen if parsing was successful in one of the branches
        Log.d(DEBUG_TAG, "parsedDate is unexpectedly null after try-catch blocks. Returning original string.")
        return birthDateString
    }

    val birthDateFormatted = if (isYearKnown) {
        parsedDate.format(DateTimeFormatter.ofPattern("d MMMM yyyy 'г.'", Locale("ru")))
    } else {
        parsedDate.format(DateTimeFormatter.ofPattern("d MMMM", Locale("ru")))
    }
    Log.d(DEBUG_TAG, "Formatted birth date part: '$birthDateFormatted'")

    var nextBirthday = parsedDate.withYear(today.year)
    if (nextBirthday.isBefore(today)) {
        nextBirthday = nextBirthday.plusYears(1)
    }
    val daysUntil = ChronoUnit.DAYS.between(today, nextBirthday)
    Log.d(DEBUG_TAG, "Calculated nextBirthday: $nextBirthday, daysUntil: $daysUntil")

    val daysUntilString = when {
        daysUntil == 0L -> stringResource(R.string.birthday_today)
        daysUntil > 0L -> stringResource(R.string.birthday_in_days, daysUntil, getDaysWord(daysUntil))
        else -> "" // This case should ideally not be hit if nextBirthday logic is correct
    }
    Log.d(DEBUG_TAG, "Days until string part: '$daysUntilString'")

    val finalString = "$birthDateFormatted $daysUntilString".trim()
    Log.d(DEBUG_TAG, "Final formatted string: '$finalString'")
    return finalString
}

@Composable
fun getDaysWord(days: Long): String {
    val absDays = Math.abs(days)
    val lastDigit = absDays % 10
    val lastTwoDigits = absDays % 100
    // Логирование для getDaysWord, если нужно
    // Log.d(DEBUG_TAG, "getDaysWord input: $days") 
    val result = if (lastTwoDigits in 11L..19L) stringResource(R.string.days_word_plural_5_many)
    else when (lastDigit) {
        1L -> stringResource(R.string.days_word_singular_1)
        in 2L..4L -> stringResource(R.string.days_word_plural_2_4)
        else -> stringResource(R.string.days_word_plural_5_many)
    }
    // Log.d(DEBUG_TAG, "getDaysWord output: '$result'")
    return result
}

@OptIn(ExperimentalLayoutApi::class) 
@Composable
fun ContactListScreen(
    navController: NavController, 
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
            // TODO: Показать Snackbar или Toast с объяснением, почему разрешение важно
        }
    }

    Column(modifier = Modifier.fillMaxSize()) { 
        Button(
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
            Text(stringResource(R.string.load_contacts_from_device_button))
        }

        if (contacts.isEmpty()) {
            Box(
                modifier = Modifier
                    .weight(1f) 
                    .fillMaxWidth()
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = stringResource(R.string.contact_list_empty),
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Center
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f), 
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 16.dp)
            ) {
                items(contacts, key = { contact -> contact.id }) { contact ->
                    ContactListItem(
                        contact = contact,
                        onContactClick = {
                             navController.navigate(AppDestinations.eventDetailRoute(contact.id))
                        },
                        onEditClick = { 
                            navController.navigate(AppDestinations.editContactRoute(contact.id))
                        }
                    )
                    Divider()
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class) 
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
            contentDescription = stringResource(R.string.contact_photo_description, contact.firstName ?: ""),
            placeholder = painterResource(id = R.drawable.ic_contact_placeholder_avatar),
            error = painterResource(id = R.drawable.ic_contact_placeholder_avatar),
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.secondaryContainer),
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
            if (contact.tags.isNotEmpty()) {
                Spacer(modifier = Modifier.height(6.dp))
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    contact.tags.forEach { tag ->
                        Box(
                            modifier = Modifier
                                .border(
                                    width = 1.dp,
                                    color = MaterialTheme.colorScheme.outline,
                                    shape = RoundedCornerShape(8.dp)
                                )
                                .padding(horizontal = 6.dp, vertical = 3.dp)
                        ) {
                            Text(
                                text = tag,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.width(16.dp)) 

        IconButton(onClick = onEditClick) {
            Icon(
                imageVector = Icons.Filled.Edit,
                contentDescription = stringResource(R.string.edit_contact_description),
                tint = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Preview(showBackground = true)
@Composable
fun ContactListScreenPreview() {
    SocialSyncTheme {
        // ContactListScreen() 
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Preview(showBackground = true)
@Composable
fun ContactListItemPreview() {
    val sampleContactWithPhotoAndTags = Contact(
        id = 1, firstName = "Елена", lastName = "Петрова", birthDate = "1990-10-15",
        photoUri = "content://com.android.contacts/contacts/1/photo",
        tags = listOf("коллега", "подруга", "бегун", "книголюб", "путешественник")
    )
    val sampleContactNoPhoto = Contact(
        id = 2, firstName = "Игорь", lastName = "Максимов", birthDate = "--12-25",
        photoUri = null,
        tags = listOf("сосед")
    )
    val sampleContactNoTags = Contact(
        id = 3, firstName = "Анна", lastName = "Волкова", birthDate = "1985-03-30"
    )
    SocialSyncTheme {
        Column(Modifier.padding(16.dp)) {
            ContactListItem(contact = sampleContactWithPhotoAndTags, onContactClick = {}, onEditClick = {})
            Divider(Modifier.padding(vertical = 8.dp))
            ContactListItem(contact = sampleContactNoPhoto, onContactClick = {}, onEditClick = {})
            Divider(Modifier.padding(vertical = 8.dp))
            ContactListItem(contact = sampleContactNoTags, onContactClick = {}, onEditClick = {})
        }
    }
}
