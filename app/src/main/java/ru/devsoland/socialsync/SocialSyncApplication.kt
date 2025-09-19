package ru.devsoland.socialsync

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class SocialSyncApplication : Application() {
    // Пока здесь ничего не нужно, Hilt сделает свою магию
    // В будущем здесь можно будет инициализировать что-то на уровне всего приложения,
    // если это потребуется (например, библиотеки для логирования, аналитики и т.д.)
}