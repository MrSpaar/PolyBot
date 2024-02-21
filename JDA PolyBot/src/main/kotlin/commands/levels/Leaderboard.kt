package commands.levels

import Colors
import Database
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.entities.emoji.Emoji
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.interactions.commands.build.Commands
import replyEmbed

object Leaderboard {
    val commandData = Commands.slash("classement", "Afficher le classement du serveur")

    fun execute(event: SlashCommandInteractionEvent) {
        val guild = event.guild!!
        val data = Database.getLeaderboard(guild.idLong)

        if (!data.next())
            return replyEmbed(event.interaction, Colors.RED, "Aucun classement sur ce serveur")

        var (names, levels, progress) = arrayOf("", "", "")

        for (i in 0..9) {
            names += guild.getMemberById(data.getLong("user_id"))?.effectiveName + "\n"
            levels +=  "${data.getInt("level")} (${Levels.format(data.getInt("xp"))})\n"
            progress += Levels.toProgressBar(data.getInt("xp"), data.getInt("level"), 5)
            data.next()
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