package events.logs

import Colors
import Database
import net.dv8tion.jda.api.audit.ActionType
import net.dv8tion.jda.api.events.guild.GuildBanEvent
import net.dv8tion.jda.api.events.guild.GuildJoinEvent
import net.dv8tion.jda.api.events.guild.GuildLeaveEvent
import net.dv8tion.jda.api.events.guild.GuildUnbanEvent
import net.dv8tion.jda.api.events.guild.invite.GuildInviteCreateEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter

class GuildLogger: ListenerAdapter() {
    override fun onGuildJoin(event: GuildJoinEvent) {
        Database.ensureGuild(event.guild.idLong)
    }

    override fun onGuildLeave(event: GuildLeaveEvent) {
        Database.deleteGuild(event.guild.idLong)
    }

    override fun onGuildInviteCreate(event: GuildInviteCreateEvent) {
        if (event.invite.inviter == null) return

        val channel = Database.getLogsChannel(event.guild) ?: return

        val expiresIn = "<t:${event.invite.timeCreated.plusSeconds(event.invite.maxAge.toLong()).toEpochSecond()}:R>"
        val uses = if(event.invite.maxUses == 0) "à l'infini" else event.invite.maxUses

        Logs.sendLog(
            channel, Colors.BLUE,
            "✉️ ${event.invite.inviter!!.asMention} a créé une [invitation](${event.invite.url}) qui expire $expiresIn, utilisable $uses",
        )
    }

    override fun onGuildBan(event: GuildBanEvent) {
        val channel = Database.getLogsChannel(event.guild) ?: return

        event.guild.retrieveAuditLogs().type(ActionType.BAN).limit(1).queue {
            Logs.sendLog(
                channel, Colors.RED,
                "\uD83D\uDC68\u200D⚖️ ${it[0].user!!.asMention} a banni ${event.user.asMention}\n❔ Raison : ${it[0].reason ?: "Pas de raison"}"
            )
        }
    }

    override fun onGuildUnban(event: GuildUnbanEvent) {
        val channel = Database.getLogsChannel(event.guild) ?: return

        event.guild.retrieveAuditLogs().type(ActionType.UNBAN).limit(1).queue {
            Logs.sendLog(
                channel, Colors.RED,
                "\uD83D\uDC68\u200D⚖️ ${it[0].user!!.asMention} a débanni ${event.user.asMention}\n❔ Raison : ${it[0].reason ?: "Pas de raison"}"
            )
        }
    }
}