package app.peterkwp.s526.domain.usecase

import app.peterkwp.s526.domain.model.Book
import app.peterkwp.s526.domain.model.DetailBook
import app.peterkwp.s526.domain.repository.LibraryRepository
import javax.inject.Inject

class BookmarkUseCase @Inject constructor(
    private val repository: LibraryRepository
) {

    fun addBookmark(isbn: String, book: DetailBook) = repository.addBookmark(isbn, book)

    fun deleteBookmark(isbn: String) = repository.deleteBookmark(isbn)

    fun checkBookmark(isbn: String): Boolean = repository.checkBookmark(isbn)

    fun getBookmark(): List<Book> {
        val hashMap = repository.getBookmark()
        val list = mutableListOf<Book>()
        hashMap.forEach { (_, detailBook) ->
            list.add(Book(detailBook.isbn13,
                          detailBook.title,
                          detailBook.subtitle,
                          detailBook.price,
                          detailBook.image,
                          detailBook.url))
        }
        return list
    }

        companion object {
        private const val TAG = "BookmarkUseCase"
    }
}