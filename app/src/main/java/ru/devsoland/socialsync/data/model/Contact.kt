package ru.devsoland.socialsync.data.model

import androidx.room.Entity
import androidx.room.Index // <-- Добавлен импорт для @Index
import androidx.room.PrimaryKey
// import java.time.LocalDate // LocalDate больше не используется напрямую здесь

@Entity(
    tableName = "contacts",
    indices = [Index(value = ["deviceContactId"])] // <-- Добавлен индекс
)
data class Contact(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val deviceContactId: String? = null, // <-- НОВОЕ ПОЛЕ
    val lastName: String,
    val firstName: String,
    val middleName: String? = null,
    val birthDate: String?, // <-- ТИП ИЗМЕНЕН на String?
    val phoneNumber: String? = null,
    val notes: String? = null
)
