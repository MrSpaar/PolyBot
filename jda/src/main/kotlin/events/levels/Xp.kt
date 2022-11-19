package events.levels

import Colors
import Database
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.entities.Member
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter
import java.time.Instant
import kotlin.random.Random

class Xp: ListenerAdapter() {
    override fun onMessageReceived(event: MessageReceivedEvent) {
        if (event.member == null || event.author.isBot) return
        val member = event.member!!

        if (member.isCooling(event.guild.idLong)) return

        val entry = Database.findUser(event.guild.idLong, member.idLong)

        if (!entry.next())
            return

        val nextLevel = entry.getInt("level") + 1
        val amount = Random.nextInt(15, 26)
        val nextCap = 5.0/6 * nextLevel * (2 * nextLevel*nextLevel + 27*nextLevel + 91)

        val levelUp = if(entry.getInt("xp") + amount > nextCap) 1 else 0
        Database.updateUserXp(event.guild.idLong, member.idLong, amount, levelUp)

        if (levelUp == 0)
            return

        val data = Database.getSettings(event.guild.idLong)

        if (!data.next())
            return

        val channel = event.guild.getTextChannelById(data.getLong("announce_chan_id")) ?: return

        channel.sendMessageEmbeds(
            EmbedBuilder()
                .setColor(Colors.GOLD)
                .setDescription("\uD83C\uDD99 ${member.asMention} vient de monter niveau **$nextLevel**")
                .build()
        ).queue()
    }

    companion object {
        private val cooldowns = hashMapOf<Long, HashMap<Long, Instant>>()

        fun Member.isCooling(guildId: Long): Boolean {
            val guildMap = cooldowns.getOrPut(guildId) { HashMap() }

            val now = Instant.now()
            val cooldown = guildMap[this.idLong]

            if (cooldown == null) {
                guildMap[this.idLong] = now.plusSeconds(60)
                return false
            }

            if (cooldown.isBefore(now)) {
                guildMap.remove(guildId)
                return false
            }

            return true
        }
    }
}