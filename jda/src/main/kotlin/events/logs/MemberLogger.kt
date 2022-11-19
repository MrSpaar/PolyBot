package events.logs

import Colors
import Database
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.audit.ActionType
import net.dv8tion.jda.api.events.guild.member.GuildMemberJoinEvent
import net.dv8tion.jda.api.events.guild.member.GuildMemberRemoveEvent
import net.dv8tion.jda.api.events.guild.member.update.GuildMemberUpdateNicknameEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter

class MemberLogger: ListenerAdapter() {
    override fun onGuildMemberJoin(event: GuildMemberJoinEvent) {
        if (event.user.isBot) return

        Database.ensureUser(event.guild.idLong, event.user.idLong)
        val settings = Database.getSettings(event.guild.idLong)

        event.guild.getRoleById(settings.getLong("newcomer_role_id"))?.apply {
            event.guild.addRoleToMember(event.user, this).queue()
        }

        event.guild.getTextChannelById(settings.getLong("welcome_chan_id"))?.apply {
            this.sendMessage(
                settings.getString("welcome_text").replace("<mention>", event.user.asMention)
            ).queue()
        }

        event.guild.getTextChannelById(settings.getLong("logs_chan_id"))?.apply {
            this.sendMessageEmbeds(
                EmbedBuilder()
                    .setColor(Colors.GREEN)
                    .setDescription(":inbox_tray: ${event.user.asMention} a rejoint le serveur")
                    .build()
            ).queue()
        }
    }

    override fun onGuildMemberRemove(event: GuildMemberRemoveEvent) {
        if (event.user.isBot) return

        val channel = Database.getLogsChannel(event.guild) ?: return

        Database.deleteUser(event.guild.idLong, event.user.idLong)
        Logs.sendLog(channel, Colors.RED, ":outbox_tray: ${event.user.asMention} a quitté le serveur")
    }

    override fun onGuildMemberUpdateNickname(event: GuildMemberUpdateNicknameEvent) {
        val channel = Database.getLogsChannel(event.guild) ?: return

        val oldNick = event.oldNickname ?: event.user.name
        val newNick = event.newNickname ?: event.member.effectiveName

        event.guild.retrieveAuditLogs().type(ActionType.MEMBER_UPDATE).limit(1).queue {
            var description = "\uD83C\uDFF7️ "

            description += if(it[0].user == event.user) "${event.user.asMention} a changé son surnom "
            else "${it[0].user?.asMention ?: "Discord"} a changé le surnom de ${event.user.asMention} "

            description += "`$oldNick` → `$newNick`"

            Logs.sendLog(channel, Colors.BLUE, description)
        }
    }
}