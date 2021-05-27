package app.peterkwp.s526.di

import app.peterkwp.s526.domain.remote.Api
import app.peterkwp.s526.util.Utils
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
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
        converter: Converter.Factory): Api
            = Retrofit.Builder()
        .baseUrl(Utils.URL)
        .client(okHttpClient)
        .addConverterFactory(converter)
        .build()
        .create(Api::class.java)
}