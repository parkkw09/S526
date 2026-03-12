package app.peter.s526

import app.peter.s526.data.entities.OLSearchDoc
import app.peter.s526.data.entities.OLSearchResponse
import app.peter.s526.data.repositories.LibraryRepository
import app.peter.s526.data.repositories.impl.LibraryRepositoryImpl
import app.peter.s526.data.source.local.S526Data
import app.peter.s526.data.source.remote.Api
import app.peter.s526.domain.model.NewBook
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
    lateinit var repository: LibraryRepository

    @Before
    fun setup() {
        mockServer.start()
        val api = Retrofit.Builder()
            .baseUrl(mockServer.url("/"))
            .client(OkHttpClient.Builder().build())
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(Api::class.java)
        val data = S526Data()
        repository = LibraryRepositoryImpl(api, data)
    }

    @After
    fun shutdown() {
        mockServer.shutdown()
    }

    @Test
    fun `새로운 책 읽어오기 테스트 - 정상적으로 데이터를 읽어와야 한다`() {
        val useCase = NewBookUseCaseImpl(repository)
        runBlocking {
            mockServer.enqueue(MockResponse().setBody(Gson().toJson(OL_RESPONSE)))
            val list = useCase.getNewBook()
            assertEquals(1, list.size)
            assertEquals(EXPECTED_BOOK.title, list[0].title)
            assertEquals(EXPECTED_BOOK.isbn, list[0].isbn)
        }
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
            language = listOf("eng")
        )
        private val OL_RESPONSE = OLSearchResponse(
            numFound = 20,
            start = 0,
            docs = listOf(OL_SEARCH_DOC)
        )
        private val EXPECTED_BOOK = NewBook(
            isbn = "1001621860589",
            title = "Python Notes for Professionals",
            subtitle = "",
            price = "",
            image = "https://covers.openlibrary.org/b/id/258027-L.jpg",
            url = "https://openlibrary.org/works/OL12345W"
        )
    }
}