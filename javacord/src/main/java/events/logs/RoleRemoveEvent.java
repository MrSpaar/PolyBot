package events.logs;

import database.Database;
import org.javacord.api.entity.auditlog.AuditLogActionType;
import org.javacord.api.entity.auditlog.AuditLogEntry;
import org.javacord.api.entity.channel.ServerTextChannel;
import org.javacord.api.entity.message.embed.EmbedBuilder;
import org.javacord.api.event.server.role.UserRoleRemoveEvent;
import org.javacord.api.listener.server.role.UserRoleRemoveListener;
import ressources.Global;

public class RoleRemoveEvent implements UserRoleRemoveListener {
    @Override
    public void onUserRoleRemove(UserRoleRemoveEvent event) {
        ServerTextChannel channel = Database.getLogsChannel(event.getServer());
        if (channel == null) return;

        event.getServer().getAuditLog(1, AuditLogActionType.MEMBER_ROLE_UPDATE).thenAccept(auditLog -> {
            AuditLogEntry entry = auditLog.getEntries().get(0);
            EmbedBuilder builder = new EmbedBuilder().setColor(Global.BLUE);

            entry.getUser().thenAccept(user -> {
                StringBuilder description = new StringBuilder("\uD83D\uDCDD ").append(user.getMentionTag());

                if (user.getId() == event.getUser().getId()) {
                    description.append(" s'est retiré ").append(event.getRole().getMentionTag());
                } else {
                    description.append(" a retiré ").append(event.getRole().getMentionTag())
                            .append(" à ").append(event.getUser().getMentionTag());
                }

                channel.sendMessage(builder.setDescription(description.toString()));
            });
        });
    }
}
