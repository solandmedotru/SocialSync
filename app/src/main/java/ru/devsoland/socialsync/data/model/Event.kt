package ru.devsoland.socialsync.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index // <-- ДОБАВЛЕН ИМПОРТ
import androidx.room.PrimaryKey
import java.time.LocalDate

@Entity(
    tableName = "events",
    foreignKeys = [
        ForeignKey(
            entity = Contact::class,
            parentColumns = ["id"],
            childColumns = ["contactId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["contactId"])] // <-- ДОБАВЛЕН ИНДЕКС ДЛЯ contactId
)
data class Event(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val contactId: Long?,
    val name: String,
    val date: LocalDate,
    val eventType: String,
    val isRecurring: Boolean = true,
    val notes: String? = null
)
