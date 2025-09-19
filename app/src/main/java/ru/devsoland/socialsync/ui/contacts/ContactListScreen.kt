package ru.devsoland.socialsync.ui.contacts

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp // Импорт для dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import ru.devsoland.socialsync.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContactListScreen(
    viewModel: ContactListViewModel = hiltViewModel()
) {
    val contacts by viewModel.contacts.collectAsStateWithLifecycle()

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
        ) {
            if (contacts.isEmpty()) {
                Text(
                    text = "Список контактов пуст.",
                    modifier = Modifier.padding(16.dp)
                )
            } else {
                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    items(contacts) { contact ->
                        Text(
                            text = "${contact.firstName} ${contact.lastName}",
                            modifier = Modifier.padding(16.dp)
                        )
                        // TODO: Добавить разделители, обработчики кликов, более красивое отображение
                    }
                }
            }
        }
    }
}