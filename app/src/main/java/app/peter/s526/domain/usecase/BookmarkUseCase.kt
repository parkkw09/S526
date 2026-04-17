package app.peter.s526.domain.usecase

import app.peter.s526.domain.model.Book
import app.peter.s526.domain.model.BookDetail

interface BookmarkUseCase {
    fun addBookmark(detail: BookDetail)
    fun deleteBookmark(detail: BookDetail)
    fun isBookmarked(detail: BookDetail): Boolean
    fun updateBookmark(bookmark: List<Book>)
    fun getBookmark(): List<Book>
}
