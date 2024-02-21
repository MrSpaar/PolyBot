@file:Suppress("ArrayInDataClass")
package api

data class AnimeObj(
    val data: Array<Data>,
)

data class Data(
    val attributes: Attributes,
)

data class Attributes(
    val synopsis: String,
    val canonicalTitle: String,
    val startDate: String,
    val endDate: String,
    val posterImage: Image,
    val averageRating: Double,
    val episodeCount: Int,
    val totalLength: Int,
)

data class Image(
    val tiny: String,
)