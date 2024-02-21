package commands.moderation

import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData

class Moderation: ListenerAdapter() {
    companion object {
        fun build(jda: JDA): Array<SlashCommandData> {
            jda.addEventListener(Moderation())

            return arrayOf(Ban.commandData, Clear.commandData, Kick.commandData, Unban.commandData)
        }
    }

    override fun onSlashCommandInteraction(event: SlashCommandInteractionEvent) {
        if (!event.isFromGuild) return

        when (event.name) {
            "clear" -> Clear.execute(event)
            "kick" -> Kick.execute(event)
            "ban" -> Ban.execute(event)
            "unban" -> Unban.execute(event)
        }
    }
}