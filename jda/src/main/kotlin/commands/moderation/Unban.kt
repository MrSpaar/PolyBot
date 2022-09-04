package commands.moderation

import replyEmbed
import checkPermissions
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.interactions.commands.OptionType
import net.dv8tion.jda.api.interactions.commands.build.Commands
import net.dv8tion.jda.api.interactions.commands.build.OptionData

object Unban {
    val commandData = Commands.slash("unban", "Débannir un utilisateur").addOptions(
        OptionData(OptionType.NUMBER, "id", "L'ID de l'utilisateur", true),
        OptionData(OptionType.STRING, "raison", "La raison du débanissement")
    )

    fun execute(event: SlashCommandInteractionEvent) {
        if (checkPermissions(event, true, Permission.BAN_MEMBERS)) return
        if (checkPermissions(event, false, Permission.BAN_MEMBERS)) return

        val user = event.jda.getUserById(event.getOption("id")!!.asLong)
        val reason = event.getOption("raison")?.asString ?: "Pas de raison"

        if (user == null) {
            replyEmbed(event.interaction, Colors.RED, "❌ Aucun utilisateur correspondant trouvé", true)
            return
        }

        event.guild!!.unban(user).queue()
        replyEmbed(
            event.interaction, Colors.ORANGE, "\uD83E\uDDD1\u200D⚖ ${user.asMention} a été débanni️\n❔ Raison : $reason"
        )
    }
}