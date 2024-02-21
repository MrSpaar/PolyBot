package commands.levels

import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData
import java.lang.Integer.max
import java.text.NumberFormat
import java.text.NumberFormat.Style
import java.util.*

class Levels: ListenerAdapter() {
    companion object {
        fun build(jda: JDA): Array<SlashCommandData> {
            jda.addEventListener(Levels())

            return arrayOf(Rank.commandData, Leaderboard.commandData)
        }

        fun <T>format(value: T): String {
            return NumberFormat.getCompactNumberInstance(Locale.US, Style.SHORT).format(value)
        }

        fun toProgressBar(xp: Int, level: Int, length: Int): String {
            if (xp == 0)
                return  "⬛".repeat(length) + " 0/100"

            val nextCap = 5 * level*level + 50*level + 100
            val nextTotal = 5.0/6 * (level+1) * (2*(level+1)*(level+1) + 27*(level+1) + 91)

            val completed = nextCap - nextTotal + xp
            val progress = (completed/nextCap)*length

            val intProgress = max(1, progress.toInt())
            val strCompleted = " ${format(completed)}/${format(nextCap)}"

            return "\uD83D\uDFE9".repeat(intProgress) + "⬛".repeat(length-intProgress) + strCompleted + "\n"
        }
    }

    override fun onSlashCommandInteraction(event: SlashCommandInteractionEvent) {
        if (!event.isFromGuild) return

        when (event.name) {
            "rang" -> Rank.execute(event)
            "classement" -> Leaderboard.execute(event)
        }
    }
}