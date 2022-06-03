package app.peter.s526.presentation.view.main

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.peter.s526.application.Log
import app.peter.s526.application.Utils
import app.peter.s526.domain.model.Book
import app.peter.s526.domain.model.DetailBook
import app.peter.s526.domain.usecase.BookmarkUseCase
import app.peter.s526.domain.usecase.DetailBookUseCase
import app.peter.s526.domain.usecase.NewBookUseCase
import app.peter.s526.domain.usecase.SearchBookUseCase
import com.google.android.gms.appset.AppSet
import com.google.android.gms.appset.AppSetIdClient
import com.google.android.play.core.review.ReviewManager
import com.google.android.play.core.review.ReviewManagerFactory
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor (
    private val newBookUseCase: NewBookUseCase,
    private val bookmarkUseCase: BookmarkUseCase,
    private val detailBookUseCase: DetailBookUseCase,
    private val searchBookUseCase: SearchBookUseCase
) : ViewModel() {

    private var _bookList: MutableLiveData<List<Book>> = MutableLiveData()
    val bookList: LiveData<List<Book>>
        get() = _bookList

    private var _bookmark: MutableLiveData<List<Book>> = MutableLiveData()
    val bookmark: LiveData<List<Book>>
        get() = _bookmark

    private var _searchBookList: MutableLiveData<List<Book>> = MutableLiveData()
    val searchBookList: LiveData<List<Book>>
        get() = _searchBookList

    private var _history: MutableLiveData<List<String>> = MutableLiveData()
    val history: LiveData<List<String>>
        get() = _history

    private var _currentSearchQuery: MutableLiveData<String> = MutableLiveData()
    val currentSearchQuery: LiveData<String>
        get() = _currentSearchQuery

    var appName: String? = null
    var reviewManager: ReviewManager? = null
    var client: AppSetIdClient? = null

    fun initClient(context: Context, appName: String) {
        client = AppSet.getClient(context)
        reviewManager = ReviewManagerFactory.create(context)
        this.appName = Utils.convertAppName(context, appName)
    }

    fun getNewBook() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val list = newBookUseCase.getNewBook()
                withContext(Dispatchers.Main) {
                    _bookList.value = list
                }
            } catch(e: Exception) {
                Log.e(TAG, "getNewBook exception [${e.localizedMessage}]")
            }
        }
    }

    fun getDetailBook(isbn: String, func: (information: DetailBook) -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val book = detailBookUseCase.getDetailBook(isbn)
                withContext(Dispatchers.Main) {
                    func.invoke(book)
                }
            } catch (e: Exception) {
                Log.e(TAG, "getDetailBook exception [${e.localizedMessage}]")
            }
        }
    }

    fun addBookmark(book: DetailBook) = bookmarkUseCase.addBookmark(book)

    fun deleteBookmark(book: DetailBook) = bookmarkUseCase.deleteBookmark(book)

    fun checkBookmark(book: DetailBook): Boolean = bookmarkUseCase.checkBookmark(book)

    fun updateBookmark(bookmark: List<Book>) = bookmarkUseCase.updateBookmark(bookmark)

    fun getBookmark() {
        _bookmark.value = bookmarkUseCase.getBookmark()
    }

    fun searchBook(query: String, page: String = "1", func: (Int, Int) -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val result = searchBookUseCase.searchBook(query, page)
                val searchList = result.books
                val pageCount = result.page.toInt()
                val totalCount = result.total.toInt()

                withContext(Dispatchers.Main) {
                    func.invoke(pageCount, totalCount)
                    if (page == "1") {
                        _searchBookList.value = emptyList()
                    }
                    _searchBookList.value = searchList
                }
            } catch(e: Exception) {
                Log.e(TAG, "searchBook exception [${e.localizedMessage}]")
            }
        }
    }

    fun searchBook2(query: String, page: String = "1", func: (Int, Int) -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val words = query.split("|")
                val books = arrayListOf<Book>()
                words.forEach { word ->
                    val result = searchBookUseCase.searchBook(word, page)
                    books.addAll(result.books.toList())
                }

                withContext(Dispatchers.Main) {
                    if (page == "1") {
                        _searchBookList.value = emptyList()
                    }
                    _searchBookList.value = books.distinctBy { book ->
                        book.isbn
                    }
                }
            } catch(e: Exception) {
                Log.e(TAG, "searchBook exception [${e.localizedMessage}]")
            }
        }
    }

    fun clearSearchResult() {
        _searchBookList.value = listOf()
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