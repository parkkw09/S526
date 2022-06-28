package app.peter.s526.domain.usecase

import app.peter.s526.domain.model.NewListBook

interface SearchBookUseCase {
    suspend fun searchBook(query: String, page: String): NewListBook
    fun addHistory(query: String)
    fun getHistory(): List<String>
}