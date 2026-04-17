package app.peter.s526.presentation.view.main.search

import android.content.Context
import android.os.Bundle
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import app.peter.s526.R
import app.peter.s526.application.Log
import app.peter.s526.databinding.FragmentSearchBinding
import app.peter.s526.presentation.util.viewBinding
import app.peter.s526.presentation.view.main.MainViewModel
import com.bumptech.glide.Glide
import com.bumptech.glide.RequestManager
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SearchFragment : Fragment(R.layout.fragment_search) {

    private val viewModel: MainViewModel by activityViewModels()
    private val binding by viewBinding(FragmentSearchBinding::bind)
    private val glide: RequestManager by lazy { Glide.with(this) }
    private val args: SearchFragmentArgs by navArgs()

    private var pageCount = 1
    private var currentPage = 1
    private var loading = false
    private var complete = false

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.d(TAG, "onViewCreated() query[${args.query}]")

        resetPagingState()

        val adapter = SearchAdapter(glide) { book ->
            Log.d(TAG, "item click [${book.isbn}]")
            navigateToDetail(view, book.isbn)
        }

        binding.searchList.apply {
            layoutManager = LinearLayoutManager(context)
            this.adapter = adapter
            addOnScrollListener(createScrollListener())
        }

        binding.search.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                hideSoftKeyboard()
                query?.let { text ->
                    resetPagingState()
                    viewModel.setCurrentSearchQuery(text)
                    viewModel.addHistory(text)
                }
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean = true
        })

        subscribeUi(adapter)

        if (args.query.isNotEmpty()) {
            binding.search.setQuery(args.query, true)
        }
    }

    override fun onDestroyView() {
        Log.d(TAG, "onDestroyView()")
        resetPagingState()
        super.onDestroyView()
    }

    private fun subscribeUi(adapter: SearchAdapter) {
        viewModel.searchBookList.observe(viewLifecycleOwner) { bookList ->
            Log.d(TAG, "searchBookList updated [size=${bookList.size}] currentPage[$currentPage]")
            if (currentPage == 1) adapter.setData(bookList) else adapter.appendData(bookList)
        }
        viewModel.currentSearchQuery.observe(viewLifecycleOwner) { query ->
            Log.d(TAG, "currentSearchQuery [$query]")
            if (query.isEmpty()) {
                clearSearchResult(adapter)
            } else {
                performSearch(query, adapter)
            }
        }
    }

    private fun performSearch(query: String, adapter: SearchAdapter) {
        viewModel.searchBook(query, pageCount.toString()) { page, total ->
            Log.d(TAG, "performSearch() page[$page] total[$total]")
            currentPage = page
            if (adapter.size() >= total && pageCount != 1) {
                loading = false
                complete = true
            } else {
                loading = false
                pageCount++
            }
        }
    }

    private fun clearSearchResult(adapter: SearchAdapter) {
        adapter.clearData()
        viewModel.clearSearchResult()
    }

    private fun navigateToDetail(view: View, isbn: String) {
        val direction = SearchFragmentDirections.actionSearchFragmentToDetailFragment(isbn)
        view.findNavController().navigate(direction)
    }

    private fun hideSoftKeyboard() {
        (context?.getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager)
            ?.hideSoftInputFromWindow(binding.search.windowToken, 0)
    }

    private fun resetPagingState() {
        pageCount = 1
        currentPage = 1
        loading = false
        complete = false
    }

    private fun createScrollListener() = object : RecyclerView.OnScrollListener() {
        override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
            val layoutManager = recyclerView.layoutManager as? LinearLayoutManager ?: return
            val adapter = recyclerView.adapter ?: return
            val position = layoutManager.findLastCompletelyVisibleItemPosition()
            val remainCount = adapter.itemCount - position
            if (remainCount < AUTO_LOAD_THRESHOLD && !loading && !complete) {
                loading = true
                viewModel.currentSearchQuery.value?.let { query ->
                    performSearch(query, recyclerView.adapter as SearchAdapter)
                }
            }
        }
    }

    companion object {
        private const val AUTO_LOAD_THRESHOLD = 5
        private const val TAG = "SearchFragment"
    }
}
