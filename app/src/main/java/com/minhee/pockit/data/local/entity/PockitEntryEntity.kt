package com.minhee.pockit.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDate
import java.time.LocalDateTime

@Entity(tableName = "pockit_entry")
data class PockitEntryEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val date: LocalDate,
    val cumulativePnl: Long,
    val dailyPnl: Long,
    val stockName: String? = null,
    val memo: String? = null,
    val createdAt: LocalDateTime = LocalDateTime.now(),
    val updatedAt: LocalDateTime = LocalDateTime.now(),
)
