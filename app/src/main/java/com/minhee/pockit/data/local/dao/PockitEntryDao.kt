package com.minhee.pockit.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.minhee.pockit.data.local.entity.EntryEmotionTagCrossRef
import com.minhee.pockit.data.local.entity.EntryThemeTagCrossRef
import com.minhee.pockit.data.local.entity.EntryWithTags
import com.minhee.pockit.data.local.entity.PockitEntryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PockitEntryDao {

    @Insert
    suspend fun insertEntry(entry: PockitEntryEntity): Long

    @Update
    suspend fun updateEntry(entry: PockitEntryEntity)

    @Delete
    suspend fun deleteEntry(entry: PockitEntryEntity)

    @Insert
    suspend fun insertThemeTagRefs(refs: List<EntryThemeTagCrossRef>)

    @Insert
    suspend fun insertEmotionTagRefs(refs: List<EntryEmotionTagCrossRef>)

    @Query("DELETE FROM entry_theme_tag WHERE entryId = :entryId")
    suspend fun deleteThemeTagRefs(entryId: Long)

    @Query("DELETE FROM entry_emotion_tag WHERE entryId = :entryId")
    suspend fun deleteEmotionTagRefs(entryId: Long)

    @Transaction
    @Query("SELECT * FROM pockit_entry WHERE date BETWEEN :startDate AND :endDate ORDER BY createdAt DESC")
    fun getEntriesByDateRange(startDate: String, endDate: String): Flow<List<EntryWithTags>>

    @Transaction
    @Query("SELECT * FROM pockit_entry WHERE date = :date ORDER BY createdAt DESC")
    fun getEntriesByDate(date: String): Flow<List<EntryWithTags>>

    @Transaction
    @Query("SELECT * FROM pockit_entry WHERE id = :id")
    suspend fun getEntryById(id: Long): EntryWithTags?

    @Query("SELECT cumulativePnl FROM pockit_entry ORDER BY date DESC, createdAt DESC LIMIT 1")
    suspend fun getLatestCumulativePnl(): Long?

    @Query("SELECT COALESCE(SUM(CASE WHEN dailyPnl > 0 THEN dailyPnl ELSE 0 END), 0) FROM pockit_entry WHERE date BETWEEN :startDate AND :endDate")
    fun getTotalProfit(startDate: String, endDate: String): Flow<Long>

    @Query("SELECT COALESCE(SUM(CASE WHEN dailyPnl < 0 THEN dailyPnl ELSE 0 END), 0) FROM pockit_entry WHERE date BETWEEN :startDate AND :endDate")
    fun getTotalLoss(startDate: String, endDate: String): Flow<Long>

    @Query("SELECT COUNT(*) FROM pockit_entry WHERE date BETWEEN :startDate AND :endDate")
    fun getEntryCount(startDate: String, endDate: String): Flow<Int>

    @Query("SELECT COUNT(*) FROM pockit_entry WHERE dailyPnl > 0 AND date BETWEEN :startDate AND :endDate")
    fun getWinCount(startDate: String, endDate: String): Flow<Int>
}
