import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.entities.TextChannel
import java.sql.Connection
import java.sql.DriverManager
import java.sql.ResultSet

object Database {
    private val CONN: Connection = DriverManager.getConnection("jdbc:sqlite:database.db")

    fun ensureTables() {
        CONN.createStatement().execute("""
            CREATE TABLE IF NOT EXISTS guilds (
                id INTEGER PRIMARY KEY,
                announce_chan_id INTEGER,
                logs_chan_id INTEGER,
                newcomer_role_id INTEGER,
                welcome_chan_id INTEGER,
                welcome_text TEXT
            )
        """)

        CONN.createStatement().execute("""
            CREATE TABLE IF NOT EXISTS users (
                user_id INTEGER,
                guild_id INTEGER,
                xp INTEGER,
                level INTEGER,
                
                PRIMARY KEY (user_id, guild_id),
                FOREIGN KEY (guild_id) REFERENCES guilds(id) ON DELETE CASCADE
            )
        """)

        CONN.createStatement().execute("""
            CREATE TABLE IF NOT EXISTS pending (
                guild_id INTEGER,
                user_id INTEGER,
                voice_chan_id INTEGER,
                text_chan_id INTEGER,
                
                PRIMARY KEY (guild_id, user_id)
            )
        """)
    }

    fun getLogsChannel(guild: Guild): TextChannel? {
        val data = getSettings(guild.idLong)

        if (!data.next())
            return null

        return guild.getTextChannelById(data.getLong("logs_chan_id"))
    }

    fun ensureGuild(guildId: Long) {
        CONN.createStatement().execute("""
            INSERT OR IGNORE INTO guilds
            VALUES ($guildId, NULL, NULL, NULL, NULL, NULL)
        """)
    }

    fun getSettings(guildId: Long): ResultSet {
        return CONN.createStatement().executeQuery("""
            SELECT *
            FROM guilds
            WHERE id = $guildId
        """)
    }

    fun <T>updateSetting(guildId: Long, colName: String, value: T) {
        val data = if(value is Long) "$value" else "\"${value}\""

        CONN.createStatement().execute("""
            UPDATE guilds
            SET $colName = $data
            WHERE id = $guildId
        """)
    }

    fun deleteGuild(guildId: Long) {
        CONN.createStatement().execute("""
             DELETE FROM guilds
             WHERE id = $guildId
        """)
    }

    fun findUser(guildId: Long, userId: Long): ResultSet {
        return CONN.createStatement().executeQuery("""
            SELECT *
            FROM users
            WHERE guild_id = $guildId AND user_id = $userId
        """)
    }

    fun ensureUser(guildId: Long, userId: Long) {
        CONN.createStatement().execute("""
            INSERT OR IGNORE INTO users
            VALUES ($guildId, $userId, 0, 0)
        """)
    }

    fun updateUserXp(guildId: Long, userId: Long, xpInc: Int, levelInc: Int) {
        CONN.createStatement().execute("""
            UPDATE users
            SET xp = xp + $xpInc, level = level + $levelInc
            WHERE guild_id = $guildId AND user_id = $userId
        """)
    }

    fun deleteUser(guildId: Long, userId: Long) {
        CONN.createStatement().execute("""
            DELETE FROM users
            WHERE guild_id = $guildId AND user_id = $userId
        """)
    }

    fun getLeaderboard(serverId: Long): ResultSet {
        return CONN.createStatement().executeQuery("""
            SELECT *, COUNT(*) OVER () total_rows
            FROM users
            WHERE guild_id = $serverId
            ORDER BY xp DESC
        """)
    }

    fun findTempChannel(guildId: Long, vocId: Long): ResultSet {
        return CONN.createStatement().executeQuery("""
            SELECT *
            FROM pending
            WHERE guild_id = $guildId AND voice_chan_id = $vocId
        """)
    }

    fun insertTempChannel(guildId: Long, userId: Long, vocId: Long, txtId: Long) {
        CONN.createStatement().execute("""
            INSERT INTO pending
            VALUES ($guildId, $userId, $vocId, $txtId)
        """)
    }

    fun deleteTempChannel(guildId: Long, vocId: Long) {
        CONN.createStatement().execute("""
            DELETE FROM pending
            WHERE guild_id = $guildId AND voice_chan_id = $vocId
        """)
    }
}