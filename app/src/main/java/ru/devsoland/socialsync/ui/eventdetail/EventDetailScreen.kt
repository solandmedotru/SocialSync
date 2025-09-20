package ru.devsoland.socialsync.ui.eventdetail

import android.content.Intent // Импорт для Share Intent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Share // Импорт для иконки Share
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
import androidx.compose.ui.graphics.Color 
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext // Импорт для Share Intent
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import ru.devsoland.socialsync.R
import ru.devsoland.socialsync.ui.AppDestinations
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

    var showEditDialog by remember { mutableStateOf(false) }
    var editingGreetingIndex by remember { mutableStateOf<Int?>(null) }
    var editingGreetingText by remember { mutableStateOf("") }

    var showDeleteConfirmDialog by remember { mutableStateOf(false) }
    var deletingGreetingIndex by remember { mutableStateOf<Int?>(null) }

    val currentContact = contactData
    val context = LocalContext.current // Для Share Intent

    if (currentContact == null) {
        Box(
            modifier = Modifier
                .fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
    } else {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            item {
                Spacer(modifier = Modifier.height(24.dp)) 
                Box(contentAlignment = Alignment.Center) { 
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
                    IconButton(
                        onClick = { navController.navigate(AppDestinations.addEditContactRoute(currentContact.id)) },
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(4.dp) 
                            .background(
                                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f),
                                CircleShape
                            )
                            .size(36.dp) 
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Edit,
                            contentDescription = stringResource(R.string.edit_contact_description),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            item {
                Text(
                    text = "${currentContact.firstName ?: ""} ${currentContact.lastName ?: ""}".trim(),
                    style = MaterialTheme.typography.headlineSmall
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

            item {
                Button(
                    onClick = {
                        val eventIdToPass = birthdayEventId
                        if (eventIdToPass != null && eventIdToPass != 0L) { 
                            navController.navigate(
                                AppDestinations.aiGreetingPromptRoute(currentContact.id, eventIdToPass)
                            )
                        } else {
                            println("EventDetailScreen: Birthday event ID is null or 0, cannot navigate to AI prompt.")
                        }
                    },
                    modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp),
                    enabled = birthdayEventId != null && birthdayEventId != 0L, 
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
                ) {
                    Text(stringResource(R.string.create_with_ai_button)) 
                }
            }

            val greetings = birthdayEvent?.generatedGreetings
            if (!greetings.isNullOrEmpty()) {
                item {
                    Text(
                        text = stringResource(R.string.greetings_section_title),
                        style = MaterialTheme.typography.titleLarge,
                        modifier = Modifier.padding(top = 16.dp, bottom = 8.dp).fillMaxWidth(),
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
                        },
                        onShareClick = { // ИЗМЕНЕНО: Добавлена обработка Share
                            val sendIntent: Intent = Intent().apply {
                                action = Intent.ACTION_SEND
                                putExtra(Intent.EXTRA_TEXT, greetingText)
                                type = "text/plain"
                            }
                            val shareIntent = Intent.createChooser(sendIntent, null)
                            context.startActivity(shareIntent)
                        }
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                }
                 item { 
                    Spacer(modifier = Modifier.height(16.dp))
                }
            } else {
                 item {
                    Spacer(modifier = Modifier.height(16.dp))
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

@Composable
fun GreetingCard(
    greetingNumber: Int,
    greetingText: String,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit,
    onShareClick: () -> Unit, // ИЗМЕНЕНО: Добавлен параметр
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
                contentDescription = null, 
                modifier = Modifier.matchParentSize(), 
                contentScale = ContentScale.Crop 
            )
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .background(MaterialTheme.colorScheme.scrim.copy(alpha = 0.5f)) 
            )

            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "Поздравление $greetingNumber",
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White 
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = greetingText,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(16.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                    // horizontalArrangement = Arrangement.End, // Убрано для гибкого размещения
                ) {
                    IconButton(onClick = onShareClick) { // ИЗМЕНЕНО: Добавлена кнопка Share
                        Icon(
                            Icons.Filled.Share, 
                            contentDescription = stringResource(R.string.share_greeting_description), // Нужен новый строковый ресурс
                            tint = Color.White
                        )
                    }
                    Spacer(Modifier.weight(1f)) // Занимает все доступное пространство, отодвигая остальные кнопки вправо
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
            Text(text = stringResource(R.string.delete_greeting_dialog_title))
        },
        text = {
            Text(text = stringResource(R.string.delete_greeting_text))
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
            ) {
                Text(stringResource(R.string.delete_button_label))
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

/*
Нужно добавить строковый ресурс:
<string name="share_greeting_description">Поделиться поздравлением</string>
*/