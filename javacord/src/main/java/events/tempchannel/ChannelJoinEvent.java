package events.tempchannel;

import database.Database;
import database.TempChannelEntry;
import org.javacord.api.entity.channel.ServerTextChannelBuilder;
import org.javacord.api.entity.channel.ServerVoiceChannelBuilder;
import org.javacord.api.event.channel.server.voice.ServerVoiceChannelMemberJoinEvent;
import org.javacord.api.listener.channel.server.voice.ServerVoiceChannelMemberJoinListener;

public class ChannelJoinEvent implements ServerVoiceChannelMemberJoinListener {
    @Override
    public void onServerVoiceChannelMemberJoin(ServerVoiceChannelMemberJoinEvent event) {
        if (!event.getChannel().getName().contains("Créer")) return;
        if (!event.getServer().canYouMoveMembers() || !event.getServer().canYouCreateChannels()) return;

        TempChannelEntry entry = Database.getTempChannel(event.getChannel());
        if (entry != null) return;

        String name = "Salon de " + event.getUser().getDisplayName(event.getServer());

        ServerTextChannelBuilder tBuilder = event.getServer().createTextChannelBuilder()
                .setName(name);
        ServerVoiceChannelBuilder vBuilder = event.getServer().createVoiceChannelBuilder()
                .setName(name);

        if (event.getChannel().getCategory().isPresent()) {
            tBuilder.setCategory(event.getChannel().getCategory().get());
            vBuilder.setCategory(event.getChannel().getCategory().get());
        }

        tBuilder.create().thenAccept(tChannel -> vBuilder.create().thenAccept(vChannel -> {
            event.getServer().moveUser(event.getUser(), vChannel);
            Database.insertTempChannel(event.getUser(), vChannel, tChannel);
        }));
    }
}
