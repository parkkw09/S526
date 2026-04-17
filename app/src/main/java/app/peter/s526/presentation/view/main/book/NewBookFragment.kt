package app.peter.s526.presentation.view.main.book

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import app.peter.s526.R
import app.peter.s526.application.Log
import app.peter.s526.databinding.FragmentNewBookBinding
import app.peter.s526.presentation.util.viewBinding
import app.peter.s526.presentation.view.main.MainViewModel
import app.peter.s526.presentation.view.main.ViewPagerFragmentDirections
import com.bumptech.glide.Glide
import com.bumptech.glide.RequestManager
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class NewBookFragment : Fragment(R.layout.fragment_new_book) {

    private val viewModel: MainViewModel by activityViewModels()
    private val binding by viewBinding(FragmentNewBookBinding::bind)
    private val glide: RequestManager by lazy { Glide.with(this) }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.d(TAG, "onViewCreated()")

        val layoutManager = LinearLayoutManager(context)
        val adapter = NewBookAdapter(glide) { book ->
            Log.d(TAG, "item click [${book.isbn}]")
            navigateToDetail(view, book.isbn)
        }

        binding.bookList.apply {
            this.layoutManager = layoutManager
            this.adapter = adapter
            addOnScrollListener(object : RecyclerView.OnScrollListener() {
                override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                    if (dy <= 0) return
                    val totalItemCount = layoutManager.itemCount
                    val lastVisibleItem = layoutManager.findLastVisibleItemPosition()
                    if (lastVisibleItem >= totalItemCount - AUTO_LOAD_THRESHOLD) {
                        viewModel.getNextNewBook()
                    }
                }
            })
        }

        viewModel.bookList.observe(viewLifecycleOwner) { bookList ->
            Log.d(TAG, "bookList updated [size=${bookList.size}]")
            adapter.submitList(bookList.toList())
        }

        if (viewModel.bookList.value.isNullOrEmpty()) {
            viewModel.getNewBook()
        }
    }

    private fun navigateToDetail(view: View, isbn: String) {
        val direction = ViewPagerFragmentDirections
            .actionViewPagerFragmentToDetailFragment(isbn)
        view.findNavController().navigate(direction)
    }

    companion object {
        private const val TAG = "NewBookFragment"
        private const val AUTO_LOAD_THRESHOLD = 5
    }
}
