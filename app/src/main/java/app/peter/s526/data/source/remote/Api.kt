package app.peter.s526.data.source.remote

import app.peter.s526.BuildConfig
import app.peter.s526.data.entities.DetailBook
import app.peter.s526.data.entities.ListBook
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface Api {

    @GET(RemoteConst.COMMAND + RemoteConst.COMMAND_BEST_SELLERS)
    suspend fun getNewBooks(@Query(RemoteConst.KEY) key: String = BuildConfig.NY_TIMES_BOOK_KEY): ListBook

    @GET(RemoteConst.COMMAND + "/books/{isbn}")
    suspend fun getBookDetail(@Path("isbn") isbn: String): DetailBook

    @GET(RemoteConst.COMMAND + "/search/{query}/{page}")
    suspend fun getSearchBook(@Path("query") query: String, @Path("page") page: String = "1"): ListBook
}