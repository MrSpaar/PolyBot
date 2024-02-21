@file:Suppress("ArrayInDataClass")
package api

data class WeatherObj(
    val list: Array<Cast>,
)

data class Cast (
    val main: Main,
    val wind: Wind,
    val weather: Array<Weather>,
)

data class Main(
    val temp: Double,
    val humidity: Int,
)

data class Weather(
    val icon: String,
    val id: Int,
    val main: String,
    val description: String
)

data class Wind(
    val speed: Double,
)
