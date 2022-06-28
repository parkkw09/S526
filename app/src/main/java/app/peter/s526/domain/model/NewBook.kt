package app.peter.s526.domain.model

data class NewBook(
    val isbn: String,
    val title: String,
    val subtitle: String,
    val price: String,
    val image: String,
    val url: String
)

data class NewDetailBook(
    val error: String,
    val title: String,
    val subtitle: String,
    val authors: String,
    val publisher: String,
    val language: String,
    val isbn10: String,
    val isbn13: String,
    val pages: String,
    val year: String,
    val rating: String,
    val desc: String,
    val price: String,
    val image: String,
    val url: String,
    val pdf: NewPdf
)

data class NewPdf(
    val freeBook: String
)

data class NewListBook(
    val error: String,
    val total: String,
    val page: String,
    val books: List<NewBook>
)