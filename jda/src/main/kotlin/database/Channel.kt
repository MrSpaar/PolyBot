package database

data class Channel(
    val _id: Long,
    val guildId: Long,
    val memberId: Long,
    val txtId: Long
)