package app.peter.s526.presentation.view.main.bookmark

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.findNavController
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import app.peter.s526.R
import app.peter.s526.application.Log
import app.peter.s526.databinding.FragmentBookmarkBinding
import app.peter.s526.presentation.util.viewBinding
import app.peter.s526.presentation.view.main.MainViewModel
import app.peter.s526.presentation.view.main.ViewPagerFragmentDirections
import com.bumptech.glide.Glide
import com.bumptech.glide.RequestManager
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class BookmarkFragment : Fragment(R.layout.fragment_bookmark) {

    private val viewModel: MainViewModel by activityViewModels()
    private val binding by viewBinding(FragmentBookmarkBinding::bind)
    private val glide: RequestManager by lazy { Glide.with(this) }

    private var bookmarkAdapter: BookmarkAdapter? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.d(TAG, "onViewCreated()")

        val adapter = BookmarkAdapter(glide) { book ->
            Log.d(TAG, "item click [${book.isbn}]")
            navigateToDetail(view, book.isbn)
        }
        bookmarkAdapter = adapter

        binding.bookmarkList.apply {
            layoutManager = LinearLayoutManager(context)
            this.adapter = adapter
        }

        ItemTouchHelper(ItemMoveCallback(adapter)).apply {
            attachToRecyclerView(binding.bookmarkList)
        }

        binding.ascending.setOnClickListener { adapter.sortList() }
        binding.descending.setOnClickListener { adapter.reverseSortList() }

        viewModel.bookmark.observe(viewLifecycleOwner) { list ->
            Log.d(TAG, "bookmark updated [size=${list.size}]")
            adapter.setData(list)
        }

        viewModel.getBookmark()
    }

    override fun onDestroyView() {
        Log.d(TAG, "onDestroyView()")
        bookmarkAdapter?.let { viewModel.updateBookmark(it.getList()) }
        bookmarkAdapter = null
        super.onDestroyView()
    }

    private fun navigateToDetail(view: View, isbn: String) {
        val direction = ViewPagerFragmentDirections
            .actionViewPagerFragmentToDetailFragment(isbn)
        view.findNavController().navigate(direction)
    }

    companion object {
        private const val TAG = "BookmarkFragment"
    }
}
