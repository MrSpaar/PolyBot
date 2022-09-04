package commands.utility

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
        val emoji = event.jda.getEmojiById(arg.replace("\\D+".toRegex(), ""))

        if (emoji == null) {
            replyEmbed(event.interaction, Colors.RED, "❌ Emoji introuvable", true)
            return
        }

        event.interaction.replyEmbeds(
            EmbedBuilder().setColor(Colors.BLUE).setImage(emoji.imageUrl).build()
        ).queue()
    }
}