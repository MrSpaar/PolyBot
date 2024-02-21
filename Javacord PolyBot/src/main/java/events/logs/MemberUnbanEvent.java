package events.logs;

import database.Database;
import org.javacord.api.entity.auditlog.AuditLogActionType;
import org.javacord.api.entity.auditlog.AuditLogEntry;
import org.javacord.api.entity.channel.ServerTextChannel;
import org.javacord.api.entity.message.embed.EmbedBuilder;
import org.javacord.api.event.server.member.ServerMemberUnbanEvent;
import org.javacord.api.listener.server.member.ServerMemberUnbanListener;
import ressources.Global;

public class MemberUnbanEvent implements ServerMemberUnbanListener {
    @Override
    public void onServerMemberUnban(ServerMemberUnbanEvent event) {
        ServerTextChannel channel = Database.getLogsChannel(event.getServer());
        if (channel == null) return;

        event.getServer().getAuditLog(1, AuditLogActionType.MEMBER_BAN_REMOVE).thenAccept(auditLog -> {
            AuditLogEntry entry = auditLog.getEntries().get(0);
            entry.getUser().thenAccept(user ->
                    channel.sendMessage(new EmbedBuilder()
                            .setColor(Global.ORANGE)
                            .setDescription(
                                    "\uD83D\uDC68\u200D⚖️ " + user.getMentionTag() + " a unban " + event.getUser().getMentionTag() +
                                            "\n❔ Raison : " + entry.getReason().orElse("Pas de raison")
                            )
                    )
            );
        });
    }
}
