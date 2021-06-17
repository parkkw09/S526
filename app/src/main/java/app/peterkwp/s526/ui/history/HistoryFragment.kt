package app.peterkwp.s526.ui.history

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import app.peterkwp.s526.databinding.FragmentHistoryBinding
import app.peterkwp.s526.ui.main.ViewPagerFragmentDirections
import app.peterkwp.s526.ui.viewmodel.MainViewModel
import app.peterkwp.s526.util.Log
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class HistoryFragment: Fragment() {

    private val viewModel: MainViewModel by activityViewModels()

    private fun subscribeUi(adapter : HistoryAdapter) {
        viewModel.history.observe(viewLifecycleOwner, { historyList ->
            Log.d(TAG, "subscribeUi() viewModel.history [$historyList]")
            adapter.submitList(historyList)
        })
    }

    private fun launchUi() {
        Log.d(TAG, "launchUi()")
        viewModel.getHistory()
    }

    private fun navigateToDetail(view: View, query: String) {
        val direction = ViewPagerFragmentDirections.actionViewPagerFragmentToSearchFragment(query)
        view.findNavController().navigate(direction)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = FragmentHistoryBinding.inflate(inflater, container, false)
        val adapter = HistoryAdapter() {
            Log.d(TAG, "onCreateView() item click [${it}]")
            navigateToDetail(binding.root, it)
        }
        binding.historyList.apply {
            layoutManager = LinearLayoutManager(context)
            this.adapter = adapter
        }
        subscribeUi(adapter)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.d(TAG, "onViewCreated()")
        launchUi()
    }

    companion object {
        private const val TAG = "HistoryFragment"
    }
}