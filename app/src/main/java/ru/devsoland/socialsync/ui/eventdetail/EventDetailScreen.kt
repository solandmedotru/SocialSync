package ru.devsoland.socialsync.ui.eventdetail

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color // Для полупрозрачного оверлея
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import ru.devsoland.socialsync.R
import ru.devsoland.socialsync.data.model.Event
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EventDetailScreen(
    navController: NavController,
    viewModel: EventDetailViewModel = hiltViewModel()
) {
    val contactData by viewModel.contact.collectAsState()
    val birthdayEvent by viewModel.birthdayEventFlow.collectAsState()
    val birthdayEventId by viewModel.birthdayEventId.collectAsState()

    // Состояния для диалога редактирования
    var showEditDialog by remember { mutableStateOf(false) }
    var editingGreetingIndex by remember { mutableStateOf<Int?>(null) }
    var editingGreetingText by remember { mutableStateOf("") }

    // Состояния для диалога подтверждения удаления
    var showDeleteConfirmDialog by remember { mutableStateOf(false) }
    var deletingGreetingIndex by remember { mutableStateOf<Int?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.event_detail_screen_title)) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.back_button_description)
                        )
                    }
                },
                actions = {
                    contactData?.let { unwrappedContact ->
                        IconButton(onClick = {
                            navController.navigate(ru.devsoland.socialsync.ui.AppDestinations.editContactRoute(unwrappedContact.id))
                        }) {
                            Icon(
                                imageVector = Icons.Filled.Edit,
                                contentDescription = stringResource(R.string.edit_contact_description)
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.primary
                ),
                windowInsets = WindowInsets(0.dp,0.dp,0.dp,0.dp)
            )
        }
    ) { innerPadding ->
        val currentContact = contactData
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
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(horizontal = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                item {
                    Spacer(modifier = Modifier.height(16.dp))
                    AsyncImage(
                        model = currentContact.photoUri, 
                        contentDescription = stringResource(
                            R.string.contact_photo_description,
                            currentContact.firstName ?: ""
                        ),
                        placeholder = painterResource(id = R.drawable.ic_contact_placeholder_avatar),
                        error = painterResource(id = R.drawable.ic_contact_placeholder_avatar),
                        modifier = Modifier
                            .size(120.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.secondaryContainer),
                        contentScale = ContentScale.Crop
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                }

                item {
                    Text(
                        text = "${currentContact.firstName ?: ""} ${currentContact.lastName ?: ""}".trim(),
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }

                item {
                    Text(
                        text = birthdayEvent?.eventType ?: stringResource(R.string.event_type_birthday),
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.secondary
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }

                currentContact.birthDate?.let {
                    item {
                        val displayText = remember(it, birthdayEvent?.date) { 
                            val dateToParse = birthdayEvent?.date?.toString() ?: it
                            try {
                                val date = LocalDate.parse(dateToParse, DateTimeFormatter.ISO_LOCAL_DATE)
                                date.format(DateTimeFormatter.ofPattern("d MMMM yyyy 'г.'", Locale("ru")))
                            } catch (e: Exception) {
                                it 
                            }
                        }
                        Text(
                            text = displayText,
                            style = MaterialTheme.typography.bodyLarge
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                }
                
                if (currentContact.tags.isNotEmpty()) {
                    item {
                        Text(
                            text = stringResource(R.string.tags_label) + currentContact.tags.joinToString(", "),
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                }

                val greetings = birthdayEvent?.generatedGreetings
                if (!greetings.isNullOrEmpty()) {
                    item {
                        Text(
                            text = stringResource(R.string.greetings_section_title),
                            style = MaterialTheme.typography.titleLarge,
                            modifier = Modifier.padding(vertical = 16.dp).fillMaxWidth(),
                            textAlign = TextAlign.Start
                        )
                    }
                    itemsIndexed(greetings) { index, greetingText ->
                        GreetingCard(
                            greetingNumber = index + 1,
                            greetingText = greetingText,
                            onEditClick = {
                                editingGreetingIndex = index
                                editingGreetingText = greetingText
                                showEditDialog = true
                            },
                            onDeleteClick = { 
                                deletingGreetingIndex = index
                                showDeleteConfirmDialog = true
                            }
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                    }
                }

                item {
                    Button(
                        onClick = {
                            val eventIdToPass = birthdayEventId
                            if (eventIdToPass != null && eventIdToPass != 0L) { 
                                navController.navigate(
                                    ru.devsoland.socialsync.ui.AppDestinations.aiGreetingPromptRoute(currentContact.id, eventIdToPass)
                                )
                            } else {
                                println("EventDetailScreen: Birthday event ID is null or 0, cannot navigate to AI prompt.")
                            }
                        },
                        modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp),
                        enabled = birthdayEventId != null && birthdayEventId != 0L 
                    ) {
                        Text(stringResource(R.string.generate_ai_greeting_button))
                    }
                }
            }
        }

        if (showEditDialog && editingGreetingIndex != null) {
            EditGreetingDialog(
                initialText = editingGreetingText,
                onDismissRequest = { showEditDialog = false },
                onConfirm = { newText ->
                    editingGreetingIndex?.let {
                        viewModel.updateGreeting(it, newText)
                    }
                    showEditDialog = false
                }
            )
        }

        if (showDeleteConfirmDialog && deletingGreetingIndex != null) {
            DeleteConfirmDialog(
                onDismissRequest = { showDeleteConfirmDialog = false },
                onConfirm = {
                    deletingGreetingIndex?.let {
                        viewModel.deleteGreeting(it)
                    }
                    showDeleteConfirmDialog = false
                }
            )
        }
    }
}

@Composable
fun GreetingCard(
    greetingNumber: Int,
    greetingText: String,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val backgroundImageRes = if (greetingNumber % 2 == 1) {
        R.drawable.congrats_bg_1
    } else {
        R.drawable.congrats_bg_2
    }

    Card(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Box {
            Image(
                painter = painterResource(id = backgroundImageRes),
                contentDescription = null, // Фон декоративный
                modifier = Modifier.matchParentSize(), // Растянуть на всю карточку
                contentScale = ContentScale.Crop // Обрезать, чтобы заполнить, сохраняя пропорции
            )
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .background(Color.Black.copy(alpha = 0.3f)) 
            )

            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "Поздравление $greetingNumber",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = greetingText,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White,
                    modifier = Modifier.fillMaxWidth() // Для многострочного текста
                )
                Spacer(modifier = Modifier.height(16.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onEditClick) {
                        Icon(
                            Icons.Filled.Edit, 
                            contentDescription = stringResource(R.string.edit_greeting_description),
                            tint = Color.White
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    IconButton(onClick = onDeleteClick) {
                        Icon(
                            Icons.Filled.Delete, 
                            contentDescription = stringResource(R.string.delete_greeting_description),
                            tint = Color.White
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun EditGreetingDialog(
    initialText: String,
    onDismissRequest: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var text by remember { mutableStateOf(initialText) }

    AlertDialog(
        onDismissRequest = onDismissRequest,
        title = {
            Text(text = stringResource(R.string.edit_greeting_dialog_title))
        },
        text = {
            OutlinedTextField(
                value = text,
                onValueChange = { text = it },
                label = { Text(stringResource(R.string.greeting_text_label)) },
                modifier = Modifier.fillMaxWidth()
            )
        },
        confirmButton = {
            Button(
                onClick = { 
                    onConfirm(text)
                }
            ) {
                Text(stringResource(R.string.save_button_label))
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismissRequest
            ) {
                Text(stringResource(R.string.cancel_button_label))
            }
        }
    )
}

@Composable
fun DeleteConfirmDialog(
    onDismissRequest: () -> Unit,
    onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismissRequest,
        title = {
            Text(text = stringResource(R.string.delete_greeting_confirm_title)) // Нужна новая строка
        },
        text = {
            Text(text = stringResource(R.string.delete_greeting_confirm_text)) // Нужна новая строка
        },
        confirmButton = {
            Button(
                onClick = onConfirm
            ) {
                Text(stringResource(R.string.delete_button_label)) // Нужна новая строка
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismissRequest
            ) {
                Text(stringResource(R.string.cancel_button_label)) // Эта строка уже есть
            }
        }
    )
}


// TODO: Добавить строки ресурсов в strings.xml:
// <string name="delete_greeting_confirm_title">Подтверждение удаления</string>
// <string name="delete_greeting_confirm_text">Вы уверены, что хотите удалить это поздравление?</string>
// <string name="delete_button_label">Удалить</string>

