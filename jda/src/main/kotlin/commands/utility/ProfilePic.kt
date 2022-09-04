package commands.utility

import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.interactions.commands.OptionType
import net.dv8tion.jda.api.interactions.commands.build.Commands

object ProfilePic {
    val commandData = Commands.slash("pfp", "Récupérer une image de profil").addOption(
        OptionType.USER, "membre", "Le membre dont tu veux l'image de profil"
    )

    fun execute(event: SlashCommandInteractionEvent) {
        val user = event.getOption("membre")?.asUser ?: event.user

        user.retrieveProfile().queue {
            event.interaction.replyEmbeds(
                EmbedBuilder().setColor(it.accentColorRaw).setImage(user.avatarUrl ?: user.defaultAvatarUrl).build()
            ).queue()
        }
    }
}