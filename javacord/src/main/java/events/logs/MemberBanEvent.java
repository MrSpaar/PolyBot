package events.logs;

import database.Database;
import org.javacord.api.entity.auditlog.AuditLogActionType;
import org.javacord.api.entity.auditlog.AuditLogEntry;
import org.javacord.api.entity.channel.ServerTextChannel;
import org.javacord.api.entity.message.embed.EmbedBuilder;
import org.javacord.api.event.server.member.ServerMemberBanEvent;
import org.javacord.api.listener.server.member.ServerMemberBanListener;
import ressources.Global;

public class MemberBanEvent implements ServerMemberBanListener {
    @Override
    public void onServerMemberBan(ServerMemberBanEvent event) {
        ServerTextChannel channel = Database.getLogsChannel(event.getServer());
        if (channel == null) return;

        event.getServer().getAuditLog(1, AuditLogActionType.MEMBER_BAN_ADD).thenAccept(auditLog -> {
            AuditLogEntry entry = auditLog.getEntries().get(0);
            entry.getUser().thenAccept(user ->
                    channel.sendMessage(new EmbedBuilder()
                            .setColor(Global.RED)
                            .setDescription(
                                    "\uD83D\uDC68\u200D⚖️ " + user.getMentionTag() + " a ban " + event.getUser().getMentionTag() +
                                    "\n❔ Raison : " + entry.getReason().orElse("Pas de raison")
                            )
                    )
            );
        });
    }
}
