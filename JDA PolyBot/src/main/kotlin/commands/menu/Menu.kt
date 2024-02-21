package commands.menu

import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter
import net.dv8tion.jda.api.interactions.commands.build.Commands
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData

class Menu : ListenerAdapter() {
    companion object {
        fun build(jda: JDA): SlashCommandData {
            jda.addEventListener(Menu())

            return Commands.slash("menu", "Base menu").addSubcommands(
                Buttons.commandData, Select.commandData
            )
        }
    }

    override fun onSlashCommandInteraction(event: SlashCommandInteractionEvent) {
        if (!event.isFromGuild) return

        when (event.subcommandName) {
            "liste" -> Select.execute(event)
            "boutons" -> Buttons.execute(event)
        }
    }
}