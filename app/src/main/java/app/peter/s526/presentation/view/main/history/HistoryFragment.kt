package app.peter.s526.presentation.view.main.history

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import app.peter.s526.R
import app.peter.s526.application.Log
import app.peter.s526.databinding.FragmentHistoryBinding
import app.peter.s526.presentation.util.viewBinding
import app.peter.s526.presentation.view.main.MainViewModel
import app.peter.s526.presentation.view.main.ViewPagerFragmentDirections
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class HistoryFragment : Fragment(R.layout.fragment_history) {

    private val viewModel: MainViewModel by activityViewModels()
    private val binding by viewBinding(FragmentHistoryBinding::bind)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.d(TAG, "onViewCreated()")

        val adapter = HistoryAdapter { query ->
            Log.d(TAG, "item click [$query]")
            navigateToSearch(view, query)
        }

        binding.historyList.apply {
            layoutManager = LinearLayoutManager(context)
            this.adapter = adapter
        }

        viewModel.history.observe(viewLifecycleOwner) { historyList ->
            Log.d(TAG, "history updated [size=${historyList.size}]")
            adapter.submitList(historyList)
        }

        viewModel.getHistory()
    }

    private fun navigateToSearch(view: View, query: String) {
        val direction = ViewPagerFragmentDirections
            .actionViewPagerFragmentToSearchFragment(query)
        view.findNavController().navigate(direction)
    }

    companion object {
        private const val TAG = "HistoryFragment"
    }
}
