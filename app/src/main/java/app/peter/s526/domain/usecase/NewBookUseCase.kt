package app.peter.s526.domain.usecase

import app.peter.s526.domain.model.NewListBook

interface NewBookUseCase {
    suspend fun getNewBook(page: String = "1"): NewListBook
}