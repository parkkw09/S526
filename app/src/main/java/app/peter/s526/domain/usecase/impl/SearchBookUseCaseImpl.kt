package app.peter.s526.domain.usecase.impl

import app.peter.s526.data.repositories.LibraryRepository
import app.peter.s526.domain.model.NewListBook
import app.peter.s526.domain.translator.BookTranslator
import app.peter.s526.domain.usecase.SearchBookUseCase
import javax.inject.Inject

class SearchBookUseCaseImpl @Inject constructor(
    private val repository: LibraryRepository
): SearchBookUseCase {

    override suspend fun searchBook(query: String, page: String): NewListBook {
        return BookTranslator.getListBook(repository.getSearchBook(query, page))
    }

    override fun addHistory(query: String) = repository.addHistory(query)
    override fun getHistory() = repository.getHistory().toList()

    companion object {
        private const val TAG = "SearchBookUseCase"
    }
}