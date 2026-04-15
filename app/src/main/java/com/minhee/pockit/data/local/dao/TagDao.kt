package com.minhee.pockit.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import com.minhee.pockit.data.local.entity.EmotionTagEntity
import com.minhee.pockit.data.local.entity.ThemeTagEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TagDao {

    @Insert
    suspend fun insertThemeTag(tag: ThemeTagEntity): Long

    @Delete
    suspend fun deleteThemeTag(tag: ThemeTagEntity)

    @Query("SELECT * FROM theme_tag ORDER BY isDefault DESC, name ASC")
    fun getAllThemeTags(): Flow<List<ThemeTagEntity>>

    @Insert
    suspend fun insertEmotionTag(tag: EmotionTagEntity): Long

    @Query("SELECT * FROM emotion_tag ORDER BY name ASC")
    fun getAllEmotionTags(): Flow<List<EmotionTagEntity>>

    @Query("SELECT COUNT(*) FROM theme_tag")
    suspend fun getThemeTagCount(): Int

    @Query("SELECT COUNT(*) FROM emotion_tag")
    suspend fun getEmotionTagCount(): Int
}
