package app.peter.s526.data.repositories.impl

import app.peter.s526.data.mapper.BookMapper
import app.peter.s526.data.source.local.LocalBookDataSource
import app.peter.s526.data.source.remote.Api
import app.peter.s526.domain.model.Book
import app.peter.s526.domain.model.BookDetail
import app.peter.s526.domain.model.BookList
import app.peter.s526.domain.repository.LibraryRepository
import javax.inject.Inject

class LibraryRepositoryImpl @Inject constructor(
    private val remoteSource: Api,
    private val localSource: LocalBookDataSource,
) : LibraryRepository {

    override suspend fun getNewBooks(page: String): BookList {
        val pageInt = page.toIntOrNull() ?: 1
        return BookMapper.toBookList(remoteSource.getNewBooks(page = pageInt), page)
    }

    override suspend fun getBookDetail(isbn: String): BookDetail =
        BookMapper.toBookDetail(remoteSource.getBookDetail(isbn), isbn)

    override suspend fun searchBooks(query: String, page: String): BookList {
        val pageInt = page.toIntOrNull() ?: 1
        return BookMapper.toBookList(remoteSource.getSearchBook(query, pageInt), page)
    }

    override fun addBookmark(book: Book) = localSource.addBookmark(book)

    override fun deleteBookmark(book: Book) = localSource.removeBookmark(book)

    override fun isBookmarked(book: Book): Boolean = localSource.containsBookmark(book)

    override fun updateBookmark(bookmark: List<Book>) = localSource.replaceBookmark(bookmark)

    override fun getBookmark(): List<Book> = localSource.bookmark

    override fun addHistory(query: String) = localSource.addHistory(query)

    override fun getHistory(): List<String> = localSource.history
}
