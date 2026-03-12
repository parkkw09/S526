package app.peter.s526.domain.usecase.impl

import app.peter.s526.data.repositories.LibraryRepository
import app.peter.s526.domain.model.NewListBook
import app.peter.s526.domain.translator.BookTranslator
import app.peter.s526.domain.usecase.NewBookUseCase
import javax.inject.Inject

class NewBookUseCaseImpl @Inject constructor(
    private val repository: LibraryRepository
): NewBookUseCase {

    override suspend fun getNewBook(page: String): NewListBook {
        return BookTranslator.getListBook(repository.getNewBook(page))
    }

    companion object {
        private const val TAG = "NewBookUseCase"
    }
}