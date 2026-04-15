package com.minhee.pockit.di

import com.minhee.pockit.data.repository.PockitRepositoryImpl
import com.minhee.pockit.domain.repository.PockitRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindPockitRepository(impl: PockitRepositoryImpl): PockitRepository
}
