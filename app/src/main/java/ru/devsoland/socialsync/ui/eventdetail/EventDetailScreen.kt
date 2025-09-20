package ru.devsoland.socialsync.ui.eventdetail

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import ru.devsoland.socialsync.R
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import java.util.Locale

fun formatEventDate(dateString: String?): String {
    if (dateString.isNullOrBlank()) return ""
    return try {
        val localDate = LocalDate.parse(dateString, DateTimeFormatter.ISO_LOCAL_DATE)
        if (localDate.year < 1800) { 
            localDate.format(DateTimeFormatter.ofPattern("d MMMM", Locale("ru")))
        } else {
            localDate.format(DateTimeFormatter.ofPattern("d MMMM", Locale("ru")))
        }
    } catch (e: DateTimeParseException) {
        if (dateString.startsWith("--") && dateString.length == 7) {
            try {
                val monthDayPart = dateString.substring(2)
                val month = monthDayPart.substring(0,2).toInt()
                val day = monthDayPart.substring(3,5).toInt()
                LocalDate.of(LocalDate.now().year, month, day)
                    .format(DateTimeFormatter.ofPattern("d MMMM", Locale("ru")))
            } catch (e2: Exception) {
                dateString 
            }
        } else {
            dateString 
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EventDetailScreen(
    navController: NavController,
    viewModel: EventDetailViewModel = hiltViewModel()
) {
    val contact by viewModel.contact.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(id = R.string.event_detail_screen_title)) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.back_button_description)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.primary
                ),
                windowInsets = WindowInsets(0.dp, 0.dp, 0.dp, 0.dp)
            )
        }
    ) { innerPadding ->
        val currentContact = contact
        if (currentContact == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .verticalScroll(rememberScrollState()) // Для прокрутки, если контента много
                    .padding(horizontal = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(24.dp))

                AsyncImage(
                    model = currentContact.photoUri,
                    contentDescription = stringResource(R.string.contact_photo_description, currentContact.firstName ?: ""),
                    placeholder = painterResource(id = R.drawable.ic_contact_placeholder_avatar),
                    error = painterResource(id = R.drawable.ic_contact_placeholder_avatar),
                    modifier = Modifier
                        .size(120.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.secondaryContainer),
                    contentScale = ContentScale.Crop
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "${currentContact.firstName ?: ""} ${currentContact.lastName ?: ""}".trim(),
                    style = MaterialTheme.typography.headlineMedium, // Немного увеличил стиль
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = stringResource(R.string.event_type_birthday), 
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = formatEventDate(currentContact.birthDate),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(32.dp))

                Text(
                    text = stringResource(R.string.congratulations_section_title),
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Start // Заголовок секции обычно слева
                )
                Spacer(modifier = Modifier.height(16.dp))

                // Список карточек с поздравлениями (пока статический)
                CongratulationsCard(
                    title = stringResource(R.string.congratulation_1_title),
                    text = stringResource(R.string.congratulation_1_text),
                    backgroundImageRes = R.drawable.congrats_bg_1 // Нужны эти drawable
                )
                Spacer(modifier = Modifier.height(16.dp))
                CongratulationsCard(
                    title = stringResource(R.string.congratulation_2_title),
                    text = stringResource(R.string.congratulation_2_text),
                    backgroundImageRes = R.drawable.congrats_bg_2 // Нужны эти drawable
                )
                Spacer(modifier = Modifier.height(16.dp)) // Отступ снизу
            }
        }
    }
}

@Composable
fun CongratulationsCard(
    title: String,
    text: String,
    backgroundImageRes: Int
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = MaterialTheme.shapes.medium
    ) {
        Box(modifier = Modifier.height(180.dp)) { // Примерная высота карточки
            Image(
                painter = painterResource(id = backgroundImageRes),
                contentDescription = null, // Декоративное изображение
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
            // Градиент для лучшей читаемости текста
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(Color.Black.copy(alpha = 0.5f), Color.Transparent),
                            startY = Float.POSITIVE_INFINITY,
                            endY = 0f
                        )
                    )
            )
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.Bottom // Текст прижат к низу
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge.copy(color = Color.White, fontWeight = FontWeight.Bold, fontSize = 20.sp),
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = text,
                    style = MaterialTheme.typography.bodyMedium.copy(color = Color.White, fontSize = 14.sp),
                    maxLines = 3 // Ограничиваем количество строк
                )
            }
        }
    }
}

// Пожалуйста, добавьте эти строки в ваш strings.xml, если их еще нет:
// <string name="contact_photo_description">Фотография контакта %1$s</string>
// <string name="congratulations_section_title">Поздравления</string>
// <string name="congratulation_1_title">Поздравление 1</string>
// <string name="congratulation_1_text">Дорогая Елена, с днем рождения! Желаю счастья, здоровья и всех благ! Пусть каждый день будет приносить радость и успехи.</string>
// <string name="congratulation_2_title">Поздравление 2</string>
// <string name="congratulation_2_text">Елена, поздравляю с днем рождения! Желаю вам вдохновения, веселья и море солнечных дней. Пусть все ваши мечты сбываются!</string>

// Также вам понадобятся два drawable ресурса для фона карточек:
// R.drawable.congrats_bg_1 (например, картинка с тортом)
// R.drawable.congrats_bg_2 (например, картинка с шариками)
// Если у вас их нет, вы можете временно заменить их на Color.Gray или что-то подобное,
// либо Image(...) можно будет закомментировать для проверки остальной разметки.
