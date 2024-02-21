@file:Suppress("ArrayInDataClass")

package api

data class TwitchObj(
    val data: Array<Stream>
)

data class Stream(
    val title: String,
    val game_name: String,
    val display_name: String,
    val broadcaster_login: String,
)

data class OAuth(
    val expires_in: Long,
    val access_token: String
)