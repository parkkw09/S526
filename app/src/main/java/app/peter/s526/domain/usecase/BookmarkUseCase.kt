package app.peter.s526.domain.usecase

import app.peter.s526.domain.model.Book
import app.peter.s526.domain.model.DetailBook
import app.peter.s526.data.repositories.LibraryRepository
import javax.inject.Inject

class BookmarkUseCase @Inject constructor(
    private val repository: LibraryRepository
) {

    fun addBookmark(detailBook: DetailBook) {
        repository.addBookmark(Book(detailBook.isbn13,
            detailBook.title,
            detailBook.subtitle,
            detailBook.price,
            detailBook.image,
            detailBook.url))
    }

    fun deleteBookmark(detailBook: DetailBook) {
        repository.deleteBookmark(Book(detailBook.isbn13,
            detailBook.title,
            detailBook.subtitle,
            detailBook.price,
            detailBook.image,
            detailBook.url))
    }

    fun checkBookmark(detailBook: DetailBook): Boolean {
        return repository.checkBookmark(Book(detailBook.isbn13,
            detailBook.title,
            detailBook.subtitle,
            detailBook.price,
            detailBook.image,
            detailBook.url))
    }

    fun updateBookmark(bookmark: List<Book>) = repository.updateBookmark(bookmark)

    fun getBookmark(): List<Book> = repository.getBookmark()

        companion object {
        private const val TAG = "BookmarkUseCase"
    }
}