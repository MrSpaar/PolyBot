package commands.music

import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData

class Music: ListenerAdapter() {
    companion object {
        fun build(jda: JDA): Array<SlashCommandData> {
            jda.addEventListener(Music())

            return arrayOf(Play.commandData, Skip.commandData)
        }
    }

    override fun onSlashCommandInteraction(event: SlashCommandInteractionEvent) {
        if (!event.isFromGuild) return

        when (event.name) {
            "play" -> Play.execute(event)
            "skip" -> Skip.execute(event)
        }
    }
}