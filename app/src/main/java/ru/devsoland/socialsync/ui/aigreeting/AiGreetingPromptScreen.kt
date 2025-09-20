package ru.devsoland.socialsync.ui.aigreeting

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import kotlinx.coroutines.launch
import ru.devsoland.socialsync.R

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun AiGreetingPromptScreen(
    navController: NavController,
    viewModel: AiGreetingPromptViewModel = hiltViewModel()
) {
    val contactName by viewModel.contactName.collectAsState()
    val availableRelationships = viewModel.availableRelationships
    val selectedRelationship by viewModel.selectedRelationship.collectAsState()
    val availableStyles = viewModel.availableStyles
    val selectedStyle by viewModel.selectedStyle.collectAsState()
    val userKeywords by viewModel.userKeywords.collectAsState()

    val isLoading by viewModel.isLoading.collectAsState()
    val generatedGreetings by viewModel.generatedGreetings.collectAsState()
    val selectedIndices by viewModel.selectedIndices.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()
    val saveStatusMessage by viewModel.saveStatusMessage.collectAsState() // <-- Получаем сообщение о статусе сохранения

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var showBottomSheet by rememberSaveable { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() } // <-- Для Snackbar

    LaunchedEffect(generatedGreetings, isLoading) {
        if (generatedGreetings.isNotEmpty() && !isLoading) {
            showBottomSheet = true
        }
    }

    // Показываем Snackbar при изменении saveStatusMessage
    LaunchedEffect(saveStatusMessage) {
        saveStatusMessage?.let {
            snackbarHostState.showSnackbar(message = it)
            viewModel.clearSaveStatusMessage() // Очищаем сообщение после показа
            // Если сохранение было успешным (можно добавить более явную проверку, если нужно)
            if (it.contains("успешно сохранены", ignoreCase = true)) {
                scope.launch {
                    if(sheetState.isVisible) sheetState.hide()
                }.invokeOnCompletion { 
                    showBottomSheet = false
                    navController.popBackStack() 
                }
            }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) }, // <-- Добавляем SnackbarHost
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.ai_greeting_top_bar_title_main)) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, stringResource(R.string.back_button_description))
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.primary
                ),
                windowInsets = TopAppBarDefaults.windowInsets
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.Start,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = stringResource(R.string.ai_greeting_generation_title_main),
                    style = MaterialTheme.typography.headlineSmall,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
                if (contactName.isNotBlank()) {
                    Text(
                        text = stringResource(R.string.ai_greeting_prompt_for_contact_short, contactName),
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = userKeywords,
                    onValueChange = { viewModel.onUserKeywordsChanged(it) },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text(stringResource(R.string.ai_greeting_prompt_label)) },
                    placeholder = { Text(stringResource(R.string.ai_greeting_prompt_placeholder)) },
                    minLines = 3,
                    enabled = !isLoading
                )

                Text(stringResource(R.string.ai_greeting_relationship_label), style = MaterialTheme.typography.titleMedium)
                FlowRow(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    availableRelationships.forEach { relationship ->
                        FilterChip(selected = (relationship == selectedRelationship), onClick = { viewModel.onRelationshipSelected(relationship) }, label = { Text(relationship) }, enabled = !isLoading)
                    }
                }

                Text(stringResource(R.string.ai_greeting_style_label), style = MaterialTheme.typography.titleMedium)
                FlowRow(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    availableStyles.forEach { style ->
                        FilterChip(selected = (style == selectedStyle), onClick = { viewModel.onStyleSelected(style) }, label = { Text(style) }, enabled = !isLoading)
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp, top = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (isLoading) {
                    CircularProgressIndicator(modifier = Modifier.padding(bottom = 16.dp))
                }
                errorMessage?.let {
                    Text(text = it, color = MaterialTheme.colorScheme.error, textAlign = TextAlign.Center, modifier = Modifier.padding(bottom = 8.dp))
                }
                Button(
                    onClick = { viewModel.generateGreeting() },
                    enabled = !isLoading,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(stringResource(R.string.ai_greeting_generate_button_label))
                }
            }
        }

        if (showBottomSheet) {
            ModalBottomSheet(
                onDismissRequest = { 
                    showBottomSheet = false 
                     // Если пользователь закрывает bottom sheet, не сохраняя, очищаем статус сохранения
                    if(saveStatusMessage != null) viewModel.clearSaveStatusMessage()
                }, 
                sheetState = sheetState
            ) {
                Column(
                    modifier = Modifier
                        .padding(start = 16.dp, end = 16.dp, bottom = 16.dp, top = 8.dp)
                        .fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(stringResource(R.string.ai_greeting_generated_results_title), style = MaterialTheme.typography.titleLarge, modifier = Modifier.padding(bottom = 16.dp))
                    if (isLoading && generatedGreetings.isEmpty()) {
                        CircularProgressIndicator(modifier = Modifier.padding(vertical = 20.dp))
                    } else if (generatedGreetings.isNotEmpty()) {
                        LazyColumn(
                            modifier = Modifier.weight(1f, fill = false).padding(bottom = 16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            itemsIndexed(generatedGreetings) { index, greeting ->
                                SelectableGreetingCard(
                                    text = greeting,
                                    isSelected = selectedIndices.contains(index),
                                    onCheckedChange = { viewModel.toggleSelection(index) }
                                )
                            }
                        }
                        Button(
                            onClick = {
                                // Вызываем метод сохранения во ViewModel
                                viewModel.saveSelectedGreetingsToEvent()
                                // Логика скрытия sheet и навигации теперь в LaunchedEffect, 
                                // который слушает saveStatusMessage
                            },
                            enabled = selectedIndices.isNotEmpty(),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(stringResource(R.string.ai_greeting_confirm_selection_button_label))
                        }
                    } else {
                        val noResultsMessage = errorMessage ?: stringResource(R.string.ai_greeting_no_results)
                        Text(noResultsMessage, modifier = Modifier.padding(vertical = 20.dp), textAlign = TextAlign.Center)
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }
    }
}

@Composable
fun SelectableGreetingCard(
    text: String, 
    isSelected: Boolean,
    onCheckedChange: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = isSelected,
                onCheckedChange = { _ -> onCheckedChange() },
                modifier = Modifier.padding(end = 12.dp)
            )
            Text(text = text, style = MaterialTheme.typography.bodyMedium) 
        }
    }
}
