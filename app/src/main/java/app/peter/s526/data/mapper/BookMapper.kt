package app.peter.s526.data.mapper

import app.peter.s526.data.entities.OLEditionResponse
import app.peter.s526.data.entities.OLSearchDoc
import app.peter.s526.data.entities.OLSearchResponse
import app.peter.s526.data.source.remote.ApiConstants
import app.peter.s526.domain.model.Book
import app.peter.s526.domain.model.BookDetail
import app.peter.s526.domain.model.BookList

/**
 * Open Library API 응답(DTO)을 도메인 모델로 직접 변환한다.
 * data 계층 내부에서 변환을 완결하여 domain 계층이 DTO를 알지 못하도록 한다.
 */
object BookMapper {

    fun toBookList(response: OLSearchResponse, page: String = "1"): BookList {
        val books = response.docs
            .filter { doc -> !doc.isbn.isNullOrEmpty() }
            .map { doc -> toBook(doc) }
        return BookList(
            total = response.numFound.toString(),
            page = page,
            books = books,
        )
    }

    fun toBook(doc: OLSearchDoc): Book {
        val isbn = doc.isbn?.firstOrNull().orEmpty()
        val coverUrl = doc.coverId?.let { id ->
            "${ApiConstants.COVER_URL}${id}-L.jpg"
        }.orEmpty()
        val bookUrl = if (doc.key.isNotEmpty()) {
            "${ApiConstants.BASE_URL}${doc.key.trimStart('/')}"
        } else {
            ""
        }
        return Book(
            isbn = isbn,
            title = doc.title,
            subtitle = doc.subtitle.orEmpty(),
            image = coverUrl,
            url = bookUrl,
        )
    }

    fun toBookDetail(response: OLEditionResponse, fallbackIsbn: String): BookDetail {
        val coverUrl = response.covers?.firstOrNull()?.let { id ->
            "${ApiConstants.COVER_URL}${id}-L.jpg"
        }.orEmpty()
        val isbn13 = response.isbn13?.firstOrNull() ?: fallbackIsbn
        val isbn10 = response.isbn10?.firstOrNull().orEmpty()
        val language = response.languages?.firstOrNull()?.key?.removePrefix("/languages/").orEmpty()
        val publisher = response.publishers?.firstOrNull().orEmpty()
        val authors = response.authors?.joinToString(", ") { author ->
            author.key.removePrefix("/authors/")
        }.orEmpty()
        val year = extractYear(response.publishDate)
        val description = extractDescription(response.description)
        val bookUrl = response.works?.firstOrNull()?.key?.let { key ->
            "${ApiConstants.BASE_URL}${key.trimStart('/')}"
        }.orEmpty()

        return BookDetail(
            title = response.title,
            subtitle = response.subtitle.orEmpty(),
            authors = authors,
            publisher = publisher,
            language = language,
            isbn10 = isbn10,
            isbn13 = isbn13,
            pages = (response.numberOfPages ?: 0).toString(),
            year = year,
            desc = description,
            image = coverUrl,
            url = bookUrl,
        )
    }

    private fun extractYear(publishDate: String?): String {
        if (publishDate == null) return ""
        val yearRegex = Regex("\\d{4}")
        return yearRegex.find(publishDate)?.value ?: publishDate
    }

    private fun extractDescription(desc: Any?): String {
        return when (desc) {
            is String -> desc
            is Map<*, *> -> desc["value"]?.toString().orEmpty()
            else -> ""
        }
    }
}
