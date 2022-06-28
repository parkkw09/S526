package app.peter.s526.data.entities

import com.google.gson.annotations.SerializedName

data class DetailBook(
    val error: String = "",
    val title: String = "",
    val subtitle: String = "",
    val authors: String = "",
    val publisher: String = "",
    val language: String = "",
    val isbn10: String = "",
    val isbn13: String = "",
    val pages: String = "",
    val year: String = "",
    val rating: String = "",
    val desc: String = "",
    val price: String = "",
    val image: String = "",
    val url: String = "",
    val pdf: Pdf = Pdf("")
)

data class Pdf(
    @SerializedName("Free eBook") val freeBook: String = ""
)