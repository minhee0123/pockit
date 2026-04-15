package com.minhee.pockit.data.local.entity

import androidx.room.Embedded
import androidx.room.Junction
import androidx.room.Relation

data class EntryWithTags(
    @Embedded val entry: PockitEntryEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "id",
        associateBy = Junction(
            EntryThemeTagCrossRef::class,
            parentColumn = "entryId",
            entityColumn = "themeTagId",
        ),
    )
    val themeTags: List<ThemeTagEntity>,
    @Relation(
        parentColumn = "id",
        entityColumn = "id",
        associateBy = Junction(
            EntryEmotionTagCrossRef::class,
            parentColumn = "entryId",
            entityColumn = "emotionTagId",
        ),
    )
    val emotionTags: List<EmotionTagEntity>,
)
