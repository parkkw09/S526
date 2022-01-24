package app.peter.s526.domain.usecase

import app.peter.s526.domain.model.DetailBook
import app.peter.s526.data.repositories.LibraryRepository
import javax.inject.Inject

class DetailBookUseCase @Inject constructor(
    private val repository: LibraryRepository
) {

    suspend fun getDetailBook(isbn: String): DetailBook {
        return repository.getDetailBook(isbn)
    }

    companion object {
        private const val TAG = "DetailBookUseCase"
    }
}