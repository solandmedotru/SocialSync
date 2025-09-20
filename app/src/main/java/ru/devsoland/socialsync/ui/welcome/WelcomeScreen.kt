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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import ru.devsoland.socialsync.R 
import ru.devsoland.socialsync.ui.theme.SocialSyncTheme

@Composable
fun WelcomeScreen(
    onStartClick: () -> Unit
) {
    Surface(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp), 
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceAround
        ) {
            Spacer(modifier = Modifier.weight(0.5f))

            Text(
                text = stringResource(id = R.string.app_name), 
                style = MaterialTheme.typography.headlineLarge,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = stringResource(id = R.string.welcome_screen_subtitle),
                style = MaterialTheme.typography.titleMedium,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = stringResource(id = R.string.welcome_screen_description),
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(24.dp)) // ИЗМЕНЕНО с 32.dp

            Image(
                painter = painterResource(id = R.drawable.welcome_illustration),
                contentDescription = stringResource(R.string.welcome_screen_illustration_description), 
                modifier = Modifier
                    .fillMaxWidth(0.8f)
                    .height(250.dp), 
                contentScale = ContentScale.Fit 
            )

            Spacer(modifier = Modifier.weight(1f))

            Button(
                onClick = onStartClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp) 
            ) {
                Text(stringResource(R.string.welcome_screen_start_button))
            }
            Spacer(modifier = Modifier.height(24.dp)) // ИЗМЕНЕНО с 32.dp
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
