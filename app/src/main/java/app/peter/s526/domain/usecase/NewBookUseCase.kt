package app.peter.s526.domain.usecase

import app.peter.s526.domain.model.Book
import app.peter.s526.domain.repository.LibraryRepository
import javax.inject.Inject

class NewBookUseCase @Inject constructor(
    private val repository: LibraryRepository
) {

    suspend fun getNewBook(): List<Book> {
        return repository.getNewBook().books
    }

    companion object {
        private const val TAG = "NewBookUseCase"
    }
}