package app.peter.s526.domain.usecase.impl

import app.peter.s526.domain.model.BookList
import app.peter.s526.domain.repository.LibraryRepository
import app.peter.s526.domain.usecase.SearchBookUseCase
import javax.inject.Inject

class SearchBookUseCaseImpl @Inject constructor(
    private val repository: LibraryRepository,
) : SearchBookUseCase {

    override suspend fun searchBook(query: String, page: String): BookList =
        repository.searchBooks(query, page)

    override fun addHistory(query: String) = repository.addHistory(query)

    override fun getHistory(): List<String> = repository.getHistory()
}
