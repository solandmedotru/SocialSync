package ru.devsoland.socialsync.data.database

import androidx.room.TypeConverter
import java.time.LocalDate
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json // <-- ДОБАВЛЕН ЭТОТ ИМПОРТ
// import kotlinx.serialization.decodeFromString // Этот импорт не обязателен, если используется Json.decodeFromString

class Converters {
    // Конвертеры для LocalDate
    @TypeConverter
    fun fromTimestamp(value: Long?): LocalDate? {
        return value?.let { LocalDate.ofEpochDay(it) }
    }

    @TypeConverter
    fun dateToTimestamp(date: LocalDate?): Long? {
        return date?.toEpochDay()
    }

    // Конвертеры для List<String> с использованием JSON
    @TypeConverter
    fun fromStringList(list: List<String>?): String? {
        return list?.let { Json.encodeToString(it) }
    }

    @TypeConverter
    fun toStringList(jsonString: String?): List<String>? {
        return jsonString?.let { Json.decodeFromString<List<String>>(it) }
    }
}
