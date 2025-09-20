package ru.devsoland.socialsync.data.database

import androidx.room.TypeConverter
import java.time.LocalDate

class Converters {
    // Конвертеры для LocalDate (существующие)
    @TypeConverter
    fun fromTimestamp(value: Long?): LocalDate? {
        return value?.let { LocalDate.ofEpochDay(it) }
    }

    @TypeConverter
    fun dateToTimestamp(date: LocalDate?): Long? {
        return date?.toEpochDay()
    }

    // Конвертеры для List<String> (новые)
    private val TAG_DELIMITER = "," // Можно выбрать другой, если запятая может быть в тегах

    @TypeConverter
    fun fromTagsList(tags: List<String>?): String? {
        return tags?.joinToString(TAG_DELIMITER)
    }

    @TypeConverter
    fun toTagsList(tagsString: String?): List<String> {
        // Возвращаем пустой список, если строка null или пуста,
        // чтобы соответствовать значению по умолчанию List<String> = emptyList() в Contact
        return tagsString?.split(TAG_DELIMITER)?.filter { it.isNotBlank() } ?: emptyList()
    }
}
