package app.peter.s526.data.source.remote

import app.peter.s526.data.entities.Book
import app.peter.s526.data.entities.DetailBook
import app.peter.s526.data.entities.ListBook
import app.peter.s526.data.entities.OLEditionResponse
import app.peter.s526.data.entities.OLSearchDoc
import app.peter.s526.data.entities.OLSearchResponse
import app.peter.s526.data.entities.Pdf

/**
 * Open Library API 응답을 기존 내부 모델(ListBook, DetailBook, Book)로 변환하는 매퍼.
 * Repository 이상 계층의 변경을 최소화하기 위해 data 계층에서 변환을 수행합니다.
 */
object OLResponseMapper {

    fun toListBook(response: OLSearchResponse, page: String = "1"): ListBook {
        val books = response.docs
            .filter { doc -> !doc.isbn.isNullOrEmpty() }
            .map { doc -> toBook(doc) }
        return ListBook(
            error = "0",
            total = response.numFound.toString(),
            page = page,
            books = books
        )
    }

    fun toBook(doc: OLSearchDoc): Book {
        val isbn = doc.isbn?.firstOrNull() ?: ""
        val coverUrl = doc.coverId?.let {
            "${RemoteConst.COVER_URL}${it}-L.jpg"
        } ?: ""
        val bookUrl = if (doc.key.isNotEmpty()) {
            "${RemoteConst.URL}${doc.key.trimStart('/')}"
        } else ""

        return Book(
            isbn = isbn,
            title = doc.title,
            subtitle = doc.subtitle ?: "",
            price = "",
            image = coverUrl,
            url = bookUrl
        )
    }

    fun toDetailBook(response: OLEditionResponse, isbn: String): DetailBook {
        val coverUrl = response.covers?.firstOrNull()?.let {
            "${RemoteConst.COVER_URL}${it}-L.jpg"
        } ?: ""
        val isbn13 = response.isbn13?.firstOrNull() ?: isbn
        val isbn10 = response.isbn10?.firstOrNull() ?: ""
        val language = response.languages?.firstOrNull()?.key?.removePrefix("/languages/") ?: ""
        val publisher = response.publishers?.firstOrNull() ?: ""
        val authors = response.authors?.joinToString(", ") { it.key.removePrefix("/authors/") } ?: ""
        val year = extractYear(response.publishDate)
        val description = extractDescription(response.description)
        val bookUrl = response.works?.firstOrNull()?.key?.let {
            "${RemoteConst.URL}${it.trimStart('/')}"
        } ?: ""

        return DetailBook(
            error = "0",
            title = response.title,
            subtitle = response.subtitle ?: "",
            authors = authors,
            publisher = publisher,
            language = language,
            isbn10 = isbn10,
            isbn13 = isbn13,
            pages = (response.numberOfPages ?: 0).toString(),
            year = year,
            rating = "0",
            desc = description,
            price = "",
            image = coverUrl,
            url = bookUrl,
            pdf = Pdf("")
        )
    }

    private fun extractYear(publishDate: String?): String {
        if (publishDate == null) return ""
        // "March 1, 2020" or "2020" 형태에서 4자리 연도 추출
        val yearRegex = Regex("\\d{4}")
        return yearRegex.find(publishDate)?.value ?: publishDate
    }

    private fun extractDescription(desc: Any?): String {
        return when (desc) {
            is String -> desc
            is Map<*, *> -> desc["value"]?.toString() ?: ""
            else -> ""
        }
    }
}
