package app.peter.s526.domain.usecase

import app.peter.s526.domain.model.BookDetail

interface DetailBookUseCase {
    suspend fun getDetailBook(isbn: String): BookDetail
}
