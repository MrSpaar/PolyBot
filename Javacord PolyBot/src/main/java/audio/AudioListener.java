package audio;

import com.sedmelluq.discord.lavaplayer.player.event.AudioEvent;
import com.sedmelluq.discord.lavaplayer.player.event.AudioEventListener;
import com.sedmelluq.discord.lavaplayer.player.event.TrackEndEvent;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;

public class AudioListener implements AudioEventListener {
    private final ServerNode node;

    public AudioListener(ServerNode node) {
        this.node = node;
    }

    @Override
    public void onEvent(AudioEvent event) {
        if (event instanceof TrackEndEvent) {
            AudioTrack next = node.getNextTrack();

            if (next == null) {
                AudioHandler.destroyNode(node);
                return;
            }

            node.getPlayer().playTrack(next);
            node.updateMessage();
        }
    }
}
