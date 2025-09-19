package ru.devsoland.socialsync

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import dagger.hilt.android.AndroidEntryPoint
import ru.devsoland.socialsync.ui.contacts.ContactListScreen // Импорт нашего экрана
import ru.devsoland.socialsync.ui.theme.SocialSyncTheme

@AndroidEntryPoint // Добавлено для Hilt
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SocialSyncTheme {
                // Убрали Scaffold отсюда, так как он теперь внутри ContactListScreen
                ContactListScreen() // Вызываем наш новый экран
            }
        }
    }
}
