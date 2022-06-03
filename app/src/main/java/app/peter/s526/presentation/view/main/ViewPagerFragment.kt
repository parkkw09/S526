package app.peter.s526.presentation.view.main

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.findNavController
import app.peter.s526.R
import app.peter.s526.application.Log
import app.peter.s526.databinding.FragmentViewPagerBinding
import app.peter.s526.presentation.util.TabType
import com.google.android.gms.appset.AppSetIdInfo
import com.google.android.gms.tasks.Task
import com.google.android.material.tabs.TabLayoutMediator
import com.google.android.play.core.review.ReviewException
import com.google.android.play.core.review.ReviewInfo
import com.google.android.play.core.review.model.ReviewErrorCode

class ViewPagerFragment: Fragment() {

    private val viewModel: MainViewModel by activityViewModels()

    private var _binding: FragmentViewPagerBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    private fun getTabIcon(position: Int): Int {
        return when (position) {
            TabType.S526_NEW -> R.drawable.book
            TabType.S526_BOOKMARK -> R.drawable.bookmark
            TabType.S526_HISTORY -> R.drawable.history
            else -> throw IndexOutOfBoundsException()
        }
    }

    private fun getTabTitle(position: Int): String? {
        return when (position) {
            TabType.S526_NEW -> getString(R.string.new_book)
            TabType.S526_BOOKMARK -> getString(R.string.bookmark)
            TabType.S526_HISTORY -> getString(R.string.history)
            else -> null
        }
    }

    private fun testReviewManager() {
        Log.d(TAG, "testReviewManager()")
        viewModel.reviewManager?.let { manager ->
            val request: Task<ReviewInfo> = manager.requestReviewFlow()
            request.addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    // We can get the ReviewInfo object
                    val reviewInfo: ReviewInfo = task.result
                    Log.d(TAG, "OnComplete() reviewInfo = " + reviewInfo.describeContents())
                    activity?.let { activity ->
                        val flow: Task<Void> = manager.launchReviewFlow(activity, reviewInfo)
                        flow.addOnCompleteListener { launchTask ->
                            // The flow has finished. The API does not indicate whether the user
                            // reviewed or not, or even whether the review dialog was shown. Thus, no
                            // matter the result, we continue our app flow.
                            Log.d(TAG, "OnComplete() ${launchTask.isComplete}")
                        }
                        flow.addOnFailureListener { e -> Log.d(TAG, "OnFailure() [${e.localizedMessage}]") }
                        flow.addOnCanceledListener { Log.d(TAG, "OnCancel()") }
                    }
                } else {
                    // There was some problem, log or handle the error code.
                    @ReviewErrorCode val reviewErrorCode: Int =
                        (task.exception as ReviewException).errorCode
                    Log.d(TAG, "Successful false =$reviewErrorCode")
                }
            }
        }
    }

    private fun testAppSetID() {
        Log.d(TAG, "testAppSetID()")
        viewModel.client?.let { client ->
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

                activity?.run {
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
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentViewPagerBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.d(TAG, "onViewCreated()")
        binding.apply {
            viewPager.adapter = BookPagerAdapter(this@ViewPagerFragment)

            // Set the icon and text for each tab
            TabLayoutMediator(tabs, viewPager) { tab, position ->
                tab.setIcon(getTabIcon(position))
                tab.text = getTabTitle(position)
            }.attach()

            (activity as AppCompatActivity).setSupportActionBar(toolbar)

            viewModel.appName?.let { appName.text = it }
            appName.setOnClickListener { testAppSetID() }
            review.setOnClickListener { testReviewManager() }
            search.setOnClickListener {
                val direction = ViewPagerFragmentDirections.actionViewPagerFragmentToSearchFragment("")
                viewModel.setCurrentSearchQuery("")
                it.findNavController().navigate(direction)
            }
        }
    }

    companion object {
        private const val TAG = "ViewPagerFragment"
    }
}