package app.peter.s526.data.module

import app.peter.s526.data.repositories.LibraryRepository
import app.peter.s526.data.repositories.impl.LibraryRepositoryImpl
import app.peter.s526.data.source.local.S526Data
import app.peter.s526.data.source.remote.Api
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
class RepositoryModule {

    @Provides
    fun provideLibraryRepository(remoteSource: Api, localSource: S526Data): LibraryRepository
        = LibraryRepositoryImpl(remoteSource, localSource)
}