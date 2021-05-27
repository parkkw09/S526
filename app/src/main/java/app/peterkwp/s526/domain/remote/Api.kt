package app.peterkwp.s526.domain.remote

import app.peterkwp.s526.util.Utils
import retrofit2.http.GET
import retrofit2.http.Path

interface Api {

    @GET(Utils.VERSION + "/new")
    suspend fun getNewBooks()

    @GET(Utils.VERSION + "/books/{isbn}")
    suspend fun getBookDetail(@Path("isbn") isbn: String)

    @GET(Utils.VERSION + "/search/{query}/{page}")
    suspend fun getSearchBook(@Path("query") isbn: String, @Path("page") page: String = "1")
}