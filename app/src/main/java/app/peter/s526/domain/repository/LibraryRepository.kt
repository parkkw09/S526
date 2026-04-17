package app.peter.s526.domain.repository

import app.peter.s526.domain.model.Book
import app.peter.s526.domain.model.BookDetail
import app.peter.s526.domain.model.BookList

interface LibraryRepository {
    suspend fun getNewBooks(page: String = "1"): BookList
    suspend fun getBookDetail(isbn: String): BookDetail
    suspend fun searchBooks(query: String, page: String): BookList

    fun addBookmark(book: Book)
    fun deleteBookmark(book: Book)
    fun isBookmarked(book: Book): Boolean
    fun updateBookmark(bookmark: List<Book>)
    fun getBookmark(): List<Book>

    fun addHistory(query: String)
    fun getHistory(): List<String>
}
