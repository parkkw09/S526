package app.peter.s526.presentation.view.main.book

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import app.peter.s526.databinding.FragmentNewBookBinding
import app.peter.s526.presentation.view.main.ViewPagerFragmentDirections
import app.peter.s526.presentation.view.main.MainViewModel
import app.peter.s526.application.Log
import app.peter.s526.presentation.util.GlideApp
import com.bumptech.glide.RequestManager
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class NewBookFragment: Fragment() {

    private val viewModel: MainViewModel by activityViewModels()
    private val glide: RequestManager by lazy { GlideApp.with(this) }

    private fun subscribeUi(adapter : NewBookAdapter) {
        viewModel.bookList.observe(viewLifecycleOwner) { bookList ->
            Log.d(TAG, "subscribeUi() viewModel.bookList [${bookList.size}]")
            adapter.submitList(bookList.toList())
        }
    }

    private fun launchUi() {
        Log.d(TAG, "launchUi()")
        viewModel.getNewBook()
    }

    private fun navigateToDetail(view: View, isbn: String) {
        val direction = ViewPagerFragmentDirections.actionViewPagerFragmentToDetailFragment(isbn)
        view.findNavController().navigate(direction)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = FragmentNewBookBinding.inflate(inflater, container, false)
        val layoutManager = LinearLayoutManager(context)
        val adapter = NewBookAdapter(glide) {
            Log.d(TAG, "onCreateView() item click [${it.isbn}]")
            navigateToDetail(binding.root, it.isbn)
        }
        binding.bookList.apply {
            this.layoutManager = layoutManager
            this.adapter = adapter
            addOnScrollListener(object : RecyclerView.OnScrollListener() {
                override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                    super.onScrolled(recyclerView, dx, dy)
                    if (dy > 0) {
                        val totalItemCount = layoutManager.itemCount
                        val lastVisibleItem = layoutManager.findLastVisibleItemPosition()
                        if (lastVisibleItem >= totalItemCount - 5) {
                            viewModel.getNextNewBook()
                        }
                    }
                }
            })
        }
        subscribeUi(adapter)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.d(TAG, "onViewCreated()")
    }

    override fun onResume() {
        super.onResume()
        Log.d(TAG, "onResume()")
        launchUi()
    }

    companion object {
        private const val TAG = "NewBookFragment"
    }
}