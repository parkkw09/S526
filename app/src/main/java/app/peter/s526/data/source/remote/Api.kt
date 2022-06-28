package app.peter.s526.data.source.remote

import app.peter.s526.data.entities.DetailBook
import app.peter.s526.data.entities.ListBook
import retrofit2.http.GET
import retrofit2.http.Path

interface Api {

    @GET(RemoteConst.VERSION + "/new")
    suspend fun getNewBooks(): ListBook

    @GET(RemoteConst.VERSION + "/books/{isbn}")
    suspend fun getBookDetail(@Path("isbn") isbn: String): DetailBook

    @GET(RemoteConst.VERSION + "/search/{query}/{page}")
    suspend fun getSearchBook(@Path("query") query: String, @Path("page") page: String = "1"): ListBook
}