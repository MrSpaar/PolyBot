package audio;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.source.youtube.YoutubeAudioSourceManager;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackInfo;
import org.javacord.api.audio.AudioConnection;
import org.javacord.api.audio.AudioSource;
import org.javacord.api.entity.channel.ServerVoiceChannel;
import org.javacord.api.entity.message.embed.EmbedBuilder;
import org.javacord.api.interaction.SlashCommandInteraction;
import org.javacord.api.interaction.callback.InteractionOriginalResponseUpdater;
import ressources.Global;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class ServerNode {
    private final long id;
    private final AudioSource source;
    private final AudioPlayer player;
    private final AudioPlayerManager manager;
    private final ArrayList<AudioTrack> queue;
    private final CompletableFuture<AudioConnection> connection;
    private CompletableFuture<InteractionOriginalResponseUpdater> updater;

    public ServerNode(ServerVoiceChannel channel) {
        id = channel.getServer().getId();
        manager = new DefaultAudioPlayerManager();
        manager.registerSourceManager(new YoutubeAudioSourceManager());

        player = manager.createPlayer();
        player.addListener(new AudioListener(this));
        source = new LavaplayerAudioSource(channel.getApi(), player);

        connection = channel.connect().thenApply(conn -> {
            conn.setAudioSource(source);
            conn.setSelfDeafened(true);
            return conn;
        });

        queue = new ArrayList<>();
    }

    public long getId() {
        return id;
    }

    public AudioPlayer getPlayer() {
        return player;
    }

    public void createMessage(SlashCommandInteraction interaction, AudioTrackInfo info) {
        updater = interaction.createImmediateResponder()
                .addEmbed(
                        new EmbedBuilder()
                                .setColor(Global.BLUE)
                                .setDescription("\uD83C\uDFB5 [`"+info.title+"`]("+info.uri+") de `"+info.author+"`")
                )
                .addEmbed(
                        new EmbedBuilder()
                                .setColor(Global.LIGHT_GRAY)
                                .setDescription("*Pas de vidéo en attente*")
                )
                .respond();
    }

    public void updateMessage() {
        int pos = 0;
        StringBuilder builder = new StringBuilder();

        for (AudioTrack track: queue) {
            pos++;
            AudioTrackInfo info = track.getInfo();
            builder.append(pos).append(") [`").append(info.title).append("`](").append(info.uri).append(") de `").append(info.author).append("`\n");
        }

        AudioTrackInfo info = player.getPlayingTrack().getInfo();

        updater.thenAccept(updater -> updater.removeAllEmbeds()
                .addEmbed(new EmbedBuilder()
                        .setColor(Global.BLUE)
                        .setDescription("\uD83C\uDFB5 [`"+info.title+"`]("+info.uri+") de `"+info.author+"`")
                )
                .addEmbed(new EmbedBuilder()
                        .setColor(Global.LIGHT_GRAY)
                        .setDescription(builder.isEmpty() ? "*Aucune vidéo en attente*": builder.toString()))
                .update());
    }

    public AudioTrack getNextTrack() {
        if (queue.isEmpty()) return null;

        return queue.remove(0);
    }

    public void addToQueue(AudioTrack track) {
        queue.add(track);
    }

    public void addToQueue(List<AudioTrack> tracks) {
        queue.addAll(tracks);
    }

    public void skipCurrentTrack() {
        player.stopTrack();
    }

    public void disconnect() {
        player.destroy();
        connection.thenAccept(AudioConnection::close);
        updater.thenAccept(InteractionOriginalResponseUpdater::delete);
    }

    public boolean notPlaying() {
        return player.getPlayingTrack() == null;
    }

    public void loadItem(SlashCommandInteraction interaction, String query) {
        connection.thenAccept(conn -> manager.loadItem(
                Global.isUrl(query) ? query: "ytsearch: " + query,
                new ResultHandler(interaction, this)
        ));
    }
}
