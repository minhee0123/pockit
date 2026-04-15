package com.minhee.pockit.di

import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.minhee.pockit.data.local.PockitDatabase
import com.minhee.pockit.data.local.dao.PockitEntryDao
import com.minhee.pockit.data.local.dao.TagDao
import com.minhee.pockit.data.local.entity.EmotionTagEntity
import com.minhee.pockit.data.local.entity.ThemeTagEntity
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Provider
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(
        @ApplicationContext context: Context,
        tagDaoProvider: Provider<TagDao>,
    ): PockitDatabase {
        return Room.databaseBuilder(
            context,
            PockitDatabase::class.java,
            "pockit.db",
        )
            .addCallback(object : RoomDatabase.Callback() {
                override fun onCreate(db: SupportSQLiteDatabase) {
                    super.onCreate(db)
                    CoroutineScope(Dispatchers.IO).launch {
                        val tagDao = tagDaoProvider.get()
                        prepopulateThemeTags(tagDao)
                        prepopulateEmotionTags(tagDao)
                    }
                }
            })
            .build()
    }

    @Provides
    fun providePockitEntryDao(database: PockitDatabase): PockitEntryDao {
        return database.pockitEntryDao()
    }

    @Provides
    fun provideTagDao(database: PockitDatabase): TagDao {
        return database.tagDao()
    }

    private suspend fun prepopulateThemeTags(tagDao: TagDao) {
        val defaultTags = listOf(
            "전력 인프라", "우주항공", "화장품", "반도체",
            "2차전지", "AI/IT", "바이오", "금융", "기타",
        )
        defaultTags.forEach { name ->
            tagDao.insertThemeTag(ThemeTagEntity(name = name, isDefault = true))
        }
    }

    private suspend fun prepopulateEmotionTags(tagDao: TagDao) {
        val defaultEmotions = listOf(
            "확신 있었다", "불안했다", "뇌동매매", "FOMO", "계획대로",
        )
        defaultEmotions.forEach { name ->
            tagDao.insertEmotionTag(EmotionTagEntity(name = name))
        }
    }
}
