package commands.moderation

import replyEmbed
import checkPermissions
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.interactions.commands.OptionType
import net.dv8tion.jda.api.interactions.commands.build.Commands
import net.dv8tion.jda.api.interactions.commands.build.OptionData

object Kick {
    val commandData = Commands.slash("kick", "Expulser un membre").addOptions(
        OptionData(OptionType.USER, "membre", "Le membre à exclure", true),
        OptionData(OptionType.STRING, "raison", "La raison de l'exclusion")
    )

    fun execute(event: SlashCommandInteractionEvent) {
        if (checkPermissions(event, true, Permission.KICK_MEMBERS)) return
        if (checkPermissions(event, false, Permission.KICK_MEMBERS)) return

        val user = event.getOption("membre")!!.asUser
        val reason = event.getOption("raison")?.asString ?: "Pas de raison"

        event.guild!!.kick(user, reason).queue()
        replyEmbed(
            event.interaction, Colors.GREEN, "\uD83D\uDD28 ${user.asMention} a été exclu\n❔ Raison : $reason"
        )
    }
}