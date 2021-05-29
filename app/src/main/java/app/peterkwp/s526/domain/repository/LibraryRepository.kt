package app.peterkwp.s526.domain.repository

import app.peterkwp.s526.domain.local.S526Data
import app.peterkwp.s526.domain.model.DetailBook
import app.peterkwp.s526.domain.remote.Api
import javax.inject.Inject

class LibraryRepository @Inject constructor (
    private val api: Api,
    private val data: S526Data
) {

    suspend fun getNewBook() = api.getNewBooks()
    suspend fun getDetailBook(isbn: String) = api.getBookDetail(isbn)

    fun addBookmark(isbn: String, book: DetailBook) {
        data.bookmark[isbn] = book
    }

    fun deleteBookmark(isbn: String) {
        data.bookmark.remove(isbn)
    }

    fun checkBookmark(isbn: String): Boolean = data.bookmark.containsKey(isbn)

    fun getBookmark() = data.bookmark
}