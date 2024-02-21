package commands.search

import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter
import net.dv8tion.jda.api.interactions.commands.build.Commands
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData

class Search: ListenerAdapter() {
    companion object {
        fun build(jda: JDA): SlashCommandData {
            jda.addEventListener(Search())

            return Commands.slash("api", "Base API").addSubcommands(
                Twitch.commandData, Weather.commandData, Anime.commandData, Wikipedia.commandData, Minecraft.commandData
            )
        }
    }

    override fun onSlashCommandInteraction(event: SlashCommandInteractionEvent) {
        when (event.subcommandName) {
            "twitch" -> Twitch.execute(event)
            "meteo" -> Weather.execute(event)
            "anime" -> Anime.execute(event)
            "wikipedia" -> Wikipedia.execute(event)
            "mc" -> Minecraft.execute(event)
        }
    }
}