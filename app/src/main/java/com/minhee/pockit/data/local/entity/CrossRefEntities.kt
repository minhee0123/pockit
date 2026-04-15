package com.minhee.pockit.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index

@Entity(
    tableName = "entry_theme_tag",
    primaryKeys = ["entryId", "themeTagId"],
    foreignKeys = [
        ForeignKey(
            entity = PockitEntryEntity::class,
            parentColumns = ["id"],
            childColumns = ["entryId"],
            onDelete = ForeignKey.CASCADE,
        ),
        ForeignKey(
            entity = ThemeTagEntity::class,
            parentColumns = ["id"],
            childColumns = ["themeTagId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index("themeTagId")],
)
data class EntryThemeTagCrossRef(
    val entryId: Long,
    val themeTagId: Long,
)

@Entity(
    tableName = "entry_emotion_tag",
    primaryKeys = ["entryId", "emotionTagId"],
    foreignKeys = [
        ForeignKey(
            entity = PockitEntryEntity::class,
            parentColumns = ["id"],
            childColumns = ["entryId"],
            onDelete = ForeignKey.CASCADE,
        ),
        ForeignKey(
            entity = EmotionTagEntity::class,
            parentColumns = ["id"],
            childColumns = ["emotionTagId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index("emotionTagId")],
)
data class EntryEmotionTagCrossRef(
    val entryId: Long,
    val emotionTagId: Long,
)
