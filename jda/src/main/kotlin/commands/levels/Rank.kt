package commands.levels

import Colors
import Database
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.interactions.commands.OptionType
import net.dv8tion.jda.api.interactions.commands.build.Commands
import replyEmbed

object Rank {
    val commandData = Commands.slash("rang", "Afficher le niveau d'un membre du serveur").addOption(
        OptionType.USER, "mention", "Le membre dont tu veux afficher le niveau"
    )

    fun execute(event: SlashCommandInteractionEvent) {
        val member = event.member!!
        val data = Database.findUser(event.guild!!.idLong, event.user.idLong)

        if (!data.next())
            return replyEmbed(event.interaction, Colors.RED, "❌ ${event.user.asMention} n'est pas enregistré dans le classement", true)

        event.interaction.replyEmbeds(
            EmbedBuilder()
                .setColor(Colors.BLUE)
                .addField(
                    "Niveau ${data.getInt("level")}",
                    " "+ Levels.toProgressBar(data.getInt("xp"), data.getInt("level"), 13),
                    false
                )
                .setAuthor("Progression de ${member.effectiveName}", null, member.effectiveAvatarUrl)
                .build()
        ).queue()
    }
}