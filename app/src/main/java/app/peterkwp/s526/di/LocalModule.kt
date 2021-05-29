package app.peterkwp.s526.di

import app.peterkwp.s526.domain.local.S526Data
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
class LocalModule {

    @Provides
    fun provideLocalData(): S526Data = S526Data()
}