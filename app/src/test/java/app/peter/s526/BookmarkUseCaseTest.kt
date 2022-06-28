package app.peter.s526

import app.peter.s526.data.repositories.LibraryRepository
import app.peter.s526.data.repositories.impl.LibraryRepositoryImpl
import app.peter.s526.data.source.local.S526Data
import app.peter.s526.data.source.remote.Api
import app.peter.s526.domain.model.NewBook
import app.peter.s526.domain.model.NewDetailBook
import app.peter.s526.domain.model.NewPdf
import app.peter.s526.domain.usecase.impl.BookmarkUseCaseImpl
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class BookmarkUseCaseTest {

    @Mock
    lateinit var api: Api
    lateinit var data: S526Data
    lateinit var repository: LibraryRepository

    @Before
    fun setup() {
        MockitoAnnotations.initMocks(this)
        data = S526Data()
        repository = LibraryRepositoryImpl(api, data)
    }

    @Test
    fun `북마크 추가 테스트 - 주어진 샘플이 정상적으로 저장되어야 한다`() {
        val useCase = BookmarkUseCaseImpl(repository)
        useCase.addBookmark(DETAIL_BOOK)
        assertEquals(BOOK_LIST, useCase.getBookmark())
    }

    @Test
    fun `북마크 삭 테스트 - 주어진 샘플이 정상적으로 삭제되어야 한다`() {
        val useCase = BookmarkUseCaseImpl(repository)
        useCase.addBookmark(DETAIL_BOOK)
        useCase.deleteBookmark(DETAIL_BOOK)
        assertEquals(EMPTY_BOOK_LIST, useCase.getBookmark())
    }

    @Test
    fun `북마크 확인 테스트 - 주어진 샘플이 존재하는지 확인 한다`() {
        val useCase = BookmarkUseCaseImpl(repository)
        useCase.addBookmark(DETAIL_BOOK)
        assertTrue(useCase.checkBookmark(DETAIL_BOOK))
    }

    @Test
    fun `북마크 업데이트 테스트 - 주어진 샘플이 업데이트 되는지 확인한다`() {
        val useCase = BookmarkUseCaseImpl(repository)
        useCase.addBookmark(DETAIL_BOOK)
        useCase.updateBookmark(UPDATE_BOOK_LIST)
        assertEquals(3, useCase.getBookmark().size)
    }

    companion object {
        private val DETAIL_BOOK = NewDetailBook(error = "0",
             title = "Python Notes for Professionals",
             subtitle = "",
             authors = "Stack Overflow Community",
             publisher = "Self-publishing",
             language = "English",
             isbn10 = "1621860582",
             isbn13 = "1001621860589",
             pages = "855",
             year = "2018",
             rating = "0",
             desc = "The Python Notes for Professionals book is compiled from Stack Overflow Documentation, the content is written by the beautiful people at Stack Overflow....",
             price = "\$0.00",
             image = "https://itbook.store/img/books/1001621860589.png",
             url = "https://itbook.store/books/1001621860589",
             pdf = NewPdf(freeBook = "https://www.dbooks.org/d/5591650063-1621860247-f0dcab9437a281b1/")
        )
        private val ITEM_BOOK = NewBook(isbn = "1001621860589",
            title = "Python Notes for Professionals",
            subtitle = "",
            price = "\$0.00",
            image = "https://itbook.store/img/books/1001621860589.png",
            url = "https://itbook.store/books/1001621860589")
        private val BOOK_LIST = listOf(ITEM_BOOK)
        private val EMPTY_BOOK_LIST = listOf<NewBook>()
        private val UPDATE_BOOK_LIST = listOf(ITEM_BOOK, ITEM_BOOK, ITEM_BOOK)
    }
}