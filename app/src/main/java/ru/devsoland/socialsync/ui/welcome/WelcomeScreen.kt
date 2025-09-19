package ru.devsoland.socialsync.ui.welcome

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import ru.devsoland.socialsync.R // <-- Убедись, что R импортирован из твоего пакета
import ru.devsoland.socialsync.ui.theme.SocialSyncTheme

@Composable
fun WelcomeScreen(
    onStartClick: () -> Unit
) {
    Surface(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceAround
        ) {
            Spacer(modifier = Modifier.weight(0.5f))

            Text(
                text = "SocialSync+",
                style = MaterialTheme.typography.headlineLarge,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Поддерживайте связи без усилий",
                style = MaterialTheme.typography.titleMedium,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Никогда не забывайте о важных моментах и поздравлениях близких вам людей.",
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(32.dp))

            Image(
                painter = painterResource(id = R.drawable.welcome_illustration),
                contentDescription = "Иллюстрация для приветственного экрана", // Описание для доступности
                modifier = Modifier
                    .fillMaxWidth(0.8f)
                    // .aspectRatio(1f) // Можно оставить, если картинка квадратная, или убрать если нет
                    .height(250.dp), // Можно подобрать высоту, или использовать aspectRatio
                contentScale = ContentScale.Fit // Масштабирование, чтобы картинка вписалась
            )

            Spacer(modifier = Modifier.weight(1f))

            Button(
                onClick = onStartClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Text("Начать")
            }
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Preview(showBackground = true)
@Composable
fun WelcomeScreenPreview() {
    SocialSyncTheme {
        WelcomeScreen(onStartClick = {})
    }
}
