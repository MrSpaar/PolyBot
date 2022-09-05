package commands.levels

import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData

class Levels: ListenerAdapter() {
    companion object {
        fun build(jda: JDA): Array<SlashCommandData> {
            jda.addEventListener(Levels())

            return arrayOf(Rank.commandData, Leaderboard.commandData)
        }
    }

    override fun onSlashCommandInteraction(event: SlashCommandInteractionEvent) {
        if (event.guild == null) return

        when (event.name) {
            "rang" -> Rank.execute(event)
            "classement" -> Leaderboard.execute(event)
        }
    }
}