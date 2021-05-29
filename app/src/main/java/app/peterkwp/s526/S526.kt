package app.peterkwp.s526

import android.app.Application
import app.peterkwp.s526.util.Log
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