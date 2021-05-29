package app.peterkwp.s526.ui.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.peterkwp.s526.domain.model.Book
import app.peterkwp.s526.domain.model.DetailBook
import app.peterkwp.s526.domain.usecase.BookmarkUseCase
import app.peterkwp.s526.domain.usecase.DetailBookUseCase
import app.peterkwp.s526.domain.usecase.NewBookUseCase
import app.peterkwp.s526.domain.usecase.SearchBookUseCase
import app.peterkwp.s526.util.Log
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

    fun getNewBook() {
        try {
            viewModelScope.launch(Dispatchers.IO) {
                val list = newBookUseCase.getNewBook()
                withContext(Dispatchers.Main) {
                    _bookList.value = list
                }
            }
        } catch(e: Exception) {
            Log.e(TAG, "getNewBook exception [${e.localizedMessage}]")
        }
    }

    fun getDetailBook(isbn: String, func: (information: DetailBook) -> Unit) {
        try {
            viewModelScope.launch(Dispatchers.IO) {
                val book = detailBookUseCase.getDetailBook(isbn)
                withContext(Dispatchers.Main) {
                    func.invoke(book)
                }
            }
        } catch(e: Exception) {
            Log.e(TAG, "getDetailBook exception [${e.localizedMessage}]")
        }
    }

    fun addBookmark(isbn: String, book: DetailBook) = bookmarkUseCase.addBookmark(isbn, book)

    fun deleteBookmark(isbn: String) = bookmarkUseCase.deleteBookmark(isbn)

    fun checkBookmark(isbn: String): Boolean = bookmarkUseCase.checkBookmark(isbn)

    fun getBookmark() {
        _bookmark.value = bookmarkUseCase.getBookmark()
    }

    companion object {
        private const val TAG = "MainViewModel"
    }
}