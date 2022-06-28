package app.peter.s526.domain.usecase.impl

import app.peter.s526.data.repositories.LibraryRepository
import app.peter.s526.domain.model.NewBook
import app.peter.s526.domain.translator.BookTranslator
import app.peter.s526.domain.usecase.NewBookUseCase
import javax.inject.Inject

class NewBookUseCaseImpl @Inject constructor(
    private val repository: LibraryRepository
): NewBookUseCase {

    override suspend fun getNewBook(): List<NewBook> {
        return repository.getNewBook().books.map {
            BookTranslator.getNewBook(it)
        }
    }

    companion object {
        private const val TAG = "NewBookUseCase"
    }
}