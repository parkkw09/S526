package app.peter.s526.data.source.remote

import app.peter.s526.BuildConfig
import app.peter.s526.data.entities.DetailBook
import app.peter.s526.data.entities.ListBook
import retrofit2.http.GET
import retrofit2.http.Query

interface Api {

    @GET(RemoteConst.COMMAND + RemoteConst.COMMAND_BEST_SELLERS_OVERVIEW)
    suspend fun getCurrentBestSeller(@Query(RemoteConst.KEY) key: String = BuildConfig.NY_TIMES_BOOK_KEY): ListBook

    @GET(RemoteConst.COMMAND + RemoteConst.COMMAND_BEST_SELLERS_FULL_OVERVIEW)
    suspend fun getCurrentBestSellerFull(@Query(RemoteConst.KEY) key: String = BuildConfig.NY_TIMES_BOOK_KEY): ListBook

    @GET(RemoteConst.COMMAND + RemoteConst.COMMAND_BEST_SELLERS_FROM_TARGET)
    suspend fun getCurrentBestSellerFromTarget(@Query(RemoteConst.AGE) age: String,
                                               @Query(RemoteConst.KEY) key: String = BuildConfig.NY_TIMES_BOOK_KEY): ListBook

    @GET(RemoteConst.COMMAND + RemoteConst.COMMAND_REVIEW)
    suspend fun getReview(@Query(RemoteConst.ISBN) isbn: Long,
                          @Query(RemoteConst.KEY) key: String = BuildConfig.NY_TIMES_BOOK_KEY): DetailBook
}