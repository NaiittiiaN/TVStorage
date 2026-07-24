package com.tvstorage.app.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

@Serializable
@Entity(tableName = "televisions")
data class TelevisionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val brand: String = "",
    val model: String = "",
    val clientName: String = "",
    val orderNumber: String = "",
    val phoneNumber: String = "",
    val notes: String = "",
    val dailyCost: Double = 100.0,
    val receivedDate: Long = System.currentTimeMillis(),
    val isArchived: Boolean = false,
    val isPaused: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)
