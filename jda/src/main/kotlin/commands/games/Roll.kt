package commands.games

import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.interactions.commands.OptionType
import net.dv8tion.jda.api.interactions.commands.build.Commands
import replyEmbed
import kotlin.random.Random

object Roll {
    val commandData = Commands.slash("roll", "Lancer des dés").addOption(
        OptionType.STRING, "texte", "Texte sous la forme 2d20+... (lancer deux dés de 20 faces + ...)", true
    )

    fun execute(event: SlashCommandInteractionEvent) {
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