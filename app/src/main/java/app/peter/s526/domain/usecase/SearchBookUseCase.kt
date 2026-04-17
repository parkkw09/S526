package app.peter.s526.domain.usecase

import app.peter.s526.domain.model.BookList

interface SearchBookUseCase {
    suspend fun searchBook(query: String, page: String): BookList
    fun addHistory(query: String)
    fun getHistory(): List<String>
}
