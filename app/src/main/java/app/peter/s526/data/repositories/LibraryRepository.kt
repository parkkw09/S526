package app.peter.s526.data.repositories

import app.peter.s526.data.entities.Book
import app.peter.s526.data.entities.DetailBook
import app.peter.s526.data.entities.ListBook

interface LibraryRepository {
    suspend fun getNewBook(): ListBook
    suspend fun getDetailBook(isbn: String): DetailBook
    suspend fun getSearchBook(query: String, page: String): ListBook

    suspend fun getCurrentBestSeller(): ListBook
    suspend fun getCurrentBestSellerFull(): ListBook
    suspend fun getCurrentBestSellerFromTarget(age: String): ListBook
    suspend fun getReview(isbn: Long): ListBook

    fun addBookmark(book: Book)
    fun deleteBookmark(book: Book)
    fun checkBookmark(book: Book) : Boolean
    fun updateBookmark(bookmark: List<Book>)
    fun getBookmark(): ArrayList<Book>
    fun addHistory(query: String)
    fun getHistory(): ArrayList<String>
}