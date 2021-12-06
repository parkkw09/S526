package app.peter.s526.ui.bookmark

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.findNavController
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import app.peter.s526.databinding.FragmentBookmarkBinding
import app.peter.s526.ui.main.ViewPagerFragmentDirections
import app.peter.s526.ui.viewmodel.MainViewModel
import app.peter.s526.util.GlideApp
import app.peter.s526.util.Log
import com.bumptech.glide.RequestManager
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class BookmarkFragment: Fragment() {

    private val viewModel: MainViewModel by activityViewModels()
    private val glide: RequestManager by lazy { GlideApp.with(this) }

    private lateinit var binding: FragmentBookmarkBinding

    private fun subscribeUi(adapter : BookmarkAdapter) {
        viewModel.bookmark.observe(viewLifecycleOwner, { bookList ->
            Log.d(TAG, "subscribeUi() viewModel.bookmark [$bookList]")
            adapter.addAllData(bookList)
        })
    }

    private fun launchUi() {
        Log.d(TAG, "launchUi()")
        viewModel.getBookmark()
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
        binding = FragmentBookmarkBinding.inflate(inflater, container, false)
        val adapter = BookmarkAdapter(glide) {
            Log.d(TAG, "onCreateView() item click [${it.isbn}]")
            navigateToDetail(binding.root, it.isbn)
        }
        binding.bookmarkList.apply {
            layoutManager = LinearLayoutManager(context)
            this.adapter = adapter
        }
        ItemTouchHelper(ItemMoveCallback(adapter)).apply {
            attachToRecyclerView(binding.bookmarkList)
        }
        binding.ascending.setOnClickListener { adapter.sortList() }
        binding.descending.setOnClickListener { adapter.reverseSortList() }
        subscribeUi(adapter)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.d(TAG, "onViewCreated()")
        launchUi()
    }

    override fun onDestroyView() {
        Log.d(TAG, "onDestroyView()")
        val adapter = binding.bookmarkList.adapter as BookmarkAdapter
        viewModel.updateBookmark(adapter.getList())
        super.onDestroyView()
    }

    companion object {
        private const val TAG = "BookmarkFragment"
    }
}