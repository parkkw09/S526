package app.peter.s526.domain.usecase

import app.peter.s526.domain.model.NewBook
import app.peter.s526.domain.model.NewDetailBook

interface BookmarkUseCase {
    fun addBookmark(detailBook: NewDetailBook)
    fun deleteBookmark(detailBook: NewDetailBook)
    fun checkBookmark(detailBook: NewDetailBook): Boolean
    fun updateBookmark(bookmark: List<NewBook>)
    fun getBookmark(): List<NewBook>
}