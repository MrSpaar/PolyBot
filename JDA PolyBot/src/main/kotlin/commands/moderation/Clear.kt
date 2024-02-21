package commands.moderation

import replyEmbed
import checkPermissions
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.interactions.commands.OptionType
import net.dv8tion.jda.api.interactions.commands.build.Commands

object Clear {
    val commandData = Commands.slash("clear", "Supprimer plusieurs messages en une fois").addOption(
        OptionType.INTEGER, "n", "Le nombre de messages à supprimer", true
    )

    fun execute(event: SlashCommandInteractionEvent) {
        if (checkPermissions(event, true, Permission.MESSAGE_HISTORY, Permission.MESSAGE_MANAGE)) return
        if (checkPermissions(event, false, Permission.MESSAGE_MANAGE)) return

        val channel = event.guildChannel
        val n = event.getOption("n")!!.asInt

        channel.history.retrievePast(n).queue {
            channel.deleteMessages(it).queue()
            replyEmbed(
                event.interaction, Colors.GREEN, "✅ ${it.size} messages ont été supprimés", true
            )
        }
    }
}