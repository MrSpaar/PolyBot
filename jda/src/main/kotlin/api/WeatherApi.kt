package api

import com.google.gson.annotations.SerializedName

data class WeatherObj(
    val list: Array<Cast>,
)

data class Cast (
    val dt: Int,
    val main: Main,
    val wind: Wind,
    val rain: Rain,
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

data class Rain(
    @SerializedName(value = "1h") val _1h: Int,
)
