package app.peter.s526.domain.usecase.impl

import app.peter.s526.data.repositories.LibraryRepository
import app.peter.s526.domain.model.NewDetailBook
import app.peter.s526.domain.translator.BookTranslator
import app.peter.s526.domain.usecase.DetailBookUseCase
import javax.inject.Inject

class DetailBookUseCaseImpl @Inject constructor(
    private val repository: LibraryRepository
): DetailBookUseCase {

    override suspend fun getDetailBook(isbn: String): NewDetailBook {
        return BookTranslator.getNewDetailBook(repository.getDetailBook(isbn))
    }

    companion object {
        private const val TAG = "DetailBookUseCase"
    }
}