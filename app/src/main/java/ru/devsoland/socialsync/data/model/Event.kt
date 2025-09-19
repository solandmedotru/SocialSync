package ru.devsoland.socialsync.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import java.time.LocalDate

@Entity(
    tableName = "events",
    foreignKeys = [
        ForeignKey(
            entity = Contact::class,
            parentColumns = ["id"],
            childColumns = ["contactId"],
            onDelete = ForeignKey.CASCADE // Если контакт удален, связанные события тоже удаляются
        )
    ]
)
data class Event(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val contactId: Long?, // Может быть null, если событие не привязано к контакту
    val name: String,
    val date: LocalDate,
    val eventType: String,
    val isRecurring: Boolean = true,
    val notes: String? = null
)