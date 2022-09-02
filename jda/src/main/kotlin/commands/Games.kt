package commands

import Colors
import games.Hangman
import replyEmbed
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter
import net.dv8tion.jda.api.interactions.commands.OptionType
import net.dv8tion.jda.api.interactions.commands.build.Commands
import net.dv8tion.jda.api.interactions.commands.build.OptionData
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData
import kotlin.random.Random

class Games: ListenerAdapter() {
    companion object {
        fun build(jda: JDA): Array<SlashCommandData> {
            jda.addEventListener(Games())

            return arrayOf(
                Commands.slash("coinflip", "Jouer à pile ou face").addOptions(
                    OptionData(OptionType.STRING, "face", "La face sur laquelle tu veux parier", true)
                        .addChoice("Pile", "Pile")
                        .addChoice("Face", "Face")
                ),
                Commands.slash("roll", "Lancer des dés").addOption(
                    OptionType.STRING, "texte", "Texte sous la forme 2d20+... (lancer deux dés de 20 faces + ...)", true
                ),
                Commands.slash("pendu", "Jouer au pendu")
            )
        }
    }

    override fun onSlashCommandInteraction(event: SlashCommandInteractionEvent) {
        when (event.name) {
            "coinflip" -> coinflipCommand(event)
            "roll" -> rollCommand(event)
            "pendu" -> Hangman(event)
        }
    }

    private fun coinflipCommand(event: SlashCommandInteractionEvent) {
        val face = event.getOption("face")!!.asString
        val choice = arrayOf("Pile", "Face")[Random.nextInt(2)]

        if (face == choice)
            replyEmbed(event.interaction, Colors.GOLD, "\uD83E\uDE99 $face ! Tu as gagné")
        else
            replyEmbed(event.interaction, Colors.RED, "\uD83E\uDE99 $choice ! Tu as perdu")
    }

    private fun rollCommand(event: SlashCommandInteractionEvent) {
        val items = event.getOption("texte")!!.asString.split("+")
        val results = ArrayList<Int>()

        items.forEach pass@{
            when (val value = it.toIntOrNull()) {
                null -> {
                    val split = it.split("d")
                    if (split.size != 2)
                        return@pass

                    val n = split[0].toIntOrNull() ?: 0
                    val faces = split[1].toIntOrNull() ?: 0

                    (0 until n).forEach{ _ -> results.add(Random.nextInt(0, faces))}
                }
                else -> results.add(value)
            }
        }

        if (results.isEmpty()) {
            replyEmbed(event.interaction, Colors.RED, "❌ Format invalide", true)
        } else {
            replyEmbed(event.interaction, Colors.GOLD, "\uD83C\uDFB2 Résultat : `${results.joinToString("` + `")}` = `${results.sum()}`")
        }
    }
}


