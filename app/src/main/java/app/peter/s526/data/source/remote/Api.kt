package app.peter.s526.data.source.remote

import app.peter.s526.data.entities.OLEditionResponse
import app.peter.s526.data.entities.OLSearchResponse
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface Api {

    @GET("search.json")
    suspend fun getNewBooks(
        @Query("q") query: String = "programming",
        @Query("sort") sort: String = "new",
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 20,
        @Query("fields") fields: String = DEFAULT_FIELDS
    ): OLSearchResponse

    @GET("isbn/{isbn}.json")
    suspend fun getBookDetail(@Path("isbn") isbn: String): OLEditionResponse

    @GET("search.json")
    suspend fun getSearchBook(
        @Query("q") query: String,
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 10,
        @Query("fields") fields: String = DEFAULT_FIELDS
    ): OLSearchResponse

    companion object {
        private const val DEFAULT_FIELDS =
            "key,title,subtitle,isbn,cover_i,author_name,first_publish_year,number_of_pages_median,publisher,language"
    }
}