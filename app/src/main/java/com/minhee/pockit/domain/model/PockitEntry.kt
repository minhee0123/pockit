package com.minhee.pockit.domain.model

import java.time.LocalDate
import java.time.LocalDateTime

data class PockitEntry(
    val id: Long = 0,
    val date: LocalDate,
    val cumulativePnl: Long,
    val dailyPnl: Long,
    val stockName: String? = null,
    val memo: String? = null,
    val themeTags: List<Tag> = emptyList(),
    val emotionTags: List<Tag> = emptyList(),
    val createdAt: LocalDateTime = LocalDateTime.now(),
    val updatedAt: LocalDateTime = LocalDateTime.now(),
)

data class Tag(
    val id: Long = 0,
    val name: String,
    val isDefault: Boolean = false,
)
