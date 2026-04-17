package app.peter.s526.domain.model

data class Book(
    val isbn: String,
    val title: String,
    val subtitle: String,
    val image: String,
    val url: String,
)

data class BookDetail(
    val title: String,
    val subtitle: String,
    val authors: String,
    val publisher: String,
    val language: String,
    val isbn10: String,
    val isbn13: String,
    val pages: String,
    val year: String,
    val desc: String,
    val image: String,
    val url: String,
)

data class BookList(
    val total: String,
    val page: String,
    val books: List<Book>,
)
