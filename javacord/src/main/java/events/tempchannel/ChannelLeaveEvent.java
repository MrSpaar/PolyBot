package events.tempchannel;

import database.Database;
import database.TempChannelEntry;
import org.javacord.api.entity.channel.ServerChannel;
import org.javacord.api.event.channel.server.voice.ServerVoiceChannelMemberLeaveEvent;
import org.javacord.api.listener.channel.server.voice.ServerVoiceChannelMemberLeaveListener;

public class ChannelLeaveEvent implements ServerVoiceChannelMemberLeaveListener {
    @Override
    public void onServerVoiceChannelMemberLeave(ServerVoiceChannelMemberLeaveEvent event) {
        if (!event.getServer().canYouCreateChannels()) return;
        if (event.getChannel().getConnectedUsers().size() > 0) return;

        TempChannelEntry entry = Database.getTempChannel(event.getChannel());
        if (entry == null) return;

        event.getServer().getTextChannelById(entry.getTxtId()).ifPresent(ServerChannel::delete);
        event.getServer().getVoiceChannelById(entry.getVocId()).ifPresent(ServerChannel::delete);
        Database.deleteTempChannel(entry);
    }
}
