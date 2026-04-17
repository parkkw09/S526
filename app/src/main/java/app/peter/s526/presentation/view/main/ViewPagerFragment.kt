package app.peter.s526.presentation.view.main

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.findNavController
import app.peter.s526.R
import app.peter.s526.application.Log
import app.peter.s526.databinding.FragmentViewPagerBinding
import app.peter.s526.presentation.util.TabType
import app.peter.s526.presentation.util.viewBinding
import com.google.android.material.tabs.TabLayoutMediator
import com.google.android.play.core.review.ReviewException
import com.google.android.play.core.review.ReviewInfo
import com.google.android.play.core.review.model.ReviewErrorCode

class ViewPagerFragment : Fragment(R.layout.fragment_view_pager) {

    private val viewModel: MainViewModel by activityViewModels()
    private val binding by viewBinding(FragmentViewPagerBinding::bind)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.d(TAG, "onViewCreated()")
        binding.apply {
            viewPager.adapter = BookPagerAdapter(this@ViewPagerFragment)

            TabLayoutMediator(tabs, viewPager) { tab, position ->
                tab.setIcon(getTabIcon(position))
                tab.text = getTabTitle(position)
            }.attach()

            (activity as AppCompatActivity).setSupportActionBar(toolbar)

            viewModel.appName?.let { appName.text = it }
            review.setOnClickListener { requestReview() }
            search.setOnClickListener {
                val direction = ViewPagerFragmentDirections
                    .actionViewPagerFragmentToSearchFragment("")
                viewModel.setCurrentSearchQuery("")
                it.findNavController().navigate(direction)
            }
        }
    }

    private fun getTabIcon(position: Int): Int = when (position) {
        TabType.S526_NEW -> R.drawable.book
        TabType.S526_BOOKMARK -> R.drawable.bookmark
        TabType.S526_HISTORY -> R.drawable.history
        else -> throw IndexOutOfBoundsException()
    }

    private fun getTabTitle(position: Int): String? = when (position) {
        TabType.S526_NEW -> getString(R.string.new_book)
        TabType.S526_BOOKMARK -> getString(R.string.bookmark)
        TabType.S526_HISTORY -> getString(R.string.history)
        else -> null
    }

    /**
     * Google Play In-App Review 플로우를 요청한다.
     * 실제 다이얼로그 표시 여부는 Google Play 내부 정책에 따라 결정된다.
     */
    private fun requestReview() {
        Log.d(TAG, "requestReview()")
        val manager = viewModel.reviewManager ?: return
        val activity = activity ?: return

        manager.requestReviewFlow().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val reviewInfo: ReviewInfo = task.result
                manager.launchReviewFlow(activity, reviewInfo)
                    .addOnCompleteListener { launchTask ->
                        Log.d(TAG, "review flow completed: ${launchTask.isComplete}")
                    }
                    .addOnFailureListener { e ->
                        Log.d(TAG, "review flow failure: ${e.localizedMessage}")
                    }
            } else {
                try {
                    @ReviewErrorCode val errorCode: Int =
                        (task.exception as ReviewException).errorCode
                    Log.d(TAG, "review request failed: $errorCode")
                } catch (e: Exception) {
                    Log.d(TAG, "review request exception: ${e.localizedMessage}")
                }
            }
        }
    }

    companion object {
        private const val TAG = "ViewPagerFragment"
    }
}
