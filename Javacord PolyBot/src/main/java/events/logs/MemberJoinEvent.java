package events.logs;

import database.Database;
import database.SettingsEntry;
import org.javacord.api.entity.channel.ServerTextChannel;
import org.javacord.api.entity.message.embed.EmbedBuilder;
import org.javacord.api.entity.permission.Role;
import org.javacord.api.entity.user.User;
import org.javacord.api.event.server.member.ServerMemberJoinEvent;
import org.javacord.api.listener.server.member.ServerMemberJoinListener;
import ressources.Global;

import java.util.Optional;

public class MemberJoinEvent implements ServerMemberJoinListener {
    @Override
    public void onServerMemberJoin(ServerMemberJoinEvent event) {
        if (event.getUser().isBot()) return;
        Database.addServerToUser(event.getServer(), event.getUser());

        SettingsEntry settings = Database.getSettings(event.getServer());
        if (settings == null) return;
        if (settings.getLogsChannelId() == 0) return;

        Optional<Role> newcomerRole = event.getServer().getRoleById(settings.getNewcomerRoleId());
        Optional<ServerTextChannel> logsChannel = event.getServer().getTextChannelById(settings.getLogsChannelId());
        Optional<ServerTextChannel> welcomeChannel = event.getServer().getTextChannelById(settings.getWelcomeChannelId());

        newcomerRole.ifPresent(role -> addRole(event.getUser(), role));
        logsChannel.ifPresent(channel -> sendLog(channel, event.getUser()));
        welcomeChannel.ifPresent(channel -> welcomeUser(channel, event.getUser(), settings.getWelcomeText()));
    }

    private void sendLog(ServerTextChannel channel, User user) {
        channel.sendMessage(new EmbedBuilder()
                .setColor(Global.GREEN)
                .setDescription(":inbox_tray: " + user.getMentionTag() + " a rejoint le serveur !")
        );
    }

    private void addRole(User user, Role role) {
        role.addUser(user);
    }

    private void welcomeUser(ServerTextChannel channel, User user, String message) {
        channel.sendMessage(message.replace("<mention>", user.getMentionTag()));
    }
}
