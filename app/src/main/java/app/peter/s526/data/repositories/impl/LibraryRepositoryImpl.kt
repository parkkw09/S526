package app.peter.s526.data.repositories.impl

import app.peter.s526.data.entities.Book
import app.peter.s526.data.repositories.LibraryRepository
import app.peter.s526.data.source.local.S526Data
import app.peter.s526.data.source.remote.Api

import javax.inject.Inject

class LibraryRepositoryImpl @Inject constructor (
    private val remoteSource: Api,
    private val localSource: S526Data
): LibraryRepository {

    override suspend fun getNewBook() = remoteSource.getNewBooks()
    override suspend fun getDetailBook(isbn: String) = remoteSource.getBookDetail(isbn)
    override suspend fun getSearchBook(query: String, page: String) = remoteSource.getSearchBook(query, page)

    override fun addBookmark(book: Book) {
        if (localSource.bookmark.contains(book)) return
        localSource.bookmark.add(book)
    }

    override fun deleteBookmark(book: Book) {
        localSource.bookmark.remove(book)
    }

    override fun checkBookmark(book: Book): Boolean = localSource.bookmark.contains(book)

    override fun updateBookmark(bookmark: List<Book>) {
        localSource.bookmark.clear()
        localSource.bookmark.addAll(bookmark)
    }

    override fun getBookmark() = localSource.bookmark

    override fun addHistory(query: String) {
        if (localSource.history.contains(query)) return
        localSource.history.add(query)
    }

    override fun getHistory() = localSource.history
}