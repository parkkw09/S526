package app.peter.s526.data.source.local

import app.peter.s526.domain.model.Book
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 즐겨찾기와 검색 기록을 보관하는 인메모리 데이터 소스.
 * 프로세스가 유지되는 동안만 데이터가 보존되며, 앱 재시작 시 소실된다.
 * 영속 저장이 필요해지면 Room/DataStore 기반 구현으로 교체한다.
 */
@Singleton
class LocalBookDataSource @Inject constructor() {

    private val _bookmark: MutableList<Book> = mutableListOf()
    val bookmark: List<Book> get() = _bookmark.toList()

    private val _history: MutableList<String> = mutableListOf()
    val history: List<String> get() = _history.toList()

    fun addBookmark(book: Book) {
        if (_bookmark.contains(book)) return
        _bookmark.add(book)
    }

    fun removeBookmark(book: Book) {
        _bookmark.remove(book)
    }

    fun containsBookmark(book: Book): Boolean = _bookmark.contains(book)

    fun replaceBookmark(books: List<Book>) {
        _bookmark.clear()
        _bookmark.addAll(books)
    }

    fun addHistory(query: String) {
        if (_history.contains(query)) return
        _history.add(query)
    }
}
