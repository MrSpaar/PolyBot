package events

import Colors
import database.Database
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.audit.ActionType
import net.dv8tion.jda.api.events.guild.GuildBanEvent
import net.dv8tion.jda.api.events.guild.GuildUnbanEvent
import net.dv8tion.jda.api.events.guild.invite.GuildInviteCreateEvent
import net.dv8tion.jda.api.events.guild.member.GuildMemberJoinEvent
import net.dv8tion.jda.api.events.guild.member.GuildMemberRemoveEvent
import net.dv8tion.jda.api.events.guild.member.GuildMemberRoleAddEvent
import net.dv8tion.jda.api.events.guild.member.GuildMemberRoleRemoveEvent
import net.dv8tion.jda.api.events.guild.member.update.GuildMemberUpdateNicknameEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter

class Logs: ListenerAdapter() {
    override fun onGuildInviteCreate(event: GuildInviteCreateEvent) {
        if (event.invite.inviter == null) return

        val channel = Database.getLogsChannel(event.guild) ?: return

        val expiresIn = "<t:${event.invite.timeCreated.plusSeconds(event.invite.maxAge.toLong()).toEpochSecond()}:R>"
        val uses = if(event.invite.maxUses == 0) "à l'infini" else event.invite.maxUses

        channel.sendMessageEmbeds(
            EmbedBuilder()
                .setColor(Colors.BLUE)
                .setDescription(
                    "✉️ ${event.invite.inviter!!.asMention} a créé une [invitation](${event.invite.url}) qui expire $expiresIn, utilisable $uses"
                )
                .build()
        ).queue()
    }

    override fun onGuildBan(event: GuildBanEvent) {
        val channel = Database.getLogsChannel(event.guild) ?: return

        event.guild.retrieveAuditLogs().type(ActionType.BAN).limit(1).queue {
            channel.sendMessageEmbeds(
                EmbedBuilder()
                    .setColor(Colors.RED)
                    .setDescription(
                        "\uD83D\uDC68\u200D⚖️ ${it[0].user!!.asMention} a banni ${event.user.asMention}\n❔ Raison : ${it[0].reason ?: "Pas de raison"}"
                    )
                    .build()
            ).queue()
        }
    }

    override fun onGuildUnban(event: GuildUnbanEvent) {
        val channel = Database.getLogsChannel(event.guild) ?: return

        event.guild.retrieveAuditLogs().type(ActionType.UNBAN).limit(1).queue {
            channel.sendMessageEmbeds(
                EmbedBuilder()
                    .setColor(Colors.RED)
                    .setDescription(
                        "\uD83D\uDC68\u200D⚖️ ${it[0].user!!.asMention} a débanni ${event.user.asMention}\n❔ Raison : ${it[0].reason ?: "Pas de raison"}"
                    )
                    .build()
            ).queue()
        }
    }

    override fun onGuildMemberJoin(event: GuildMemberJoinEvent) {
        if (event.user.isBot) return
        Database.insertMember(event.guild.idLong, event.user.idLong)

        val settings = Database.cache[event.guild.idLong] ?: return

        event.guild.getRoleById(settings.newcomerRoleId)?.apply {
            event.guild.addRoleToMember(event.user, this)
        }

        event.guild.getTextChannelById(settings.welcomeChannelId)?.apply {
            this.sendMessage(settings.welcomeText.replace("<mention>", event.user.asMention))
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

    override fun onGuildMemberRoleAdd(event: GuildMemberRoleAddEvent) {
        val channel = Database.getLogsChannel(event.guild) ?: return

        event.guild.retrieveAuditLogs().type(ActionType.MEMBER_ROLE_UPDATE).limit(1).queue {
            var description = "\uD83D\uDCDD "

            description += if (it[0].user == event.user) "${event.user.asMention} s'est ajouté ${event.roles[0].asMention}"
                           else "${it[0].user?.asMention ?: "Discord"} a ajouté ${event.roles[0].asMention} a ${event.user.asMention}"

            channel.sendMessageEmbeds(
                EmbedBuilder()
                    .setColor(Colors.BLUE)
                    .setDescription(description)
                    .build()
            ).queue()
        }
    }

    override fun onGuildMemberRoleRemove(event: GuildMemberRoleRemoveEvent) {
        val channel = Database.getLogsChannel(event.guild) ?: return

        event.guild.retrieveAuditLogs().type(ActionType.MEMBER_ROLE_UPDATE).limit(1).queue {
            var description = "\uD83D\uDCDD "

            description += if (it[0].user == event.user) "${event.user.asMention} s'est retiré ${event.roles[0].asMention}"
            else "${it[0].user?.asMention ?: "Discord"} a retiré ${event.roles[0].asMention} a ${event.user.asMention}"

            channel.sendMessageEmbeds(
                EmbedBuilder()
                    .setColor(Colors.BLUE)
                    .setDescription(description)
                    .build()
            ).queue()
        }
    }

    override fun onGuildMemberUpdateNickname(event: GuildMemberUpdateNicknameEvent) {
        val oldNick = event.oldNickname ?: event.user.name
        val newNick = event.newNickname ?: event.member.effectiveName

        val channel = Database.getLogsChannel(event.guild) ?: return

        event.guild.retrieveAuditLogs().type(ActionType.MEMBER_UPDATE).limit(1).queue {
            var description = "\uD83D\uDCDD "

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