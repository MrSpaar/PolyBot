package commands.info

import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter
import net.dv8tion.jda.api.interactions.commands.build.Commands
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData

class Info: ListenerAdapter() {
    companion object {
        fun build(jda: JDA): SlashCommandData {
            jda.addEventListener(Info())

            return Commands.slash("info", "Base info").addSubcommands(
                Server.commandData, User.commandData, Role.commandData
            )
        }
    }

    override fun onSlashCommandInteraction(event: SlashCommandInteractionEvent) {
        if (!event.isFromGuild) return

        when (event.subcommandName) {
            "membre" -> User.execute(event)
            "serveur" -> Server.execute(event)
            "role" -> Role.execute(event)
        }
    }
}