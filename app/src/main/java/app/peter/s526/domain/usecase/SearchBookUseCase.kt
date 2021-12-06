package app.peter.s526.domain.usecase

import app.peter.s526.domain.model.ListBook
import app.peter.s526.domain.repository.LibraryRepository
import javax.inject.Inject

class SearchBookUseCase @Inject constructor(
    private val repository: LibraryRepository
) {

    suspend fun searchBook(query: String, page: String): ListBook {
        return repository.getSearchBook(query, page)
    }

    fun addHistory(query: String) = repository.addHistory(query)
    fun getHistory() = repository.getHistory().toList()

    companion object {
        private const val TAG = "SearchBookUseCase"
    }
}