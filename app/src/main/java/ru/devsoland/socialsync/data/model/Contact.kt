package ru.devsoland.socialsync.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDate

@Entity(tableName = "contacts")
data class Contact(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val lastName: String,
    val firstName: String,
    val middleName: String? = null,
    val birthDate: LocalDate,
    val phoneNumber: String? = null,
    val notes: String? = null
)