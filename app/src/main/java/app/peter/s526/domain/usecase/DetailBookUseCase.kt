package app.peter.s526.domain.usecase

import app.peter.s526.domain.model.NewDetailBook

interface DetailBookUseCase {
    suspend fun getDetailBook(isbn: String): NewDetailBook
}