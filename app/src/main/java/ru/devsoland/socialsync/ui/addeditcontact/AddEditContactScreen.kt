package ru.devsoland.socialsync.ui.addeditcontact

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer // Оставляем для специфичных случаев, если понадобятся
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height // Оставляем для специфичных случаев
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width // Оставляем для специфичных случаев
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DateRange // Для иконки календаря
import androidx.compose.material.icons.filled.Done
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DividerDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.InputChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import kotlinx.coroutines.flow.collectLatest
import ru.devsoland.socialsync.R
import ru.devsoland.socialsync.ui.AppDestinations
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun AddEditContactScreen(
    navController: NavController, 
    viewModel: AddEditContactViewModel = hiltViewModel()
) {
    val keyboardController = LocalSoftwareKeyboardController.current

    val firstName by viewModel.firstName.collectAsState()
    val lastName by viewModel.lastName.collectAsState()
    val phoneNumber by viewModel.phoneNumber.collectAsState()
    val birthDate by viewModel.birthDate.collectAsState()
    val originalContact by viewModel.contactState.collectAsState()

    val currentTags by viewModel.tags.collectAsState()
    val suggestedTags = viewModel.suggestedTags
    var customTagInput by remember { mutableStateOf("") }

    val contactId = viewModel.currentContactId
    val isLoadingExistingContact = contactId != AppDestinations.DEFAULT_NEW_CONTACT_ID && originalContact == null

    LaunchedEffect(key1 = Unit) {
        viewModel.saveSuccessEvent.collectLatest {
            navController.popBackStack()
        }
    }

    var showDatePicker by remember { mutableStateOf(false) }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = { viewModel.saveContact() }) {
                Icon(
                    imageVector = Icons.Filled.Done,
                    contentDescription = stringResource(R.string.save_contact_description)
                )
            }
        }
    ) { innerPadding -> 
        if (isLoadingExistingContact) {
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
                    .padding(horizontal = 16.dp) 
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp) // ИЗМЕНЕНО: Добавлено spacedBy, Spacer будут удалены ниже
            ) {
                // Spacer(modifier = Modifier.height(16.dp)) // УДАЛЕНО (первый отступ не нужен с spacedBy, если только не для отодвигания от TopAppBar)
                // Если нужен отступ от TopAppBar, можно добавить Spacer(modifier = Modifier.height(0.dp)) или оставить Column без verticalArrangement вверху
                // и добавить Spacer(16.dp) здесь. Или просто положиться на innerPadding.
                // Решено оставить без начального Spacer, т.к. innerPadding + padding(horizontal=16) уже есть.

                OutlinedTextField(
                    value = firstName,
                    onValueChange = { viewModel.firstName.value = it },
                    label = { Text(stringResource(R.string.first_name_label)) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                // Spacer(modifier = Modifier.height(12.dp)) // УДАЛЕНО
                OutlinedTextField(
                    value = lastName,
                    onValueChange = { viewModel.lastName.value = it },
                    label = { Text(stringResource(R.string.last_name_label)) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                // Spacer(modifier = Modifier.height(12.dp)) // УДАЛЕНО

                OutlinedTextField(
                    value = phoneNumber, 
                    onValueChange = { newRawNumber ->
                        val digitsOnly = newRawNumber.filter { it.isDigit() }
                        viewModel.phoneNumber.value = digitsOnly.take(10)
                    },
                    label = { Text(stringResource(R.string.phone_number_label)) },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    singleLine = true,
                    visualTransformation = PhoneNumberVisualTransformation()
                )
                // Spacer(modifier = Modifier.height(12.dp)) // УДАЛЕНО

                OutlinedTextField(
                    value = birthDate ?: "",
                    onValueChange = { /* Изменения только через DatePicker */ },
                    readOnly = true,
                    label = { Text(stringResource(R.string.birth_date_label)) },
                    placeholder = { Text(stringResource(R.string.birth_date_placeholder)) }, 
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showDatePicker = true },
                    trailingIcon = {
                        IconButton(onClick = { showDatePicker = true }) {
                            Icon(
                                Icons.Filled.DateRange,
                                contentDescription = stringResource(id = R.string.select_date_description)
                            )
                        }
                    },
                    singleLine = true
                )

                // Spacer(modifier = Modifier.height(24.dp)) // УДАЛЕНО (будет 16.dp из-за spacedBy)
                HorizontalDivider(Modifier, DividerDefaults.Thickness, DividerDefaults.color) // Разделитель останется с отступом 16.dp сверху и снизу
                // Spacer(modifier = Modifier.height(16.dp)) // УДАЛЕНО

                Text(
                    stringResource(R.string.tags_section_title),
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.fillMaxWidth()
                )
                // Spacer(modifier = Modifier.height(8.dp)) // УДАЛЕНО

                Row(verticalAlignment = Alignment.CenterVertically) { // Этот Row также получит отступ 16dp от предыдущего Text
                    OutlinedTextField(
                        value = customTagInput,
                        onValueChange = { customTagInput = it },
                        label = { Text(stringResource(R.string.add_tag_label)) },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                        keyboardActions = KeyboardActions(onDone = {
                            viewModel.addTag(customTagInput)
                            customTagInput = ""
                            keyboardController?.hide()
                        })
                    )
                    Spacer(modifier = Modifier.width(8.dp)) // Внутренний Spacer в Row остается
                    IconButton(onClick = {
                        viewModel.addTag(customTagInput)
                        customTagInput = ""
                        keyboardController?.hide()
                    }, enabled = customTagInput.isNotBlank()) {
                        Icon(Icons.Filled.AddCircle, contentDescription = stringResource(R.string.add_tag_button_description))
                    }
                }
                // Spacer(modifier = Modifier.height(8.dp)) // УДАЛЕНО

                if (currentTags.isNotEmpty()) {
                    Text(
                        stringResource(R.string.current_tags_label),
                        style = MaterialTheme.typography.titleSmall,
                        modifier = Modifier.fillMaxWidth()
                    )
                    FlowRow(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), // Этот FlowRow получит 16dp сверху, но его собственный padding(vertical=4.dp) будет применен.
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        currentTags.forEach { tag ->
                            InputChip(
                                selected = false,
                                onClick = { /* Клик по чипу не делает ничего */ },
                                label = { Text(tag) },
                                trailingIcon = {
                                    IconButton(onClick = { viewModel.removeTag(tag) }, modifier = Modifier.size(18.dp)) {
                                        Icon(Icons.Filled.Close, contentDescription = stringResource(R.string.remove_tag_description, tag))
                                    }
                                }
                            )
                        }
                    }
                    // Spacer(modifier = Modifier.height(12.dp)) // УДАЛЕНО
                }

                Text(
                    stringResource(R.string.suggested_tags_label),
                    style = MaterialTheme.typography.titleSmall,
                    modifier = Modifier.fillMaxWidth()
                )
                FlowRow(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), // Аналогично предыдущему FlowRow
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    suggestedTags.forEach { tag ->
                        if (!currentTags.contains(tag)) {
                            InputChip(
                                selected = false,
                                onClick = { viewModel.addTag(tag) },
                                label = { Text(tag) }
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(80.dp)) // Этот Spacer в конце для FAB лучше оставить, т.к. он специфичен
            }
        }
    }

    if (showDatePicker) {
        val calendar = Calendar.getInstance()
        var initialSelectedDateMillis: Long? = null
        birthDate?.let {
            if (it.matches(Regex("^\\d{4}-\\d{2}-\\d{2}$"))) {
                try {
                    val parts = it.split("-")
                    calendar.set(parts[0].toInt(), parts[1].toInt() - 1, parts[2].toInt())
                    initialSelectedDateMillis = calendar.timeInMillis
                } catch (e: Exception) {
                    // Keep initialSelectedDateMillis null, DatePicker uses current date
                }
            }
        }

        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = initialSelectedDateMillis,
            yearRange = IntRange(1900, Calendar.getInstance().get(Calendar.YEAR))
        )

        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let { millis ->
                            val selectedCalendar = Calendar.getInstance().apply { timeInMillis = millis }
                            val year = selectedCalendar.get(Calendar.YEAR)
                            val month = selectedCalendar.get(Calendar.MONTH) + 1
                            val day = selectedCalendar.get(Calendar.DAY_OF_MONTH)
                            viewModel.birthDate.value = String.format("%04d-%02d-%02d", year, month, day)
                        }
                        showDatePicker = false
                    }
                ) { Text(stringResource(id = R.string.dialog_ok)) }
            },
            dismissButton = {
                TextButton(
                    onClick = { showDatePicker = false }
                ) { Text(stringResource(id = R.string.dialog_cancel)) }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}

class PhoneNumberVisualTransformation : VisualTransformation {
    override fun filter(text: AnnotatedString): TransformedText {
        val inputText = text.text.filter { it.isDigit() }.take(10)
        val maskedNumber = buildString {
            append("+7 (")
            for (i in 0 until 10) { 
                if (i < inputText.length) {
                    append(inputText[i])
                } 
                if (i == 2) append(") ")
                if (i == 5) append("-")
                if (i == 7) append("-")
            }
        }.trimEnd { it == '-' || it == ' ' || it == ')' } 
         .let { 
            if (it == "+7 (" && inputText.isEmpty()) "+7 (" 
            else if (it == "+7" && inputText.isEmpty()) "+7 (" 
            else it
        }

        val numberOffsetTranslator = object : OffsetMapping {
            override fun originalToTransformed(offset: Int): Int {
                if (offset == 0) return 4
                if (offset <= 3) return offset + 4
                if (offset <= 6) return offset + 4 + 2
                if (offset <= 8) return offset + 4 + 2 + 1
                if (offset <= 10) return offset + 4 + 2 + 1 + 1
                return 18 
            }

            override fun transformedToOriginal(offset: Int): Int {
                var digitCount = 0
                var currentTransformedOffset = 0
                if (offset <= 4) return 0 
                currentTransformedOffset = 4
                for (i in 0 until 3) {
                    currentTransformedOffset++ 
                    if (currentTransformedOffset >= offset) return digitCount
                    digitCount++
                }
                currentTransformedOffset += 2
                if (currentTransformedOffset >= offset) return digitCount
                for (i in 0 until 3) {
                    currentTransformedOffset++ 
                    if (currentTransformedOffset >= offset) return digitCount
                    digitCount++
                }
                currentTransformedOffset++
                if (currentTransformedOffset >= offset) return digitCount
                for (i in 0 until 2) {
                    currentTransformedOffset++ 
                    if (currentTransformedOffset >= offset) return digitCount
                    digitCount++
                }
                currentTransformedOffset++
                if (currentTransformedOffset >= offset) return digitCount
                for (i in 0 until 2) {
                    currentTransformedOffset++ 
                    if (currentTransformedOffset >= offset) return digitCount
                    digitCount++
                }
                return digitCount 
            }
        }
        return TransformedText(AnnotatedString(maskedNumber), numberOffsetTranslator)
    }
}
