package com.example.rightway_out.di

import com.example.rightway_out.data.repository.ClearanceRepositoryImpl
import com.example.rightway_out.domain.repository.ClearanceRepository
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
    abstract fun bindClearanceRepository(impl: ClearanceRepositoryImpl): ClearanceRepository
}
