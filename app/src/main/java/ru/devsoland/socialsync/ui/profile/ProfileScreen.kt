package ru.devsoland.socialsync.ui.profile

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding // Этот импорт может быть не нужен, если padding применяется NavHost
import androidx.compose.material3.Text
// ExperimentalMaterial3Api, MaterialTheme, Scaffold, TopAppBar, TopAppBarDefaults больше не нужны здесь
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
// import androidx.compose.ui.res.stringResource // Не используется, если заголовок в MainActivity
// import ru.devsoland.socialsync.R // Не используется, если заголовок в MainActivity

// OptIn(ExperimentalMaterial3Api::class) // Больше не нужен здесь
@Composable
fun ProfileScreen(
    // Сюда можно будет добавить NavController или ViewModel, если понадобится
) {
    // Scaffold и TopAppBar удалены, так как они теперь управляются из MainActivity
    Box(
        modifier = Modifier
            .fillMaxSize(),
            // .padding(innerPadding) // innerPadding будет применен NavHost в MainActivity
        contentAlignment = Alignment.Center
    ) {
        Text("Экран Профиля")
    }
}

// Строка для strings.xml <string name="profile_screen_title">Профиль</string> уже должна быть там,
// так как используется в MainActivity для заголовка TopAppBar.
