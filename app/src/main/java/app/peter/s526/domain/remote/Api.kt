package app.peter.s526.domain.remote

import app.peter.s526.domain.data.DomainConst
import app.peter.s526.domain.model.DetailBook
import app.peter.s526.domain.model.ListBook
import retrofit2.http.GET
import retrofit2.http.Path

interface Api {

    @GET(DomainConst.VERSION + "/new")
    suspend fun getNewBooks(): ListBook

    @GET(DomainConst.VERSION + "/books/{isbn}")
    suspend fun getBookDetail(@Path("isbn") isbn: String): DetailBook

    @GET(DomainConst.VERSION + "/search/{query}/{page}")
    suspend fun getSearchBook(@Path("query") query: String, @Path("page") page: String = "1"): ListBook
}