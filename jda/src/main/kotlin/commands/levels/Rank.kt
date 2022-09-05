package commands.levels

import Colors
import database.Database
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
        val member = event.getOption("mention")?.asMember ?: event.member!!

        val data = Database.findMember(event.guild!!.idLong, member.idLong)
            ?: return replyEmbed(event.interaction, Colors.RED, "❌ ${member.asMention} n'est pas enregistré dans le classement", true)

        event.interaction.replyEmbeds(
            EmbedBuilder()
                .setColor(Colors.BLUE)
                .addField("Niveau ${data.level}", " "+data.toProgressBar(13), false)
                .setAuthor("Progression de ${member.effectiveName}", null, member.effectiveAvatarUrl)
                .build()
        ).queue()
    }
}