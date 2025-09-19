package ru.devsoland.socialsync

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import dagger.hilt.android.AndroidEntryPoint
import ru.devsoland.socialsync.ui.AppDestinations // <-- ДОБАВЛЕНО
import ru.devsoland.socialsync.ui.contacts.ContactListScreen
import ru.devsoland.socialsync.ui.theme.SocialSyncTheme
import ru.devsoland.socialsync.ui.welcome.WelcomeScreen // <-- ДОБАВЛЕНО

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SocialSyncTheme {
                AppNavigator() // <-- Вызываем наш новый Composable для навигации
            }
        }
    }
}

@Composable
fun AppNavigator() {
    val navController = rememberNavController()
    NavHost(
        navController = navController,
        startDestination = AppDestinations.WELCOME_ROUTE // <-- Стартовый экран
    ) {
        composable(AppDestinations.WELCOME_ROUTE) {
            WelcomeScreen(
                onStartClick = {
                    // Переход на экран списка контактов
                    // Мы можем также очистить backstack до WelcomeScreen,
                    // чтобы пользователь не мог вернуться на него кнопкой "назад"
                    // после того, как нажал "Начать".
                    navController.navigate(AppDestinations.CONTACT_LIST_ROUTE) {
                        popUpTo(AppDestinations.WELCOME_ROUTE) {
                            inclusive = true
                        }
                    }
                }
            )
        }
        composable(AppDestinations.CONTACT_LIST_ROUTE) {
            ContactListScreen()
            // Если ContactListScreen ожидает NavController или другие параметры,
            // их нужно будет передать здесь.
        }
        // Сюда можно будет добавлять другие экраны
    }
}
