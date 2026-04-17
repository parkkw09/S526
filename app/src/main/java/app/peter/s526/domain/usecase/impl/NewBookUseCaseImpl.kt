package app.peter.s526.domain.usecase.impl

import app.peter.s526.domain.model.BookList
import app.peter.s526.domain.repository.LibraryRepository
import app.peter.s526.domain.usecase.NewBookUseCase
import javax.inject.Inject

class NewBookUseCaseImpl @Inject constructor(
    private val repository: LibraryRepository,
) : NewBookUseCase {

    override suspend fun getNewBook(page: String): BookList = repository.getNewBooks(page)
}
