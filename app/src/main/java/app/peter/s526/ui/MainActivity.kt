package app.peter.s526.ui

import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import app.peter.s526.databinding.ActivityMainBinding
import app.peter.s526.ui.viewmodel.MainViewModel
import app.peter.s526.util.Log
import com.google.android.gms.appset.AppSet
import com.google.android.gms.appset.AppSetIdInfo
import com.google.android.gms.tasks.Task
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private lateinit var viewModel: MainViewModel
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val client = AppSet.getClient(applicationContext)
        val task: Task<AppSetIdInfo> = client.appSetIdInfo

        task.addOnSuccessListener {
            // Determine current scope of app set ID.
            val scope: Int = it.scope

            // Read app set ID value, which uses version 4 of the
            // universally unique identifier (UUID) format.
            val id: String = it.id

            Log.d(TAG, "onCreate() App set ID scope[$scope]")
            Log.d(TAG, "onCreate() App set ID id[$id]")
            Log.d(TAG, "onCreate() App set ID Thread id[${Thread.currentThread()}]")

            val dialog = AlertDialog.Builder(this)
                .setTitle("App set ID scope[$scope]")
                .setMessage("id[$id]")
                .setPositiveButton("OK") { dialog, _ ->
                    dialog.dismiss()
                }
                .create()
            dialog.show()
        }
    }

    companion object {
        private const val TAG = "MainActivity"
    }
}