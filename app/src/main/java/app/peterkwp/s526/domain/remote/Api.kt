package app.peterkwp.s526.domain.remote

import app.peterkwp.s526.domain.data.DomainConst
import retrofit2.http.GET
import retrofit2.http.Path

interface Api {

    @GET(DomainConst.VERSION + "/new")
    suspend fun getNewBooks()

    @GET(DomainConst.VERSION + "/books/{isbn}")
    suspend fun getBookDetail(@Path("isbn") isbn: String)

    @GET(DomainConst.VERSION + "/search/{query}/{page}")
    suspend fun getSearchBook(@Path("query") isbn: String, @Path("page") page: String = "1")
}