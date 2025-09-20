package ru.devsoland.socialsync

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AccountCircle // Для Контактов
import androidx.compose.material.icons.filled.DateRange // Для Событий
import androidx.compose.material.icons.filled.Person // Для Профиля
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.navArgument
import dagger.hilt.android.AndroidEntryPoint
import ru.devsoland.socialsync.ui.AppDestinations
import ru.devsoland.socialsync.ui.addcontact.AddContactScreen
import ru.devsoland.socialsync.ui.aigreeting.AiGreetingPromptScreen // <-- ДОБАВЛЕН ИМПОРТ
import ru.devsoland.socialsync.ui.contacts.ContactListScreen
import ru.devsoland.socialsync.ui.editcontact.EditContactScreen
import ru.devsoland.socialsync.ui.eventdetail.EventDetailScreen
import ru.devsoland.socialsync.ui.events.EventsScreen
import ru.devsoland.socialsync.ui.profile.ProfileScreen
import ru.devsoland.socialsync.ui.theme.SocialSyncTheme
import ru.devsoland.socialsync.ui.welcome.WelcomeScreen

@OptIn(ExperimentalMaterial3Api::class)
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppNavigator() {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    val currentRoute = currentDestination?.route

    val isEventDetailScreen = currentRoute?.startsWith("event_detail/") == true
    val isEditContactScreen = currentRoute?.startsWith("edit_contact/") == true
    val isAiGreetingPromptScreen = currentRoute?.startsWith("ai_greeting_prompt/") == true

    val showBottomBarAndFabAndMainTopBar = currentRoute !in listOf(
        AppDestinations.WELCOME_ROUTE,
        AppDestinations.ADD_CONTACT_ROUTE
    ) && !isEventDetailScreen && !isEditContactScreen && !isAiGreetingPromptScreen

    Scaffold(
        topBar = {
            if (showBottomBarAndFabAndMainTopBar) {
                val currentScreenItem = bottomNavItems.find { it.route == currentRoute }
                currentScreenItem?.let {
                    CenterAlignedTopAppBar(
                        title = { Text(stringResource(id = it.labelResId)) },
                        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer,
                            titleContentColor = MaterialTheme.colorScheme.primary
                        )
                    )
                }
            }
        },
        bottomBar = {
            if (showBottomBarAndFabAndMainTopBar) {
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
            if (currentRoute == AppDestinations.CONTACT_LIST_ROUTE && showBottomBarAndFabAndMainTopBar) {
                FloatingActionButton(onClick = {
                    navController.navigate(AppDestinations.ADD_CONTACT_ROUTE)
                }) {
                    Icon(Icons.Filled.Add, contentDescription = stringResource(R.string.add_contact_description))
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = AppDestinations.WELCOME_ROUTE,
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
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
                ContactListScreen(navController = navController) 
            }
            composable(AppDestinations.EVENTS_ROUTE) {
                EventsScreen(navController = navController)
            }
            composable(AppDestinations.PROFILE_ROUTE) {
                ProfileScreen()
            }
            composable(AppDestinations.ADD_CONTACT_ROUTE) {
                AddContactScreen() 
            }
            composable(
                route = AppDestinations.EVENT_DETAIL_ROUTE_PATTERN,
                arguments = listOf(navArgument(AppDestinations.EVENT_DETAIL_CONTACT_ID_ARG) { type = NavType.LongType })
            ) {
                EventDetailScreen(navController = navController)
            }
            composable(
                route = AppDestinations.EDIT_CONTACT_ROUTE_PATTERN,
                arguments = listOf(navArgument(AppDestinations.EDIT_CONTACT_ID_ARG) { type = NavType.LongType })
            ) { 
                EditContactScreen(navController = navController)
            }
            composable(
                route = AppDestinations.AI_GREETING_PROMPT_ROUTE_PATTERN,
                arguments = listOf(
                    navArgument(AppDestinations.AI_GREETING_PROMPT_CONTACT_ID_ARG) { 
                        type = NavType.LongType 
                    },
                    navArgument(AppDestinations.AI_GREETING_PROMPT_EVENT_ID_ARG) { 
                        type = NavType.LongType 
                    }
                )
            ) { 
                AiGreetingPromptScreen(navController = navController)
            }
        }
    }
}
