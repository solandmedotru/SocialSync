package ru.devsoland.socialsync.ui.editcontact

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Done
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.InputChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope // Не используется, можно удалить
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
// import kotlinx.coroutines.launch // Не используется, можно удалить
import ru.devsoland.socialsync.R

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun EditContactScreen(
    navController: NavController,
    viewModel: EditContactViewModel = hiltViewModel()
) {
    // val coroutineScope = rememberCoroutineScope() // Не используется
    val keyboardController = LocalSoftwareKeyboardController.current

    val firstName by viewModel.firstName.collectAsState()
    val lastName by viewModel.lastName.collectAsState()
    val phoneNumber by viewModel.phoneNumber.collectAsState()
    val birthDate by viewModel.birthDate.collectAsState()
    val originalContact by viewModel.contactState.collectAsState()

    val currentTags by viewModel.tags.collectAsState()
    val suggestedTags = viewModel.suggestedTags
    var customTagInput by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(id = R.string.edit_contact_screen_title)) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.back_button_description)
                        )
                    }
                },
                actions = {
                    IconButton(onClick = {
                        viewModel.saveContact()
                        navController.popBackStack()
                    }) {
                        Icon(
                            imageVector = Icons.Filled.Done,
                            contentDescription = stringResource(R.string.save_contact_description)
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
        if (originalContact == null) {
            Box(
                modifier = Modifier.fillMaxSize().padding(innerPadding),
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
            ) {
                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = firstName,
                    onValueChange = { viewModel.firstName.value = it },
                    label = { Text(stringResource(R.string.first_name_label)) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(
                    value = lastName,
                    onValueChange = { viewModel.lastName.value = it },
                    label = { Text(stringResource(R.string.last_name_label)) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(
                    value = phoneNumber,
                    onValueChange = { viewModel.phoneNumber.value = it },
                    label = { Text(stringResource(R.string.phone_number_label)) },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    singleLine = true
                )
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(
                    value = birthDate ?: "",
                    onValueChange = { viewModel.birthDate.value = it },
                    label = { Text(stringResource(R.string.birth_date_label)) },
                    placeholder = { Text(stringResource(R.string.birth_date_placeholder)) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(24.dp))
                Divider()
                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    stringResource(R.string.tags_section_title),
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
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
                    Spacer(modifier = Modifier.width(8.dp))
                    IconButton(onClick = {
                        viewModel.addTag(customTagInput)
                        customTagInput = ""
                        keyboardController?.hide()
                    }, enabled = customTagInput.isNotBlank()) {
                        Icon(Icons.Filled.AddCircle, contentDescription = stringResource(R.string.add_tag_button_description))
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))

                if (currentTags.isNotEmpty()) {
                    Text(
                        stringResource(R.string.current_tags_label),
                        style = MaterialTheme.typography.titleSmall,
                        modifier = Modifier.fillMaxWidth()
                    )
                    FlowRow(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
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
                    Spacer(modifier = Modifier.height(12.dp))
                }

                Text(
                    stringResource(R.string.suggested_tags_label),
                    style = MaterialTheme.typography.titleSmall,
                    modifier = Modifier.fillMaxWidth()
                )
                FlowRow(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
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
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}
