package app.peterkwp.s526.di

import app.peterkwp.s526.domain.local.S526Data
import app.peterkwp.s526.domain.remote.Api
import app.peterkwp.s526.domain.repository.LibraryRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class RepositoryModule {

    @Singleton
    @Provides
    fun provideLibraryRepository(api: Api, data: S526Data): LibraryRepository = LibraryRepository(api, data)
}