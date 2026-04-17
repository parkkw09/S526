package app.peter.s526

import app.peter.s526.data.entities.OLSearchDoc
import app.peter.s526.data.entities.OLSearchResponse
import app.peter.s526.data.repositories.impl.LibraryRepositoryImpl
import app.peter.s526.data.source.local.LocalBookDataSource
import app.peter.s526.data.source.remote.Api
import app.peter.s526.domain.model.Book
import app.peter.s526.domain.repository.LibraryRepository
import app.peter.s526.domain.usecase.impl.NewBookUseCaseImpl
import com.google.gson.Gson
import kotlinx.coroutines.runBlocking
import okhttp3.OkHttpClient
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Before
import org.junit.Test
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import kotlin.test.assertEquals

class NewBookUseCaseTest {

    private val mockServer = MockWebServer()
    private lateinit var repository: LibraryRepository

    @Before
    fun setup() {
        mockServer.start()
        val api = Retrofit.Builder()
            .baseUrl(mockServer.url("/"))
            .client(OkHttpClient.Builder().build())
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(Api::class.java)
        repository = LibraryRepositoryImpl(api, LocalBookDataSource())
    }

    @After
    fun shutdown() {
        mockServer.shutdown()
    }

    @Test
    fun `신간 도서 조회 - 정상적으로 데이터를 읽어와야 한다`() = runBlocking {
        mockServer.enqueue(MockResponse().setBody(Gson().toJson(OL_RESPONSE)))

        val useCase = NewBookUseCaseImpl(repository)
        val result = useCase.getNewBook()

        assertEquals(1, result.books.size)
        assertEquals(EXPECTED_BOOK.title, result.books[0].title)
        assertEquals(EXPECTED_BOOK.isbn, result.books[0].isbn)
    }

    companion object {
        private val OL_SEARCH_DOC = OLSearchDoc(
            key = "/works/OL12345W",
            title = "Python Notes for Professionals",
            subtitle = null,
            isbn = listOf("1001621860589"),
            coverId = 258027,
            authorName = listOf("Stack Overflow Community"),
            firstPublishYear = 2018,
            numberOfPagesMedian = 855,
            publisher = listOf("Self-publishing"),
            language = listOf("eng"),
        )
        private val OL_RESPONSE = OLSearchResponse(
            numFound = 20,
            start = 0,
            docs = listOf(OL_SEARCH_DOC),
        )
        private val EXPECTED_BOOK = Book(
            isbn = "1001621860589",
            title = "Python Notes for Professionals",
            subtitle = "",
            image = "https://covers.openlibrary.org/b/id/258027-L.jpg",
            url = "https://openlibrary.org/works/OL12345W",
        )
    }
}
