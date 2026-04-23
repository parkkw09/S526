package app.peter.s526.presentation.view.main

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.peter.s526.application.Log
import app.peter.s526.application.Utils
import app.peter.s526.domain.model.Book
import app.peter.s526.domain.model.BookDetail
import app.peter.s526.domain.usecase.BookmarkUseCase
import app.peter.s526.domain.usecase.DetailBookUseCase
import app.peter.s526.domain.usecase.NewBookUseCase
import app.peter.s526.domain.usecase.SearchBookUseCase
import com.google.android.play.core.review.ReviewManager
import com.google.android.play.core.review.ReviewManagerFactory
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val newBookUseCase: NewBookUseCase,
    private val bookmarkUseCase: BookmarkUseCase,
    private val detailBookUseCase: DetailBookUseCase,
    private val searchBookUseCase: SearchBookUseCase,
) : ViewModel() {

    private val _bookList = MutableStateFlow<List<Book>>(emptyList())
    val bookList: StateFlow<List<Book>> = _bookList.asStateFlow()

    private var currentNewBookPage = 1
    private var isNewBookLoading = false
    private var hasMoreNewBooks = true

    private val _bookmark = MutableStateFlow<List<Book>>(emptyList())
    val bookmark: StateFlow<List<Book>> = _bookmark.asStateFlow()

    private val _searchBookList = MutableStateFlow<List<Book>>(emptyList())
    val searchBookList: StateFlow<List<Book>> = _searchBookList.asStateFlow()

    private val _history = MutableStateFlow<List<String>>(emptyList())
    val history: StateFlow<List<String>> = _history.asStateFlow()

    private val _currentSearchQuery = MutableStateFlow("")
    val currentSearchQuery: StateFlow<String> = _currentSearchQuery.asStateFlow()

    var appName: String? = null
        private set
    var reviewManager: ReviewManager? = null
        private set

    fun initClient(context: Context, appName: String) {
        reviewManager = ReviewManagerFactory.create(context)
        this.appName = Utils.convertAppName(context, appName)
    }

    fun getNewBook() {
        currentNewBookPage = 1
        hasMoreNewBooks = true
        _bookList.value = emptyList()
        loadNewBooks(currentNewBookPage)
    }

    fun getNextNewBook() {
        if (isNewBookLoading || !hasMoreNewBooks) return
        loadNewBooks(currentNewBookPage)
    }

    private fun loadNewBooks(page: Int) {
        isNewBookLoading = true
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val result = newBookUseCase.getNewBook(page.toString())
                val newBooks = result.books
                val total = result.total.toIntOrNull() ?: 0

                withContext(Dispatchers.Main) {
                    val current = _bookList.value
                    _bookList.value = current + newBooks
                    currentNewBookPage++
                    hasMoreNewBooks = current.size + newBooks.size < total && newBooks.isNotEmpty()
                    isNewBookLoading = false
                }
            } catch (e: Exception) {
                Log.e(TAG, "getNewBook exception [${e.localizedMessage}]")
                withContext(Dispatchers.Main) {
                    isNewBookLoading = false
                }
            }
        }
    }

    fun getDetailBook(isbn: String, onResult: (BookDetail) -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val detail = detailBookUseCase.getDetailBook(isbn)
                withContext(Dispatchers.Main) { onResult(detail) }
            } catch (e: Exception) {
                Log.e(TAG, "getDetailBook exception [${e.localizedMessage}]")
            }
        }
    }

    fun addBookmark(book: BookDetail) = bookmarkUseCase.addBookmark(book)
    fun deleteBookmark(book: BookDetail) = bookmarkUseCase.deleteBookmark(book)
    fun isBookmarked(book: BookDetail): Boolean = bookmarkUseCase.isBookmarked(book)
    fun updateBookmark(bookmark: List<Book>) = bookmarkUseCase.updateBookmark(bookmark)

    fun getBookmark() {
        _bookmark.value = bookmarkUseCase.getBookmark()
    }

    fun searchBook(query: String, page: String = "1", onResult: (Int, Int) -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val result = searchBookUseCase.searchBook(query, page)
                val pageCount = result.page.toInt()
                val totalCount = result.total.toInt()

                withContext(Dispatchers.Main) {
                    onResult(pageCount, totalCount)
                    if (page == "1") _searchBookList.value = emptyList()
                    _searchBookList.value = result.books
                }
            } catch (e: Exception) {
                Log.e(TAG, "searchBook exception [${e.localizedMessage}]")
            }
        }
    }

    fun searchBookMulti(query: String, page: String = "1") {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val words = query.split("|").filter { it.isNotBlank() }
                val results = words
                    .map { word -> async { searchBookUseCase.searchBook(word, page) } }
                    .awaitAll()

                val merged = results
                    .flatMap { it.books }
                    .distinctBy { it.isbn }

                withContext(Dispatchers.Main) {
                    if (page == "1") _searchBookList.value = emptyList()
                    _searchBookList.value = merged
                }
            } catch (e: Exception) {
                Log.e(TAG, "searchBookMulti exception [${e.localizedMessage}]")
            }
        }
    }

    fun clearSearchResult() {
        _searchBookList.value = emptyList()
    }

    fun setCurrentSearchQuery(query: String) {
        _currentSearchQuery.value = query
    }

    fun addHistory(query: String) = searchBookUseCase.addHistory(query)

    fun getHistory() {
        _history.value = searchBookUseCase.getHistory()
    }

    companion object {
        private const val TAG = "MainViewModel"
    }
}
