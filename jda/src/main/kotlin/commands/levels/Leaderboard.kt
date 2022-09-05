package commands.levels

import Colors
import database.Database
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.entities.emoji.Emoji
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.interactions.commands.build.Commands

object Leaderboard {
    val commandData = Commands.slash("classement", "Afficher le classement du serveur")

    fun execute(event: SlashCommandInteractionEvent) {
        val guild = event.guild!!

        var (names, levels, progress) = arrayOf("", "", "")

        Database.getLeaderboard(guild.idLong).limit(10).forEach {
            val entry = it.guilds[0]

            names += guild.getMemberById(it._id)?.effectiveName + "\n"
            levels += entry.toSummary() + "\n"
            progress += entry.toProgressBar(5) + "\n"
        }

        event.interaction.replyEmbeds(
            EmbedBuilder()
                .setColor(Colors.BLUE)
                .setFooter("Page 1")
                .addField("Nom", names, true)
                .addField("Niveau", levels, true)
                .addField("Progression", progress, true)
                .build()
        ).queue {
            it.retrieveOriginal().queue { msg ->
                msg.addReaction(Emoji.fromUnicode("◀️")).queue()
                msg.addReaction(Emoji.fromUnicode("▶️")).queue()
            }
        }
    }
}