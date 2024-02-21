package commands.utility

import Colors
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.interactions.commands.OptionType
import net.dv8tion.jda.api.interactions.commands.build.Commands
import replyEmbed

object Emoji {
    val commandData = Commands.slash("emoji", "Récupérer l'image d'un émoji").addOption(
        OptionType.STRING, "emoji", "L'emoji dont tu veux la source", true
    )

    fun execute(event: SlashCommandInteractionEvent) {
        val arg = event.getOption("emoji")!!.asString
        val id = arg.replace("\\D+".toRegex(), "")

        if (id == "")
            return replyEmbed(event.interaction, Colors.RED, "❌ Emoji custom invalide", true)

        val emoji = event.jda.getEmojiById(id)
            ?: return replyEmbed(event.interaction, Colors.RED, "❌ Emoji introuvable", true)

        event.interaction.replyEmbeds(
            EmbedBuilder().setColor(Colors.BLUE).setImage(emoji.imageUrl).build()
        ).queue()
    }
}