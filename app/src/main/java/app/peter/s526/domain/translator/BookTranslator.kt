package app.peter.s526.domain.translator

import app.peter.s526.data.entities.Book
import app.peter.s526.data.entities.DetailBook
import app.peter.s526.data.entities.ListBook
import app.peter.s526.domain.model.NewBook
import app.peter.s526.domain.model.NewDetailBook
import app.peter.s526.domain.model.NewListBook
import app.peter.s526.domain.model.NewPdf

object BookTranslator {

    fun getBookByDetailBook(detailBook: NewDetailBook): Book
        = Book(
            detailBook.isbn13,
            detailBook.title,
            detailBook.subtitle,
            detailBook.price,
            detailBook.image,
            detailBook.url)

    fun getBook(book: NewBook): Book
        = Book(book.isbn,
            book.title,
            book.subtitle,
            book.price,
            book.image,
            book.url)

    fun getBookList(list: List<NewBook>) = list.map {
        getBook(it)
    }

    fun getNewBook(book: Book): NewBook
        = NewBook(book.isbn,
                book.title,
                book.subtitle,
                book.price,
                book.image,
                book.url)

    fun getNewBookList(list: List<Book>) = list.map {
        getNewBook(it)
    }

    fun getNewDetailBook(book : DetailBook): NewDetailBook {
        return NewDetailBook(
            book.error,
            book.title,
            book.subtitle,
            book.authors,
            book.publisher,
            book.language,
            book.isbn10,
            book.isbn13,
            book.pages,
            book.year,
            book.rating,
            book.desc,
            book.price,
            book.image,
            book.url,
            NewPdf(book.pdf.freeBook),
        )
    }

    fun getListBook(list: ListBook): NewListBook {
        return NewListBook(
            list.error,
            list.total,
            list.page,
            list.books.map {
                getNewBook(it)
            }
        )
    }
}