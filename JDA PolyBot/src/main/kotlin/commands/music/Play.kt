package commands.music

import Colors
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.interactions.commands.OptionType
import net.dv8tion.jda.api.interactions.commands.build.Commands
import replyEmbed

object Play {
    val commandData = Commands.slash("play", "Ecouter une video youtube").addOption(
        OptionType.STRING, "recherche", "Un lien youtube ou le nom d'une vidéo", true
    )

    fun execute(event: SlashCommandInteractionEvent) {
        val channel = event.member!!.voiceState?.channel
            ?: return replyEmbed(event.interaction, Colors.RED, "❌ Vous devez être connecté à un salon", true)

        val arg = event.getOption("recherche")!!.asString
        val query = if (arg.startsWith("https://")) arg else "ytsearch: $arg"

        Manager.loadItem(event.guild!!, channel, event.interaction, query)
    }
}