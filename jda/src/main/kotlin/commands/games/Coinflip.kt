package commands.games

import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.interactions.commands.OptionType
import net.dv8tion.jda.api.interactions.commands.build.Commands
import net.dv8tion.jda.api.interactions.commands.build.OptionData
import replyEmbed
import kotlin.random.Random

object Coinflip {
    val commandData = Commands.slash("coinflip", "Jouer à pile ou face").addOptions(
        OptionData(OptionType.STRING, "face", "La face sur laquelle tu veux parier", true)
            .addChoice("Pile", "Pile")
            .addChoice("Face", "Face")
    )

    fun execute(event: SlashCommandInteractionEvent) {
        val face = event.getOption("face")!!.asString
        val choice = arrayOf("Pile", "Face")[Random.nextInt(2)]

        if (face == choice)
            replyEmbed(event.interaction, Colors.GOLD, "\uD83E\uDE99 $face ! Tu as gagné")
        else
            replyEmbed(event.interaction, Colors.RED, "\uD83E\uDE99 $choice ! Tu as perdu")
    }
}