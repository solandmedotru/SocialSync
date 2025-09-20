package ru.devsoland.socialsync.data.model

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "contacts",
    indices = [Index(value = ["deviceContactId"])]
)
data class Contact(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val deviceContactId: String? = null,
    val lastName: String? = null, // Сделаем nullable на случай, если есть только имя
    val firstName: String,
    val middleName: String? = null,
    val birthDate: String?,
    val phoneNumber: String? = null,
    val photoUri: String? = null,
    val notes: String? = null,
    val tags: List<String> = emptyList() // <-- НОВОЕ ПОЛЕ ДЛЯ ТЕГОВ
)
