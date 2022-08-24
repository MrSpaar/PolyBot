package events.logs;

import database.Database;
import org.javacord.api.entity.auditlog.AuditLogActionType;
import org.javacord.api.entity.auditlog.AuditLogEntry;
import org.javacord.api.entity.channel.ServerTextChannel;
import org.javacord.api.entity.message.embed.EmbedBuilder;
import org.javacord.api.event.user.UserChangeNicknameEvent;
import org.javacord.api.listener.user.UserChangeNicknameListener;
import ressources.Global;

public class NicknameUpdateEvent implements UserChangeNicknameListener {
    @Override
    public void onUserChangeNickname(UserChangeNicknameEvent event) {
        if (event.getOldNickname().isEmpty() || event.getNewNickname().isEmpty()) return;

        ServerTextChannel channel = Database.getLogsChannel(event.getServer());
        if (channel == null) return;

        event.getServer().getAuditLog(1, AuditLogActionType.MEMBER_UPDATE).thenAccept(auditLog -> {
            AuditLogEntry entry = auditLog.getEntries().get(0);
            EmbedBuilder builder = new EmbedBuilder()
                    .setColor(Global.BLUE);

            entry.getUser().thenAccept(user -> {
                String summary = "(`" + event.getOldNickname().get() + "` → `" + event.getNewNickname().get() + "`)";
                StringBuilder description = new StringBuilder("\uD83D\uDCDD ").append(user.getMentionTag());

                if (user.getId() == event.getUser().getId()) {
                    description.append(" a changé son surnom ");
                } else {
                    description.append(" a changé le surnom de ").append(event.getUser().getMentionTag()).append(" ");
                }

                description.append(summary);
                channel.sendMessage(builder.setDescription(description.toString()));
            });
        });
    }
}
