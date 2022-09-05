package events.levels

import Colors
import kotlin.math.min
import database.Database
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter

class Pages: ListenerAdapter() {
    override fun onMessageReactionAdd(event: MessageReactionAddEvent) {
        if (event.member == null || event.user!!.isBot) return

        val emoji = event.emoji.asUnicode().name
        if (emoji != "◀️" && emoji != "▶️") return

        val message = event.retrieveMessage().complete()
        if (message.embeds.size != 1) return

        val embed = message.embeds[0]
        if ("Page" !in (embed.footer?.text ?: "")) return

        val entries = Database.getLeaderboard(event.guild.idLong).toList()

        val inc = if(emoji == "◀️") -1 else 1
        val pageNumber = entries.size/10 + min(1, entries.size%10)

        val currentPage = embed.footer!!.text!!.replace("Page ", "").toInt()
        val nextPage = ((currentPage + inc) % pageNumber).let{ if(it == 0) pageNumber else it}

        var (names, levels, progress) = arrayOf("", "", "")

        entries.subList(nextPage*10-9, nextPage*10).forEach {
            val entry = it.guilds[0]

            names += event.member!!.effectiveName + "\n"
            levels += entry.toSummary() + "\n"
            progress += entry.toProgressBar(5) + "\n"
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