package app.peter.s526.domain.model

data class ListBook(
    val error: String = "",
    val total: String = "",
    val page: String = "1",
    val books: List<Book> = listOf()
)