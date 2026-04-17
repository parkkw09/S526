package app.peter.s526.domain.usecase.impl

import app.peter.s526.domain.model.Book
import app.peter.s526.domain.model.BookDetail
import app.peter.s526.domain.repository.LibraryRepository
import app.peter.s526.domain.usecase.BookmarkUseCase
import javax.inject.Inject

class BookmarkUseCaseImpl @Inject constructor(
    private val repository: LibraryRepository,
) : BookmarkUseCase {

    override fun addBookmark(detail: BookDetail) {
        repository.addBookmark(detail.toBook())
    }

    override fun deleteBookmark(detail: BookDetail) {
        repository.deleteBookmark(detail.toBook())
    }

    override fun isBookmarked(detail: BookDetail): Boolean =
        repository.isBookmarked(detail.toBook())

    override fun updateBookmark(bookmark: List<Book>) {
        repository.updateBookmark(bookmark)
    }

    override fun getBookmark(): List<Book> = repository.getBookmark()

    private fun BookDetail.toBook(): Book = Book(
        isbn = isbn13,
        title = title,
        subtitle = subtitle,
        image = image,
        url = url,
    )
}
