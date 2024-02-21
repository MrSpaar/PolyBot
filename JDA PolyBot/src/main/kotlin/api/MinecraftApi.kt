package api

data class StatusObj(
    val status: Status
)

data class Status(
    val online: Boolean,
    val cpu: Double,
    val ram: Int,
    val version: String,
    val players: Players
)

data class Players(
    val online: Int,
    val max: Int
)