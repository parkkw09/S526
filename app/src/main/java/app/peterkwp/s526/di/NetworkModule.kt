package app.peterkwp.s526.di

import app.peterkwp.s526.util.Log
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class NetworkModule {

    @Provides
    fun provideLoggingInterceptor(): HttpLoggingInterceptor {
        val logger = HttpLoggingInterceptor(object: HttpLoggingInterceptor.Logger {
            override fun log(message: String) {
                var parseMessage = message
                Log.d(TAG, parseMessage)
                if (parseMessage.contains("END")) {
                    Log.d(TAG, "\n")
                    parseMessage += "\n"
                }
            }
        })
        return logger.apply { level = HttpLoggingInterceptor.Level.BODY }
    }

    @Singleton
    @Provides
    fun provideOkHttpClient(loggingInterceptor: HttpLoggingInterceptor): OkHttpClient
            = OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor)
        .build()

    companion object {
        private const val TAG = "S526NetworkModule"
    }
}