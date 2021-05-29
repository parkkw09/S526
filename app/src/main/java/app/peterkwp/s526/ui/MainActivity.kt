package app.peterkwp.s526.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import app.peterkwp.s526.databinding.ActivityMainBinding
import app.peterkwp.s526.ui.viewmodel.MainViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private lateinit var viewModel: MainViewModel
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }
}