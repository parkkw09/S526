package app.peter.s526.domain.usecase.impl

import app.peter.s526.domain.model.BookDetail
import app.peter.s526.domain.repository.LibraryRepository
import app.peter.s526.domain.usecase.DetailBookUseCase
import javax.inject.Inject

class DetailBookUseCaseImpl @Inject constructor(
    private val repository: LibraryRepository,
) : DetailBookUseCase {

    override suspend fun getDetailBook(isbn: String): BookDetail = repository.getBookDetail(isbn)
}
