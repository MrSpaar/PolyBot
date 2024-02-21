package commands.utility

import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData

class Utility: ListenerAdapter() {
    companion object {
        fun build(jda: JDA): Array<SlashCommandData> {
            jda.addEventListener(Utility())

            return arrayOf(Emoji.commandData, Poll.commandData, ProfilePic.commandData)
        }
    }

    override fun onSlashCommandInteraction(event: SlashCommandInteractionEvent) {
        when (event.name) {
            "emoji" -> Emoji.execute(event)
            "sondage" -> Poll.execute(event)
            "pfp" -> ProfilePic.execute(event)
        }
    }
}