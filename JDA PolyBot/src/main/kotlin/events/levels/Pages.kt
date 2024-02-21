package events.levels

import Colors
import Database
import kotlin.math.ceil
import commands.levels.Levels
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter

class Pages: ListenerAdapter() {
    override fun onMessageReactionAdd(event: MessageReactionAddEvent) {
        if (event.member == null || event.user!!.isBot) return

        val emoji = event.emoji.name
        if (emoji != "◀️" && emoji != "▶️") return

        val message = event.retrieveMessage().complete()
        if (message.embeds.size != 1) return

        val embed = message.embeds[0]
        if ("Page" !in (embed.footer?.text ?: "")) return

        val entries = Database.getLeaderboard(event.guild.idLong)
        val entryCount = entries.getInt("total_rows")

        val inc = if(emoji == "◀️") -1 else 1
        val pageCount = ceil(entryCount/10.0).toInt()

        val currentPage = embed.footer!!.text!!.replace("Page ", "").toInt()
        val nextPage = minOf((currentPage+inc) % pageCount, pageCount)

        var (names, levels, progress) = arrayOf("", "", "")

        val b = nextPage*10
        val a = b-9

        for (i in 0..a)
            entries.next()

        for (i in a..b) {
            names += event.guild.getMemberById(entries.getLong("user_id"))?.effectiveName + "\n"
            levels += "${entries.getInt("level")} (${Levels.format(entries.getInt("xp"))})\n"
            progress += Levels.toProgressBar(entries.getInt("xp"), entries.getInt("level"), 5)
            entries.next()
        }

        message.editMessageEmbeds(
            EmbedBuilder()
                .setColor(Colors.BLUE)
                .setFooter("Page $nextPage")
                .addField("Nom", names, true)
                .addField("Niveau", levels, true)
                .addField("Progression", progress, true)
                .build()
        ).queue()

        event.reaction.removeReaction(event.user!!).queue()
    }
}