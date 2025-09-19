package ru.devsoland.socialsync

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AccountCircle // Для Контактов
import androidx.compose.material.icons.filled.DateRange // <-- ЗАМЕНА ДЛЯ ИКОНКИ Event
import androidx.compose.material.icons.filled.Person // Для Профиля
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import dagger.hilt.android.AndroidEntryPoint
import ru.devsoland.socialsync.ui.AppDestinations
import ru.devsoland.socialsync.ui.addcontact.AddContactScreen
import ru.devsoland.socialsync.ui.contacts.ContactListScreen
import ru.devsoland.socialsync.ui.events.EventsScreen
import ru.devsoland.socialsync.ui.profile.ProfileScreen
import ru.devsoland.socialsync.ui.theme.SocialSyncTheme
import ru.devsoland.socialsync.ui.welcome.WelcomeScreen

@OptIn(ExperimentalMaterial3Api::class) // Добавлено для CenterAlignedTopAppBar и TopAppBarDefaults
@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SocialSyncTheme {
                AppNavigator()
            }
        }
    }
}

// Элементы для Bottom Navigation Bar
sealed class BottomNavItem(val route: String, val icon: ImageVector, val labelResId: Int) {
    object Events : BottomNavItem(AppDestinations.EVENTS_ROUTE, Icons.Filled.DateRange, R.string.events_screen_title)
    object Contacts : BottomNavItem(AppDestinations.CONTACT_LIST_ROUTE, Icons.Filled.AccountCircle, R.string.contact_list_title)
    object Profile : BottomNavItem(AppDestinations.PROFILE_ROUTE, Icons.Filled.Person, R.string.profile_screen_title)
}

val bottomNavItems = listOf(
    BottomNavItem.Events,
    BottomNavItem.Contacts,
    BottomNavItem.Profile
)

@OptIn(ExperimentalMaterial3Api::class) // Уже было, но подтверждаем для TopAppBar
@Composable
fun AppNavigator() {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    val showBottomBarAndFab = currentDestination?.route !in listOf(
        AppDestinations.WELCOME_ROUTE,
        AppDestinations.ADD_CONTACT_ROUTE
    )

    Scaffold(
        topBar = {
            if (showBottomBarAndFab) { // Показываем TopAppBar на тех же экранах, что и BottomBar
                val currentScreenItem = bottomNavItems.find { it.route == currentDestination?.route }
                currentScreenItem?.let { // Убедимся, что нашли элемент для заголовка
                    CenterAlignedTopAppBar(
                        title = { Text(stringResource(id = it.labelResId)) },
                        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer,
                            titleContentColor = MaterialTheme.colorScheme.primary
                        ),
                    )
                }
            }
        },
        bottomBar = {
            if (showBottomBarAndFab) {
                NavigationBar {
                    bottomNavItems.forEach { screen ->
                        NavigationBarItem(
                            icon = { Icon(screen.icon, contentDescription = stringResource(screen.labelResId)) },
                            label = { Text(stringResource(screen.labelResId)) },
                            selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true,
                            onClick = {
                                navController.navigate(screen.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        )
                    }
                }
            }
        },
        floatingActionButton = {
            if (currentDestination?.route == AppDestinations.CONTACT_LIST_ROUTE && showBottomBarAndFab) {
                FloatingActionButton(onClick = {
                    navController.navigate(AppDestinations.ADD_CONTACT_ROUTE)
                }) {
                    Icon(Icons.Filled.Add, contentDescription = stringResource(R.string.add_contact_description)) // Используем ресурс
                }
            }
        }
    ) { innerPadding -> // innerPadding теперь будет учитывать и TopAppBar и BottomNavigationBar
        NavHost(
            navController = navController,
            startDestination = AppDestinations.WELCOME_ROUTE,
            // Modifier.padding(innerPadding) здесь уже применен к NavHost контейнеру
            // Отдельные экраны не должны добавлять этот innerPadding еще раз, если они не используют свой Scaffold
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding) // Применяем отступы ко всему содержимому NavHost
        ) {
            composable(AppDestinations.WELCOME_ROUTE) {
                WelcomeScreen(
                    onStartClick = {
                        navController.navigate(AppDestinations.CONTACT_LIST_ROUTE) {
                            popUpTo(AppDestinations.WELCOME_ROUTE) { inclusive = true }
                        }
                    }
                )
            }
            composable(AppDestinations.CONTACT_LIST_ROUTE) {
                // ContactListScreen будет принимать paddingValues отсюда
                ContactListScreen()
            }
            composable(AppDestinations.EVENTS_ROUTE) {
                // EventsScreen будет принимать paddingValues отсюда
                EventsScreen()
            }
            composable(AppDestinations.PROFILE_ROUTE) {
                // ProfileScreen будет принимать paddingValues отсюда
                ProfileScreen()
            }
            composable(AppDestinations.ADD_CONTACT_ROUTE) {
                // AddContactScreen, вероятно, не будет иметь TopAppBar из MainActivity
                // т.к. showBottomBarAndFab для него false
                AddContactScreen()
            }
        }
    }
}

// Предполагается, что R.string.add_contact_description существует.
// Если нет, замените на "Добавить контакт" или создайте ресурс.
