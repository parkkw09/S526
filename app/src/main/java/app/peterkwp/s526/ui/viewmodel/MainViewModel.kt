package app.peterkwp.s526.ui.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import app.peterkwp.s526.domain.usecase.BookmarkUseCase
import app.peterkwp.s526.domain.usecase.DetailBookUseCase
import app.peterkwp.s526.domain.usecase.NewBookUseCase
import app.peterkwp.s526.domain.usecase.SearchBookUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor (
    private val newBookUseCase: NewBookUseCase,
    private val bookmarkUseCase: BookmarkUseCase,
    private val detailBookUseCase: DetailBookUseCase,
    private val searchBookUseCase: SearchBookUseCase
) : ViewModel() {

    private val _index = MutableLiveData<Int>()
    val text: LiveData<String> = Transformations.map(_index) {
        "Hello world from section: $it"
    }

    fun setIndex(index: Int) {
        _index.value = index
    }
}