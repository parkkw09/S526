package app.peter.s526

import app.peter.s526.data.repositories.impl.LibraryRepositoryImpl
import app.peter.s526.data.source.local.LocalBookDataSource
import app.peter.s526.data.source.remote.Api
import app.peter.s526.domain.model.Book
import app.peter.s526.domain.model.BookDetail
import app.peter.s526.domain.repository.LibraryRepository
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
    private lateinit var dataSource: LocalBookDataSource
    private lateinit var repository: LibraryRepository

    @Before
    fun setup() {
        MockitoAnnotations.initMocks(this)
        dataSource = LocalBookDataSource()
        repository = LibraryRepositoryImpl(api, dataSource)
    }

    @Test
    fun `북마크 추가 - 주어진 샘플이 정상 저장되어야 한다`() {
        val useCase = BookmarkUseCaseImpl(repository)
        useCase.addBookmark(DETAIL_BOOK)
        assertEquals(BOOK_LIST, useCase.getBookmark())
    }

    @Test
    fun `북마크 삭제 - 주어진 샘플이 정상 삭제되어야 한다`() {
        val useCase = BookmarkUseCaseImpl(repository)
        useCase.addBookmark(DETAIL_BOOK)
        useCase.deleteBookmark(DETAIL_BOOK)
        assertEquals(emptyList(), useCase.getBookmark())
    }

    @Test
    fun `북마크 여부 확인 - 저장된 책이 존재해야 한다`() {
        val useCase = BookmarkUseCaseImpl(repository)
        useCase.addBookmark(DETAIL_BOOK)
        assertTrue(useCase.isBookmarked(DETAIL_BOOK))
    }

    @Test
    fun `북마크 업데이트 - 전달한 목록이 그대로 반영되어야 한다`() {
        val useCase = BookmarkUseCaseImpl(repository)
        useCase.addBookmark(DETAIL_BOOK)
        useCase.updateBookmark(UPDATE_BOOK_LIST)
        assertEquals(UPDATE_BOOK_LIST.size, useCase.getBookmark().size)
    }

    companion object {
        private val DETAIL_BOOK = BookDetail(
            title = "Python Notes for Professionals",
            subtitle = "",
            authors = "Stack Overflow Community",
            publisher = "Self-publishing",
            language = "English",
            isbn10 = "1621860582",
            isbn13 = "1001621860589",
            pages = "855",
            year = "2018",
            desc = "The Python Notes for Professionals book is compiled from Stack Overflow Documentation.",
            image = "https://covers.openlibrary.org/b/id/258027-L.jpg",
            url = "https://openlibrary.org/works/OL12345W",
        )
        private val ITEM_BOOK = Book(
            isbn = "1001621860589",
            title = "Python Notes for Professionals",
            subtitle = "",
            image = "https://covers.openlibrary.org/b/id/258027-L.jpg",
            url = "https://openlibrary.org/works/OL12345W",
        )
        private val BOOK_LIST = listOf(ITEM_BOOK)
        private val UPDATE_BOOK_LIST = listOf(
            ITEM_BOOK,
            ITEM_BOOK.copy(isbn = "1"),
            ITEM_BOOK.copy(isbn = "2"),
        )
    }
}
