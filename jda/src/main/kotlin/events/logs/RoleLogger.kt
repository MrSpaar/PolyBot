package events.logs

import Colors
import Database
import Translate
import net.dv8tion.jda.api.audit.ActionType
import net.dv8tion.jda.api.events.guild.member.GuildMemberRoleAddEvent
import net.dv8tion.jda.api.events.guild.member.GuildMemberRoleRemoveEvent
import net.dv8tion.jda.api.events.role.RoleCreateEvent
import net.dv8tion.jda.api.events.role.RoleDeleteEvent
import net.dv8tion.jda.api.events.role.update.RoleUpdateColorEvent
import net.dv8tion.jda.api.events.role.update.RoleUpdateNameEvent
import net.dv8tion.jda.api.events.role.update.RoleUpdatePermissionsEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter

class RoleLogger: ListenerAdapter() {
    override fun onRoleCreate(event: RoleCreateEvent) {
        val channel = Database.getLogsChannel(event.guild) ?: return

        event.guild.retrieveAuditLogs().type(ActionType.ROLE_CREATE).limit(1).queue {
            val user = it[0].user?.asMention ?: "Discord"
            Logs.sendLog(channel, Colors.GREEN, "\uD83D\uDCCC $user a créé le rôle ${event.role.asMention}")
        }
    }

    override fun onRoleUpdateColor(event: RoleUpdateColorEvent) {
        val channel = Database.getLogsChannel(event.guild) ?: return

        event.guild.retrieveAuditLogs().type(ActionType.ROLE_UPDATE).limit(1).queue {
            val user = it[0].user?.asMention ?: "Discord"
            val update = "`${event.oldColorRaw}` → `${event.newColorRaw}`"

            Logs.sendLog(channel, Colors.BLUE, "\uD83C\uDF08 $user a modifié la couleur de ${event.role.asMention} ($update)")
        }
    }

    override fun onRoleUpdateName(event: RoleUpdateNameEvent) {
        val channel = Database.getLogsChannel(event.guild) ?: return

        event.guild.retrieveAuditLogs().type(ActionType.ROLE_UPDATE).limit(1).queue {
            val user = it[0].user?.asMention ?: "Discord"
            val update = "`${event.oldName}` → `${event.newName}`"

            Logs.sendLog(channel, Colors.BLUE, "\uD83C\uDFF7️ $user a modifié le nom de ${event.role.asMention} ($update)")
        }
    }

    override fun onRoleUpdatePermissions(event: RoleUpdatePermissionsEvent) {
        val channel = Database.getLogsChannel(event.guild) ?: return

        event.guild.retrieveAuditLogs().type(ActionType.ROLE_UPDATE).limit(1).queue { entry ->
            val user = entry[0].user?.asMention ?: "Discord"

            val added = event.newPermissions
                .filter { it !in event.oldPermissions }.joinToString("\n") { "✅ " + Translate.PERMISSION[it] }

            val removed = event.oldPermissions
                .filter { it !in event.newPermissions }.joinToString("\n") { "❌ " + Translate.PERMISSION[it] }

            Logs.sendLog(channel, Colors.BLUE, "$user a changé les permissions de ${event.role.asMention}\n$added\n$removed")
        }
    }

    override fun onRoleDelete(event: RoleDeleteEvent) {
        val channel = Database.getLogsChannel(event.guild) ?: return

        event.guild.retrieveAuditLogs().type(ActionType.ROLE_DELETE).limit(1).queue {
            val user = it[0].user?.asMention ?: "Discord"
            Logs.sendLog(channel, Colors.RED, "\uD83D\uDDD1️ $user a supprimé le rôle `@${event.role.name}`")
        }
    }

    override fun onGuildMemberRoleAdd(event: GuildMemberRoleAddEvent) {
        val channel = Database.getLogsChannel(event.guild) ?: return

        event.guild.retrieveAuditLogs().type(ActionType.MEMBER_ROLE_UPDATE).limit(1).queue {
            var description = "\uD83D\uDCCC "

            description += if (it[0].user == event.user) "${event.user.asMention} s'est ajouté ${event.roles[0].asMention}"
            else "${it[0].user?.asMention ?: "Discord"} a ajouté ${event.roles[0].asMention} a ${event.user.asMention}"

            Logs.sendLog(channel, Colors.BLUE, description)
        }
    }

    override fun onGuildMemberRoleRemove(event: GuildMemberRoleRemoveEvent) {
        val channel = Database.getLogsChannel(event.guild) ?: return

        event.guild.retrieveAuditLogs().type(ActionType.MEMBER_ROLE_UPDATE).limit(1).queue {
            var description = "\uD83D\uDCCC "

            description += if (it[0].user == event.user) "${event.user.asMention} s'est retiré ${event.roles[0].asMention}"
            else "${it[0].user?.asMention ?: "Discord"} a retiré ${event.roles[0].asMention} a ${event.user.asMention}"

            Logs.sendLog(channel, Colors.BLUE, description)
        }
    }
}