package commands

import Colors
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter
import net.dv8tion.jda.api.interactions.commands.OptionType
import net.dv8tion.jda.api.interactions.commands.build.Commands
import net.dv8tion.jda.api.interactions.commands.build.OptionData
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData
import replyEmbed

class Moderation: ListenerAdapter() {
    companion object {
        fun build(jda: JDA): Array<SlashCommandData> {
            jda.addEventListener(Moderation())

            return arrayOf(
                Commands.slash("clear", "Supprimer plusieurs messages en une fois").addOption(
                    OptionType.INTEGER, "n", "Le nombre de messages à supprimer", true
                ),
                Commands.slash("kick", "Exclure un membre").addOptions(
                    OptionData(OptionType.USER, "membre", "Le membre à exclure", true),
                    OptionData(OptionType.STRING, "raison", "La raison de l'exclusion")
                ),
                Commands.slash("ban", "Bannir un membre définivement").addOptions(
                    OptionData(OptionType.USER, "membre", "Le membre à bannir", true),
                    OptionData(OptionType.STRING, "raison", "La raison du banissement")
                ),
                Commands.slash("unban", "Débannir un utilisateur").addOptions(
                    OptionData(OptionType.NUMBER, "id", "L'ID de l'utilisateur", true),
                    OptionData(OptionType.STRING, "raison", "La raison du débanissement")
                )
            )
        }
    }

    override fun onSlashCommandInteraction(event: SlashCommandInteractionEvent) {
        if (event.guild == null) return

        when (event.name) {
            "clear" -> clearCommand(event)
            "kick" -> kickCommand(event)
            "ban" -> banCommand(event)
            "unban" -> unbanCommand(event)
        }
    }

    private fun checkPermissions(event: SlashCommandInteractionEvent, self: Boolean, vararg permissions: Permission): Boolean {
        val member = if (self) event.guild!!.selfMember else event.member!!
        val desc = if (self) "Je n'ai" else "Tu n'as"

        if (!member.hasPermission(*permissions)) {
            replyEmbed(event.interaction, Colors.RED, "❌ $desc la permission de faire ça", true)
            return true
        }

        return false
    }

    private fun clearCommand(event: SlashCommandInteractionEvent) {
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

    private fun kickCommand(event: SlashCommandInteractionEvent) {
        if (checkPermissions(event, true, Permission.KICK_MEMBERS)) return
        if (checkPermissions(event, false, Permission.KICK_MEMBERS)) return

        val user = event.getOption("membre")!!.asUser
        val reason = event.getOption("raison")?.asString ?: "Pas de raison"

        event.guild!!.kick(user, reason).queue()
        replyEmbed(
            event.interaction, Colors.GREEN, "\uD83D\uDD28 ${user.asMention} a été exclu\n❔ Raison : $reason"
        )
    }

    private fun banCommand(event: SlashCommandInteractionEvent) {
        if (checkPermissions(event, true, Permission.BAN_MEMBERS)) return
        if (checkPermissions(event, false, Permission.BAN_MEMBERS)) return

        val user = event.getOption("membre")!!.asUser
        val reason = event.getOption("raison")?.asString ?: "Pas de raison"

        event.guild!!.ban(user, 0, reason).queue()
        replyEmbed(
            event.interaction, Colors.GREEN, "\uD83D\uDCE4 ${user.asMention} a été banni\n❔ Raison : $reason"
        )
    }

    private fun unbanCommand(event: SlashCommandInteractionEvent) {
        if (checkPermissions(event, true, Permission.BAN_MEMBERS)) return
        if (checkPermissions(event, false, Permission.BAN_MEMBERS)) return

        val user = event.jda.getUserById(event.getOption("id")!!.asLong)
        val reason = event.getOption("raison")?.asString ?: "Pas de raison"

        if (user == null) {
            replyEmbed(event.interaction, Colors.RED, "❌ Aucun utilisateur correspondant trouvé")
            return
        }

        event.guild!!.unban(user).queue()
        replyEmbed(
            event.interaction, Colors.ORANGE, "\uD83E\uDDD1\u200D⚖ ${user.asMention} a été débanni️\n❔ Raison : $reason"
        )
    }
}