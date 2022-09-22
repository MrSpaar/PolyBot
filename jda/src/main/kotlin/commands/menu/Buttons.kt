package commands.menu

import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.interactions.commands.OptionType
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData
import replyEmbed

object Buttons {
    val commandData = SubcommandData("boutons", "Créer un menu de rôles composé de boutons").addOption(
        OptionType.STRING, "titre", "Le titre du menu de rôles", true
    )

    fun execute(event: SlashCommandInteractionEvent) {
        replyEmbed(
            event.interaction, Colors.GREEN,
            "Envoie un message avec les rôles du menu\nFormat : `emoji1 @Role1 emoji2 @Role2 ...` (emojis non custom)"
        )

        event.jda.addEventListener(CreateListener(event.interaction, 0))
    }
}