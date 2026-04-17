package app.peter.s526.domain.module

import app.peter.s526.domain.usecase.BookmarkUseCase
import app.peter.s526.domain.usecase.DetailBookUseCase
import app.peter.s526.domain.usecase.NewBookUseCase
import app.peter.s526.domain.usecase.SearchBookUseCase
import app.peter.s526.domain.usecase.impl.BookmarkUseCaseImpl
import app.peter.s526.domain.usecase.impl.DetailBookUseCaseImpl
import app.peter.s526.domain.usecase.impl.NewBookUseCaseImpl
import app.peter.s526.domain.usecase.impl.SearchBookUseCaseImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent
import dagger.hilt.android.scopes.ViewModelScoped

@Module
@InstallIn(ViewModelComponent::class)
abstract class UseCaseModule {

    @Binds
    @ViewModelScoped
    abstract fun bindNewBookUseCase(impl: NewBookUseCaseImpl): NewBookUseCase

    @Binds
    @ViewModelScoped
    abstract fun bindBookmarkUseCase(impl: BookmarkUseCaseImpl): BookmarkUseCase

    @Binds
    @ViewModelScoped
    abstract fun bindDetailBookUseCase(impl: DetailBookUseCaseImpl): DetailBookUseCase

    @Binds
    @ViewModelScoped
    abstract fun bindSearchBookUseCase(impl: SearchBookUseCaseImpl): SearchBookUseCase
}
