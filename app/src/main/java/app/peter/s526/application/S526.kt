package app.peter.s526.application

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class S526: Application() {

    override fun onCreate() {
        Log.d(TAG, "onCreate()")
        super.onCreate()
    }

    companion object {
        private const val TAG = "Application"
    }
}