package app.peter.s526.data.entities

import com.google.gson.annotations.SerializedName

data class Book (
    @SerializedName("isbn13") val isbn: String = "",
    val title: String = "",
    val subtitle: String = "",
    val price: String = "",
    val image: String = "",
    val url: String = ""
)