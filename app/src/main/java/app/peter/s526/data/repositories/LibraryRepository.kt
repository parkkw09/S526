package app.peter.s526.data.repositories

import app.peter.s526.data.source.local.S526Data
import app.peter.s526.domain.model.Book
import app.peter.s526.data.source.remote.Api
import javax.inject.Inject

class LibraryRepository @Inject constructor (
    private val api: Api,
    private val data: S526Data
) {

    suspend fun getNewBook() = api.getNewBooks()
    suspend fun getDetailBook(isbn: String) = api.getBookDetail(isbn)
    suspend fun getSearchBook(query: String, page: String) = api.getSearchBook(query, page)

    fun addBookmark(book: Book) {
        if (data.bookmark.contains(book)) return
        data.bookmark.add(book)
    }

    fun deleteBookmark(book: Book) {
        data.bookmark.remove(book)
    }

    fun checkBookmark(book: Book): Boolean = data.bookmark.contains(book)

    fun updateBookmark(bookmark: List<Book>) {
        data.bookmark.clear()
        data.bookmark.addAll(bookmark)
    }

    fun getBookmark() = data.bookmark

    fun addHistory(query: String) {
        if (data.history.contains(query)) return
        data.history.add(query)
    }

    fun getHistory() = data.history
}