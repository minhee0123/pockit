package com.minhee.pockit.domain.repository

import com.minhee.pockit.domain.model.PockitEntry
import com.minhee.pockit.domain.model.Tag
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate
import java.time.YearMonth

interface PockitRepository {
    fun getEntriesByMonth(yearMonth: YearMonth): Flow<List<PockitEntry>>
    fun getEntriesByDate(date: LocalDate): Flow<List<PockitEntry>>
    suspend fun getEntryById(id: Long): PockitEntry?
    suspend fun saveEntry(entry: PockitEntry, themeTagIds: List<Long>, emotionTagIds: List<Long>)
    suspend fun updateEntry(entry: PockitEntry, themeTagIds: List<Long>, emotionTagIds: List<Long>)
    suspend fun deleteEntry(entry: PockitEntry)
    fun getMonthlyProfit(yearMonth: YearMonth): Flow<Long>
    fun getMonthlyLoss(yearMonth: YearMonth): Flow<Long>
    fun getMonthlyEntryCount(yearMonth: YearMonth): Flow<Int>
    fun getMonthlyWinCount(yearMonth: YearMonth): Flow<Int>
    fun getAllThemeTags(): Flow<List<Tag>>
    fun getAllEmotionTags(): Flow<List<Tag>>
    suspend fun addThemeTag(name: String): Long
    suspend fun deleteThemeTag(tag: Tag)
}
