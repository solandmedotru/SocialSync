package ru.devsoland.socialsync.ui.contacts

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import ru.devsoland.socialsync.R
import java.time.LocalDate
import java.time.MonthDay
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import java.util.Locale

// Вспомогательная функция для форматирования даты рождения
@Composable
private fun formatBirthDate(birthDateString: String?): String {
    if (birthDateString.isNullOrBlank()) {
        return "не указана"
    }
    return try {
        // Попробовать распарсить как полную дату (YYYY-MM-DD)
        val fullDate = LocalDate.parse(birthDateString, DateTimeFormatter.ISO_LOCAL_DATE) // ISO_LOCAL_DATE это "yyyy-MM-dd"
        
        // Проверяем год: если он "нереальный" (например, < 1800), форматируем как дату без года
        if (fullDate.year < 1800) {
            "${fullDate.format(DateTimeFormatter.ofPattern("dd.MM", Locale.getDefault()))}.-" // <--- НОВАЯ ЛОГИКА ЗДЕСЬ
        } else {
            fullDate.format(DateTimeFormatter.ofPattern("dd.MM.yyyy", Locale.getDefault()))
        }
    } catch (e: DateTimeParseException) {
        // Если не удалось распарсить как полную дату, попробовать как дату без года (например, "--MM-DD")
        if (birthDateString.startsWith("--") && birthDateString.length == 7) { // Простая проверка формата --MM-DD
            try {
                val monthDayPart = birthDateString.substring(2) // Извлекаем "MM-DD"
                val monthDay = MonthDay.parse(monthDayPart, DateTimeFormatter.ofPattern("MM-dd", Locale.getDefault()))
                "${monthDay.format(DateTimeFormatter.ofPattern("dd.MM", Locale.getDefault()))}.-"
            } catch (e2: DateTimeParseException) {
                birthDateString // Не удалось распарсить и как MM-dd, вернуть исходную строку
            }
        } else {
            birthDateString // Неизвестный формат, вернуть исходную строку
        }
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
            println("Разрешение READ_CONTACTS получено")
            viewModel.loadContactsFromDevice()
        } else {
            println("В разрешении READ_CONTACTS отказано")
            // TODO: Показать Snackbar или Toast
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(id = R.string.app_name)) },
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
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Button(
                onClick = {
                    when (ContextCompat.checkSelfPermission(
                        context,
                        Manifest.permission.READ_CONTACTS
                    )) {
                        PackageManager.PERMISSION_GRANTED -> {
                            println("Разрешение READ_CONTACTS уже было предоставлено")
                            viewModel.loadContactsFromDevice()
                        }
                        else -> {
                            permissionLauncher.launch(Manifest.permission.READ_CONTACTS)
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Загрузить контакты с устройства")
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (contacts.isEmpty()) {
                Text(
                    text = "Список контактов пуст. Нажмите кнопку выше, чтобы загрузить контакты с вашего устройства.",
                    modifier = Modifier.padding(16.dp),
                    style = MaterialTheme.typography.bodyLarge
                )
            } else {
                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    items(contacts) { contact ->
                        Column(
                            modifier = Modifier
                                .fillParentMaxWidth()
                                .padding(vertical = 8.dp)
                        ) {
                            Text(
                                text = "${contact.firstName} ${contact.lastName}",
                                style = MaterialTheme.typography.titleMedium
                            )
                            contact.phoneNumber?.let { phoneNumber ->
                                Text(
                                    text = "Телефон: $phoneNumber",
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                            // Используем нашу вспомогательную функцию для форматирования
                            val formattedBirthDate = formatBirthDate(contact.birthDate)
                            Text(
                                text = "Дата рождения: $formattedBirthDate",
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }
            }
        }
    }
}
