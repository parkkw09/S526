package app.peter.s526.domain.usecase.impl

import app.peter.s526.data.repositories.LibraryRepository
import app.peter.s526.domain.model.NewBook
import app.peter.s526.domain.model.NewDetailBook
import app.peter.s526.domain.translator.BookTranslator
import app.peter.s526.domain.usecase.BookmarkUseCase
import javax.inject.Inject

class BookmarkUseCaseImpl @Inject constructor(
    private val repository: LibraryRepository
): BookmarkUseCase {

    override fun addBookmark(detailBook: NewDetailBook) {
        repository.addBookmark(BookTranslator.getBookByDetailBook(detailBook))
    }

    override fun deleteBookmark(detailBook: NewDetailBook) {
        repository.deleteBookmark(BookTranslator.getBookByDetailBook(detailBook))
    }

    override fun checkBookmark(detailBook: NewDetailBook): Boolean {
        return repository.checkBookmark(BookTranslator.getBookByDetailBook(detailBook))
    }

    override fun updateBookmark(bookmark: List<NewBook>) {
        repository.updateBookmark(BookTranslator.getBookList(bookmark))
    }

    override fun getBookmark(): List<NewBook> {
        return repository.getBookmark().map {
            BookTranslator.getNewBook(it)
        }
    }

    companion object {
        private const val TAG = "BookmarkUseCase"
    }
}