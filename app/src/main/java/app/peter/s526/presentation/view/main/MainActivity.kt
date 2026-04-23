package app.peter.s526.presentation.view.main

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.navigation.compose.rememberNavController
import app.peter.s526.R
import app.peter.s526.application.Log
import app.peter.s526.presentation.ui.navigation.S526NavHost
import app.peter.s526.presentation.ui.theme.S526Theme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val viewModel by viewModels<MainViewModel>()

    private fun initializeApplication() {
        viewModel.initClient(applicationContext, resources.getString(R.string.app_name))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(TAG, "onCreate()")

        initializeApplication()

        setContent {
            S526Theme {
                val navController = rememberNavController()
                S526NavHost(navController = navController)
            }
        }
    }

    companion object {
        private const val TAG = "MainActivity"
    }
}