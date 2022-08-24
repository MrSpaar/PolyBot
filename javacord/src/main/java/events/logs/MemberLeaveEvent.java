package events.logs;

import database.Database;
import org.javacord.api.entity.channel.ServerTextChannel;
import org.javacord.api.entity.message.embed.EmbedBuilder;
import org.javacord.api.event.server.member.ServerMemberLeaveEvent;
import org.javacord.api.listener.server.member.ServerMemberLeaveListener;
import ressources.Global;

public class MemberLeaveEvent implements ServerMemberLeaveListener {
    @Override
    public void onServerMemberLeave(ServerMemberLeaveEvent event) {
        if (event.getUser().isBot()) return;
        Database.removeServerFromUser(event.getServer(), event.getUser());

        ServerTextChannel channel = Database.getLogsChannel(event.getServer());
        if (channel == null) return;

        channel.sendMessage(new EmbedBuilder()
                .setColor(Global.RED)
                .setDescription(":outbox_tray: " + event.getUser().getMentionTag() + " a quitté le serveur")
        );
    }
}
