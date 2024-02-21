package events.logs;

import database.Database;
import org.javacord.api.entity.channel.ServerTextChannel;
import org.javacord.api.entity.message.embed.EmbedBuilder;
import org.javacord.api.event.channel.server.invite.ServerChannelInviteCreateEvent;
import org.javacord.api.listener.channel.server.invite.ServerChannelInviteCreateListener;
import ressources.Global;

public class InviteCreateEvent implements ServerChannelInviteCreateListener {
    @Override
    public void onServerChannelInviteCreate(ServerChannelInviteCreateEvent event) {
        ServerTextChannel channel = Database.getLogsChannel(event.getServer());
        if (channel == null) return;
        if (event.getInvite().getInviter().isEmpty()) return;

        event.getChannel().getInvites().thenAccept(richInvites -> richInvites.forEach(richInvite -> {
            if (richInvite.getCode().equals(event.getInvite().getCode())) {
                String uses = richInvite.getMaxUses() == 0 ? "à l'infini": richInvite.getMaxUses() + " fois";
                String expiresIn = "<t:" + richInvite.getCreationTimestamp().plusSeconds(richInvite.getMaxAgeInSeconds()).getEpochSecond() + ":R>";
                String mention  = event.getInvite().getInviter().get().getMentionTag();

                channel.sendMessage(new EmbedBuilder()
                        .setColor(Global.BLUE)
                        .setDescription("✉️ " + mention + " a créé une [invitation](" + richInvite.getUrl() + ") qui expire " + expiresIn + ", utilisable " + uses)
                );
            }
        }));
    }
}
