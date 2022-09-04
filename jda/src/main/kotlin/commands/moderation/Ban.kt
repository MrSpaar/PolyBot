package commands.moderation

import replyEmbed
import checkPermissions
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.interactions.commands.OptionType
import net.dv8tion.jda.api.interactions.commands.build.Commands
import net.dv8tion.jda.api.interactions.commands.build.OptionData

object Ban {
    val commandData = Commands.slash("ban", "Bannir un membre définivement").addOptions(
        OptionData(OptionType.USER, "membre", "Le membre à bannir", true),
        OptionData(OptionType.STRING, "raison", "La raison du banissement")
    )

    fun execute(event: SlashCommandInteractionEvent) {
        if (checkPermissions(event, true, Permission.BAN_MEMBERS)) return
        if (checkPermissions(event, false, Permission.BAN_MEMBERS)) return

        val user = event.getOption("membre")!!.asUser
        val reason = event.getOption("raison")?.asString ?: "Pas de raison"

        event.guild!!.ban(user, 0, reason).queue()
        replyEmbed(
            event.interaction, Colors.GREEN, "\uD83D\uDCE4 ${user.asMention} a été banni\n❔ Raison : $reason"
        )
    }
}