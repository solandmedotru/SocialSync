package ru.devsoland.socialsync

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.core.tween 
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AccountCircle 
import androidx.compose.material.icons.filled.DateRange 
import androidx.compose.material.icons.filled.Person 
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember // Добавлен импорт для remember
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
import ru.devsoland.socialsync.ui.addeditcontact.AddEditContactScreen
import ru.devsoland.socialsync.ui.aigreeting.AiGreetingPromptScreen
import ru.devsoland.socialsync.ui.contacts.ContactListScreen
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

    val snackbarHostState = remember { SnackbarHostState() } // ИЗМЕНЕНО: Создаем SnackbarHostState здесь

    val isEventDetailScreen = currentRoute?.startsWith("${AppDestinations.EVENT_DETAIL_ROUTE_PATTERN.substringBefore("/")}/") == true
    val isAddEditContactScreen = currentRoute?.startsWith("${AppDestinations.ADD_EDIT_CONTACT_ROUTE_BASE}/") == true
    val isAiGreetingPromptScreen = currentRoute?.startsWith("${AppDestinations.AI_GREETING_PROMPT_ROUTE_PATTERN.substringBefore("/")}/") == true

    val showBottomBarAndFab = currentRoute !in listOf(
        AppDestinations.WELCOME_ROUTE,
    ) && !isEventDetailScreen && !isAddEditContactScreen && !isAiGreetingPromptScreen

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) }, // ИЗМЕНЕНО: Используем созданный SnackbarHostState
        topBar = {
            if (currentRoute != AppDestinations.WELCOME_ROUTE) {
                TopAppBar(
                    title = {
                        val titleResId = when {
                            currentRoute == AppDestinations.CONTACT_LIST_ROUTE -> R.string.contact_list_title
                            currentRoute == AppDestinations.EVENTS_ROUTE -> R.string.events_screen_title
                            currentRoute == AppDestinations.PROFILE_ROUTE -> R.string.profile_screen_title
                            isEventDetailScreen -> R.string.event_detail_screen_title
                            isAiGreetingPromptScreen -> R.string.ai_greeting_top_bar_title_main
                            isAddEditContactScreen -> {
                                val contactIdArg = navBackStackEntry?.arguments?.getLong(AppDestinations.CONTACT_ID_ARG, AppDestinations.DEFAULT_NEW_CONTACT_ID)
                                if (contactIdArg == AppDestinations.DEFAULT_NEW_CONTACT_ID) {
                                    R.string.add_contact_screen_title
                                } else {
                                    R.string.edit_contact_screen_title
                                }
                            }
                            else -> R.string.app_name 
                        }
                        Text(stringResource(id = titleResId))
                    },
                    navigationIcon = {
                        if (navController.previousBackStackEntry != null) {
                            IconButton(onClick = { navController.navigateUp() }) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = stringResource(R.string.back_button_description)
                                )
                            }
                        }
                    },
                    actions = {
                        
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                )
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
            if (showBottomBarAndFab && currentRoute == AppDestinations.CONTACT_LIST_ROUTE) {
                FloatingActionButton(onClick = { 
                    navController.navigate(AppDestinations.addEditContactRoute(AppDestinations.DEFAULT_NEW_CONTACT_ID))
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
            composable(
                route = AppDestinations.WELCOME_ROUTE,
                exitTransition = { fadeOut(animationSpec = tween(300)) } 
            ) {
                WelcomeScreen(
                    onStartClick = {
                        navController.navigate(AppDestinations.CONTACT_LIST_ROUTE) {
                            popUpTo(AppDestinations.WELCOME_ROUTE) { inclusive = true }
                        }
                    }
                )
            }
            composable(
                route = AppDestinations.CONTACT_LIST_ROUTE,
                enterTransition = { slideInHorizontally(initialOffsetX = { -it }) + fadeIn() }, 
                exitTransition = { slideOutHorizontally(targetOffsetX = { -it }) + fadeOut() }, 
                popEnterTransition = { slideInHorizontally(initialOffsetX = { -it }) + fadeIn() }, 
                popExitTransition = { slideOutHorizontally(targetOffsetX = { it }) + fadeOut() } 
            ) {
                ContactListScreen(navController = navController)
            }
            composable(
                route = AppDestinations.EVENTS_ROUTE,
                enterTransition = { slideInHorizontally(initialOffsetX = { -it }) + fadeIn() }, 
                exitTransition = { slideOutHorizontally(targetOffsetX = { -it }) + fadeOut() }, 
                popEnterTransition = { slideInHorizontally(initialOffsetX = { -it }) + fadeIn() },
                popExitTransition = { slideOutHorizontally(targetOffsetX = { it }) + fadeOut() }
            ) {
                EventsScreen(navController = navController)
            }
            composable(
                route = AppDestinations.PROFILE_ROUTE,
                enterTransition = { slideInHorizontally(initialOffsetX = { -it }) + fadeIn() }, 
                exitTransition = { slideOutHorizontally(targetOffsetX = { -it }) + fadeOut() }, 
                popEnterTransition = { slideInHorizontally(initialOffsetX = { -it }) + fadeIn() },
                popExitTransition = { slideOutHorizontally(targetOffsetX = { it }) + fadeOut() }
            ) {
                ProfileScreen()
            }
            composable(
                route = AppDestinations.EVENT_DETAIL_ROUTE_PATTERN,
                arguments = listOf(navArgument(AppDestinations.EVENT_DETAIL_CONTACT_ID_ARG) { type = NavType.LongType }),
                enterTransition = { slideInHorizontally(initialOffsetX = { it }) + fadeIn() }, 
                exitTransition = { slideOutHorizontally(targetOffsetX = { -it }) + fadeOut() }, 
                popEnterTransition = { slideInHorizontally(initialOffsetX = { -it }) + fadeIn() }, 
                popExitTransition = { slideOutHorizontally(targetOffsetX = { it }) + fadeOut() } 
            ) {
                EventDetailScreen(navController = navController)
            }
            composable(
                route = AppDestinations.ADD_EDIT_CONTACT_ROUTE_PATTERN,
                arguments = listOf(navArgument(AppDestinations.CONTACT_ID_ARG) { 
                    type = NavType.LongType
                    defaultValue = AppDestinations.DEFAULT_NEW_CONTACT_ID
                }),
                enterTransition = { slideInHorizontally(initialOffsetX = { it }) + fadeIn() }, 
                exitTransition = { slideOutHorizontally(targetOffsetX = { -it }) + fadeOut() },
                popEnterTransition = { slideInHorizontally(initialOffsetX = { -it }) + fadeIn() },
                popExitTransition = { slideOutHorizontally(targetOffsetX = { it }) + fadeOut() }
            ) { 
                AddEditContactScreen(navController = navController)
            }
            composable(
                route = AppDestinations.AI_GREETING_PROMPT_ROUTE_PATTERN,
                arguments = listOf(
                    navArgument(AppDestinations.AI_GREETING_PROMPT_CONTACT_ID_ARG) { type = NavType.LongType },
                    navArgument(AppDestinations.AI_GREETING_PROMPT_EVENT_ID_ARG) { type = NavType.LongType }
                ),
                enterTransition = { slideInHorizontally(initialOffsetX = { it }) + fadeIn() }, 
                exitTransition = { slideOutHorizontally(targetOffsetX = { -it }) + fadeOut() },
                popEnterTransition = { slideInHorizontally(initialOffsetX = { -it }) + fadeIn() },
                popExitTransition = { slideOutHorizontally(targetOffsetX = { it }) + fadeOut() }
            ) { 
                // ИЗМЕНЕНО: Передаем snackbarHostState в AiGreetingPromptScreen
                AiGreetingPromptScreen(navController = navController, snackbarHostState = snackbarHostState)
            }
        }
    }
}
