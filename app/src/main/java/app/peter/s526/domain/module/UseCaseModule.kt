package app.peter.s526.domain.module

import app.peter.s526.data.repositories.LibraryRepository
import app.peter.s526.domain.usecase.BookmarkUseCase
import app.peter.s526.domain.usecase.DetailBookUseCase
import app.peter.s526.domain.usecase.NewBookUseCase
import app.peter.s526.domain.usecase.SearchBookUseCase
import app.peter.s526.domain.usecase.impl.BookmarkUseCaseImpl
import app.peter.s526.domain.usecase.impl.DetailBookUseCaseImpl
import app.peter.s526.domain.usecase.impl.NewBookUseCaseImpl
import app.peter.s526.domain.usecase.impl.SearchBookUseCaseImpl
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class UseCaseModule {

    @Singleton
    @Provides
    fun provideBookmarkUseCase(repository: LibraryRepository): BookmarkUseCase
            = BookmarkUseCaseImpl(repository)

    @Singleton
    @Provides
    fun provideDetailBookUseCase(repository: LibraryRepository): DetailBookUseCase
            = DetailBookUseCaseImpl(repository)

    @Singleton
    @Provides
    fun provideNewBookUseCase(repository: LibraryRepository): NewBookUseCase
            = NewBookUseCaseImpl(repository)

    @Singleton
    @Provides
    fun provideSearchBookUseCase(repository: LibraryRepository): SearchBookUseCase
            = SearchBookUseCaseImpl(repository)
}