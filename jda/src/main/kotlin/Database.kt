import org.litote.kmongo.*
import kotlin.reflect.KMutableProperty1
import kotlin.reflect.KProperty1

data class Server(
    var _id: Long,
    var announceChannelId: Long,
    var logsChannelId: Long,
    var newcomerRoleId: Long,
    var welcomeChannelId: Long,
    var welcomeText: String
)

private val client = KMongo.createClient(Vars.DB_URI)
private val database = client.getDatabase("data")

private val cache = HashMap<Long, Server>().apply {
    database.getCollection<Server>("setup").find().forEach{ this[it._id] = it }
}

fun <T> updateConfig(guildId: Long, field: KProperty1<Server, T>, value: T) {
    (field as KMutableProperty1<Server, T>).set(cache[guildId]!!, value)
    database.getCollection<Server>("setup").updateOneById(guildId, setValue(field, value))
}
