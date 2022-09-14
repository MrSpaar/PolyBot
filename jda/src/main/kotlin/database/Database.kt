package database

import Vars
import com.mongodb.client.FindIterable
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.entities.TextChannel
import org.litote.kmongo.*
import kotlin.reflect.KMutableProperty1
import kotlin.reflect.KProperty1

object Database {
    private val client = KMongo.createClient(Vars.DB_URI)
    private val database = client.getDatabase("data")

    val cache = HashMap<Long, Settings>().apply {
        database.getCollection<Settings>("setup").find().forEach{
            this[it._id] = it
        }
    }

    fun <T> updateConfig(guildId: Long, field: KProperty1<Settings, T>, value: T) {
        (field as KMutableProperty1<Settings, T>).set(cache[guildId]!!, value)
        database.getCollection<Settings>("setup").updateOneById(guildId, setValue(field, value))
    }

    fun getAnnounceChannel(guild: Guild): TextChannel? {
        return guild.getTextChannelById(cache[guild.idLong]!!.announceChannelId)
    }

    fun getLogsChannel(guild: Guild): TextChannel? {
        return guild.getTextChannelById(cache[guild.idLong]!!.logsChannelId)
    }

    fun findMember(guildId: Long, memberId: Long): Server? {
        return database.getCollection<Member>("members")
            .findOneById(memberId)
            ?.guilds?.first { it.id == guildId }
    }

    fun insertMember(guildId: Long, memberId: Long): Server {
        val member = Member(memberId, arrayListOf(Server(guildId, 0, 0)))

        database.getCollection<Member>("members").insertOne(member)
        return member.guilds[0]
    }

    fun updateMember(guildId: Long, memberId: Long): Server {
        val guild = Server(guildId, 0, 0)

        database.getCollection<Member>("members").updateOneById(
            memberId, addToSet(Member::guilds, guild), upsert()
        )

        return guild
    }

    fun findTempChannel(guildId: Long, memberId: Long): Channel? {
        return database.getCollection<Channel>("pending").find(
            and(Channel::guildId eq guildId, Channel::memberId eq memberId)
        ).first()
    }

    fun insertTempChannel(memberId: Long, guildId: Long, txtId: Long, vocId: Long) {
        database.getCollection<Channel>("pending").insertOne(
            Channel(memberId, guildId, txtId, vocId)
        )
    }

    fun deleteTempChannel(guildId: Long, memberId: Long) {
        database.getCollection<Channel>("pending").deleteOne(
            and(Channel::guildId eq guildId, Channel::memberId eq memberId)
        )
    }

    fun deleteMember(guildId: Long, memberId: Long) {
        database.getCollection<Member>("members").updateOneById(
            memberId,
            pullByFilter(Member::guilds, Server::id eq guildId)
        )
    }

    fun updateMember(guildId: Long, memberId: Long, amount: Int, levelUp: Int) {
        database.getCollection<Member>("members").updateOne(
            and(
                Member::_id eq memberId,
                (Member::guilds / Server::id) eq guildId
            ),
            combine(
                inc(Member::guilds.posOp / Server::xp, amount),
                inc(Member::guilds.posOp / Server::level, levelUp)
            )
        )
    }

    fun getLeaderboard(guildId: Long): FindIterable<Member> {
        return database.getCollection<Member>("members")
            .find((Member::guilds / Server::id) eq guildId)
            .projection(Member::guilds.posOp)
            .sort(descending(Member::guilds / Server::xp))
    }
}