package ru.devsoland.socialsync.ui.addcontact

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets // <-- ДОБАВЛЕН ИМПОРТ
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp // <-- ДОБАВЛЕН ИМПОРТ для dp
import ru.devsoland.socialsync.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddContactScreen(
    // Сюда можно будет добавить NavController или ViewModel
) {

    Box(
        modifier = Modifier
            .fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text("Экран Добавления Контакта") // TODO: Заменить на реальный контент
    }

}

// Убедитесь, что строка <string name="add_contact_screen_title">Добавить контакт</string>
// или аналогичная есть в вашем файле strings.xml
