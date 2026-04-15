package com.minhee.pockit.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.minhee.pockit.data.local.converter.DateConverters
import com.minhee.pockit.data.local.dao.PockitEntryDao
import com.minhee.pockit.data.local.dao.TagDao
import com.minhee.pockit.data.local.entity.EmotionTagEntity
import com.minhee.pockit.data.local.entity.EntryEmotionTagCrossRef
import com.minhee.pockit.data.local.entity.EntryThemeTagCrossRef
import com.minhee.pockit.data.local.entity.PockitEntryEntity
import com.minhee.pockit.data.local.entity.ThemeTagEntity

@Database(
    entities = [
        PockitEntryEntity::class,
        ThemeTagEntity::class,
        EmotionTagEntity::class,
        EntryThemeTagCrossRef::class,
        EntryEmotionTagCrossRef::class,
    ],
    version = 1,
    exportSchema = false,
)
@TypeConverters(DateConverters::class)
abstract class PockitDatabase : RoomDatabase() {
    abstract fun pockitEntryDao(): PockitEntryDao
    abstract fun tagDao(): TagDao
}
