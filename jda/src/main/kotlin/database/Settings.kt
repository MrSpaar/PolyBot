package database

data class Settings(
    val _id: Long,
    var announceChannelId: Long,
    var logsChannelId: Long,
    var newcomerRoleId: Long,
    var welcomeChannelId: Long,
    var welcomeText: String
)