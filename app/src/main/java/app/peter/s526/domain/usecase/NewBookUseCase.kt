package app.peter.s526.domain.usecase

import app.peter.s526.domain.model.NewBook

interface NewBookUseCase {
    suspend fun getNewBook(): List<NewBook>
}