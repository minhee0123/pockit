package com.minhee.pockit.data.repository

import com.minhee.pockit.data.local.dao.PockitEntryDao
import com.minhee.pockit.data.local.dao.TagDao
import com.minhee.pockit.data.local.entity.EntryEmotionTagCrossRef
import com.minhee.pockit.data.local.entity.EntryThemeTagCrossRef
import com.minhee.pockit.data.local.entity.EntryWithTags
import com.minhee.pockit.data.local.entity.PockitEntryEntity
import com.minhee.pockit.data.local.entity.ThemeTagEntity
import com.minhee.pockit.domain.model.PockitEntry
import com.minhee.pockit.domain.model.Tag
import com.minhee.pockit.domain.repository.PockitRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.YearMonth
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PockitRepositoryImpl @Inject constructor(
    private val entryDao: PockitEntryDao,
    private val tagDao: TagDao,
) : PockitRepository {

    override fun getEntriesByMonth(yearMonth: YearMonth): Flow<List<PockitEntry>> {
        val start = yearMonth.atDay(1).toString()
        val end = yearMonth.atEndOfMonth().toString()
        return entryDao.getEntriesByDateRange(start, end).map { list ->
            list.map { it.toDomain() }
        }
    }

    override fun getEntriesByDate(date: LocalDate): Flow<List<PockitEntry>> {
        return entryDao.getEntriesByDate(date.toString()).map { list ->
            list.map { it.toDomain() }
        }
    }

    override suspend fun getEntryById(id: Long): PockitEntry? {
        return entryDao.getEntryById(id)?.toDomain()
    }

    override suspend fun saveEntry(
        entry: PockitEntry,
        themeTagIds: List<Long>,
        emotionTagIds: List<Long>,
    ) {
        val now = LocalDateTime.now()
        val entity = PockitEntryEntity(
            date = entry.date,
            realizedPnl = entry.realizedPnl,
            stockName = entry.stockName,
            memo = entry.memo,
            createdAt = now,
            updatedAt = now,
        )
        val entryId = entryDao.insertEntry(entity)
        entryDao.insertThemeTagRefs(themeTagIds.map { EntryThemeTagCrossRef(entryId, it) })
        entryDao.insertEmotionTagRefs(emotionTagIds.map { EntryEmotionTagCrossRef(entryId, it) })
    }

    override suspend fun updateEntry(
        entry: PockitEntry,
        themeTagIds: List<Long>,
        emotionTagIds: List<Long>,
    ) {
        val entity = PockitEntryEntity(
            id = entry.id,
            date = entry.date,
            realizedPnl = entry.realizedPnl,
            stockName = entry.stockName,
            memo = entry.memo,
            createdAt = entry.createdAt,
            updatedAt = LocalDateTime.now(),
        )
        entryDao.updateEntry(entity)
        entryDao.deleteThemeTagRefs(entry.id)
        entryDao.deleteEmotionTagRefs(entry.id)
        entryDao.insertThemeTagRefs(themeTagIds.map { EntryThemeTagCrossRef(entry.id, it) })
        entryDao.insertEmotionTagRefs(emotionTagIds.map { EntryEmotionTagCrossRef(entry.id, it) })
    }

    override suspend fun deleteEntry(entry: PockitEntry) {
        entryDao.deleteEntry(
            PockitEntryEntity(
                id = entry.id,
                date = entry.date,
                realizedPnl = entry.realizedPnl,
                stockName = entry.stockName,
                memo = entry.memo,
                createdAt = entry.createdAt,
                updatedAt = entry.updatedAt,
            )
        )
    }

    override fun getMonthlyProfit(yearMonth: YearMonth): Flow<Long> {
        return entryDao.getTotalProfit(
            yearMonth.atDay(1).toString(),
            yearMonth.atEndOfMonth().toString(),
        )
    }

    override fun getMonthlyLoss(yearMonth: YearMonth): Flow<Long> {
        return entryDao.getTotalLoss(
            yearMonth.atDay(1).toString(),
            yearMonth.atEndOfMonth().toString(),
        )
    }

    override fun getMonthlyEntryCount(yearMonth: YearMonth): Flow<Int> {
        return entryDao.getEntryCount(
            yearMonth.atDay(1).toString(),
            yearMonth.atEndOfMonth().toString(),
        )
    }

    override fun getMonthlyWinCount(yearMonth: YearMonth): Flow<Int> {
        return entryDao.getWinCount(
            yearMonth.atDay(1).toString(),
            yearMonth.atEndOfMonth().toString(),
        )
    }

    override fun getAllThemeTags(): Flow<List<Tag>> {
        return tagDao.getAllThemeTags().map { list ->
            list.map { Tag(it.id, it.name, it.isDefault) }
        }
    }

    override fun getAllEmotionTags(): Flow<List<Tag>> {
        return tagDao.getAllEmotionTags().map { list ->
            list.map { Tag(it.id, it.name) }
        }
    }

    override suspend fun addThemeTag(name: String): Long {
        return tagDao.insertThemeTag(ThemeTagEntity(name = name, isDefault = false))
    }

    override suspend fun deleteThemeTag(tag: Tag) {
        tagDao.deleteThemeTag(ThemeTagEntity(id = tag.id, name = tag.name, isDefault = tag.isDefault))
    }

    private fun EntryWithTags.toDomain() = PockitEntry(
        id = entry.id,
        date = entry.date,
        realizedPnl = entry.realizedPnl,
        stockName = entry.stockName,
        memo = entry.memo,
        themeTags = themeTags.map { Tag(it.id, it.name, it.isDefault) },
        emotionTags = emotionTags.map { Tag(it.id, it.name) },
        createdAt = entry.createdAt,
        updatedAt = entry.updatedAt,
    )
}
