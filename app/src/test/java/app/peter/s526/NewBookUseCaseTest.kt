package app.peter.s526

import app.peter.s526.data.repositories.LibraryRepository
import app.peter.s526.data.repositories.impl.LibraryRepositoryImpl
import app.peter.s526.data.source.local.S526Data
import app.peter.s526.data.source.remote.Api
import app.peter.s526.domain.model.NewBook
import app.peter.s526.domain.model.NewListBook
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
            mockServer.enqueue(MockResponse().setBody(Gson().toJson(RESPONSE)))
            val list = useCase.getNewBook()
            assertEquals(listOf(ITEM_BOOK), list)
        }
    }

    companion object {
        private val ITEM_BOOK = NewBook(isbn = "1001621860589",
            title = "Python Notes for Professionals",
            subtitle = "",
            price = "\$0.00",
            image = "https://itbook.store/img/books/1001621860589.png",
            url = "https://itbook.store/books/1001621860589")
        private val RESPONSE = NewListBook(error = "0", total = "20", page = "1", books = listOf(ITEM_BOOK))
    }
}