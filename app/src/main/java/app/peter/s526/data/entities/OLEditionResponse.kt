package app.peter.s526.data.entities

import com.google.gson.annotations.SerializedName

data class OLEditionResponse(
    val title: String = "",
    val subtitle: String? = null,
    val authors: List<OLAuthorRef>? = null,
    val publishers: List<String>? = null,
    @SerializedName("publish_date") val publishDate: String? = null,
    @SerializedName("number_of_pages") val numberOfPages: Int? = null,
    @SerializedName("isbn_10") val isbn10: List<String>? = null,
    @SerializedName("isbn_13") val isbn13: List<String>? = null,
    val covers: List<Int>? = null,
    val languages: List<OLLanguageRef>? = null,
    val description: Any? = null,
    val works: List<OLWorkRef>? = null
)

data class OLAuthorRef(val key: String = "")
data class OLLanguageRef(val key: String = "")
data class OLWorkRef(val key: String = "")
