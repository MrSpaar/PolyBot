package events.logs

import database.Database
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.audit.ActionType
import net.dv8tion.jda.api.events.guild.member.GuildMemberJoinEvent
import net.dv8tion.jda.api.events.guild.member.GuildMemberRemoveEvent
import net.dv8tion.jda.api.events.guild.member.update.GuildMemberUpdateNicknameEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter

class MemberLogger: ListenerAdapter() {
    override fun onGuildMemberJoin(event: GuildMemberJoinEvent) {
        if (event.user.isBot) return
        Database.updateMember(event.guild.idLong, event.user.idLong)

        val settings = Database.cache[event.guild.idLong] ?: return

        event.guild.getRoleById(settings.newcomerRoleId)?.apply {
            event.guild.addRoleToMember(event.user, this).queue()
        }

        event.guild.getTextChannelById(settings.welcomeChannelId)?.apply {
            this.sendMessage(settings.welcomeText.replace("<mention>", event.user.asMention)).queue()
        }

        event.guild.getTextChannelById(settings.logsChannelId)?.apply {
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
        Database.deleteMember(event.guild.idLong, event.user.idLong)

        val channel = Database.getLogsChannel(event.guild) ?: return

        channel.sendMessageEmbeds(
            EmbedBuilder()
                .setColor(Colors.RED)
                .setDescription(":outbox_tray: ${event.user.asMention} a quitté le serveur")
                .build()
        ).queue()
    }

    override fun onGuildMemberUpdateNickname(event: GuildMemberUpdateNicknameEvent) {
        val oldNick = event.oldNickname ?: event.user.name
        val newNick = event.newNickname ?: event.member.effectiveName

        val channel = Database.getLogsChannel(event.guild) ?: return

        event.guild.retrieveAuditLogs().type(ActionType.MEMBER_UPDATE).limit(1).queue {
            var description = "\uD83C\uDFF7️ "

            description += if(it[0].user == event.user) "${event.user.asMention} a changé son surnom "
            else "${it[0].user?.asMention ?: "Discord"} a changé le surnom de ${event.user.asMention} "

            description += "`$oldNick` → `$newNick`"

            channel.sendMessageEmbeds(
                EmbedBuilder()
                    .setColor(Colors.BLUE)
                    .setDescription(description)
                    .build()
            ).queue()
        }
    }
}