package app.peter.s526.data.source.remote

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import retrofit2.Converter
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

@Module
@InstallIn(SingletonComponent::class)
class ApiModule {

    @Provides
    fun provideConverterFactoryJson(): Converter.Factory = GsonConverterFactory.create()

    @Provides
    fun provideApi(
        okHttpClient: OkHttpClient,
        converter: Converter.Factory): Api {
        val clientWithUserAgent = okHttpClient.newBuilder()
            .addInterceptor(Interceptor { chain ->
                val request = chain.request().newBuilder()
                    .header("User-Agent", "S526App (peter.app)")
                    .build()
                chain.proceed(request)
            })
            .build()
        return Retrofit.Builder()
            .baseUrl(RemoteConst.URL)
            .client(clientWithUserAgent)
            .addConverterFactory(converter)
            .build()
            .create(Api::class.java)
    }
}