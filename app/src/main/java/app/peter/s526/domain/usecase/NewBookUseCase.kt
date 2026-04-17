package app.peter.s526.domain.usecase

import app.peter.s526.domain.model.BookList

interface NewBookUseCase {
    suspend fun getNewBook(page: String = "1"): BookList
}
