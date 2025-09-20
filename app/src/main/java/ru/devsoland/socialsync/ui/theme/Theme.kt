package ru.devsoland.socialsync.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color // Добавлено для комментариев, если понадобится Color.White и т.д.
import androidx.compose.ui.platform.LocalContext

// DarkColorScheme используется, когда системная тема темная И dynamicColor = false (или на Android < 12).
// Эти цвета применяются, если динамические цвета (Material You) не используются.
private val DarkColorScheme = darkColorScheme(
    primary = Purple80,      // Основной акцентный цвет. Используется для ключевых компонентов, таких как FAB (плавающие кнопки действий),
                            // выделенные кнопки, активные состояния переключателей, ползунков, индикаторов выполнения.
    onPrimary = Color.Black, // Цвет текста и иконок НА `primary` фоне (пример, если Purple80 светлый).
                            // M3 автоматически подбирает контрастный цвет, но его можно задать явно.

    secondary = PurpleGrey80,  // Вторичный акцентный цвет. Используется для менее выраженных, но все еще акцентных элементов,
                            // таких как фильтры, некоторые кнопки, акценты на карточках.
    onSecondary = Color.Black, // Цвет текста и иконок НА `secondary` фоне.

    tertiary = Pink80,       // Третичный акцентный цвет. Используется для создания дополнительных акцентов или для элементов,
                            // которые должны выделяться, но не так сильно, как `primary` или `secondary`.
                            // Например, некоторые декоративные элементы, иллюстрации, менее важные кнопки.
    onTertiary = Color.Black,  // Цвет текста и иконок НА `tertiary` фоне.

    // --- Контейнеры для акцентных цветов ---
    // primaryContainer = ...,   // Цвет фона для контейнеров, связанных с `primary` (например, фон для группы элементов
                                // с основным акцентом, но менее интенсивный, чем сам `primary`).
    // onPrimaryContainer = ..., // Цвет текста и иконок НА `primaryContainer`.
    // secondaryContainer = ..., // Аналогично для `secondary`.
    // onSecondaryContainer = ...,// Аналогично для `secondary`.
    // tertiaryContainer = ...,  // Аналогично для `tertiary`.
    // onTertiaryContainer = ..., // Аналогично для `tertiary`.

    // --- Цвета для ошибок ---
    // error = Color(0xFFB00020),          // Яркий цвет для обозначения ошибок (поля ввода, сообщения).
    // onError = Color.White,            // Цвет текста и иконок НА `error` фоне.
    // errorContainer = Color(0xFFFCD8DF),// Менее интенсивный фон для контейнеров с ошибками.
    // onErrorContainer = Color.Black,   // Цвет текста и иконок НА `errorContainer`.

    // --- Основные цвета поверхностей и фона ---
    // background = Color(0xFF121212),      // Основной цвет фона приложения.
    // onBackground = Color.White,         // Цвет текста и иконок НА `background` фоне.

    // surface = Color(0xFF1E1E1E),        // Цвет поверхностей компонентов (карточки, диалоги, меню, BottomSheet).
    // onSurface = Color.White,          // Цвет текста и иконок НА `surface` фоне.

    // surfaceVariant = Color(0xFF2C2C2E),  // Вариант `surface` для визуального разделения блоков UI или для элементов,
                                         // требующих небольшого отличия от основной поверхности (например, фон неактивных вкладок).
    // onSurfaceVariant = Color(0xFFCAC4D0),// Цвет текста и иконок НА `surfaceVariant`.

    // surfaceBright = ..., // Более светлый вариант surface (для светлой темы)
    // surfaceDim = ...,    // Более темный вариант surface (для светлой темы)
    // surfaceContainerLowest = ..., // Самый светлый/темный контейнер
    // surfaceContainerLow = ...,    // Чуть менее светлый/темный
    // surfaceContainer = ...,       // Нейтральный контейнер
    // surfaceContainerHigh = ...,   // Чуть более выраженный
    // surfaceContainerHighest = ...,// Самый выраженный контейнер (ближе к primary/secondary)

    // --- Цвета для обводок и разделителей ---
    // outline = Color(0xFF8E8E93),        // Цвет обводок (например, для OutlinedTextField, границ компонентов).
    // outlineVariant = Color(0xFF424242),  // Более тонкий или менее заметный вариант обводки (например, разделители).

    // --- Специальные цвета ---
    // scrim = Color(0x99000000),          // Цвет для затемняющих оверлеев (например, под BottomSheet или диалогом для выделения модальности).
                                         // Обычно черный с некоторой прозрачностью.
    // inverseSurface = Color(0xFFE1E1E6),  // Инвертированный `surface`. Если `surface` темный, `inverseSurface` светлый, и наоборот.
                                         // Используется для выделения определенных элементов или для компонентов на инвертированном фоне.
    // inverseOnSurface = Color.Black,   // Цвет текста/иконок НА `inverseSurface`.
    // inversePrimary = Purple40         // `primary` цвет, адаптированный для использования НА `inverseSurface`.
)

// LightColorScheme используется, когда системная тема светлая И dynamicColor = false (или на Android < 12).
// Эти цвета применяются, если динамические цвета (Material You) не используются.
private val LightColorScheme = lightColorScheme(
    primary = Purple40,       // Основной акцентный цвет.
    onPrimary = Color.White,    // Текст/иконки на `primary`.

    secondary = PurpleGrey40,   // Вторичный акцентный цвет.
    onSecondary = Color.White,  // Текст/иконки на `secondary`.

    tertiary = Pink40,        // Третичный акцентный цвет.
    onTertiary = Color.White,   // Текст/иконки на `tertiary`.

    // Все остальные роли (container, error, background, surface, outline, scrim и т.д.)
    // будут автоматически выведены Material 3 из этих основных цветов,
    // если их не переопределить явно, как показано в закомментированных примерах в DarkColorScheme.
    // Например:
    // background = Color(0xFFFFFBFE),
    // surface = Color(0xFFFFFBFE),
    // onBackground = Color(0xFF1C1B1F),
    // onSurface = Color(0xFF1C1B1F),
)

@Composable
fun SocialSyncTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color доступен на Android 12+
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }

        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography, // Определено в Type.kt
        // shapes = Shapes, // Если у вас есть файл Shapes.kt для кастомизации форм компонентов
        content = content
    )
}
