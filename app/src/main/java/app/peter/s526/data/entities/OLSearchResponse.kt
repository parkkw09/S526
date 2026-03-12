package app.peter.s526.data.entities

import com.google.gson.annotations.SerializedName

data class OLSearchResponse(
    @SerializedName("numFound") val numFound: Int = 0,
    val start: Int = 0,
    val docs: List<OLSearchDoc> = listOf()
)

data class OLSearchDoc(
    val key: String = "",
    val title: String = "",
    val subtitle: String? = null,
    val isbn: List<String>? = null,
    @SerializedName("cover_i") val coverId: Int? = null,
    @SerializedName("author_name") val authorName: List<String>? = null,
    @SerializedName("first_publish_year") val firstPublishYear: Int? = null,
    @SerializedName("number_of_pages_median") val numberOfPagesMedian: Int? = null,
    val publisher: List<String>? = null,
    val language: List<String>? = null
)
