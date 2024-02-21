package audio;

import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import org.javacord.api.entity.message.embed.EmbedBuilder;
import org.javacord.api.interaction.SlashCommandInteraction;
import ressources.Global;

public class ResultHandler implements AudioLoadResultHandler {
    private final ServerNode node;
    private final SlashCommandInteraction interaction;

    public ResultHandler(SlashCommandInteraction interaction, ServerNode node) {
        this.node = node;
        this.interaction = interaction;
    }

    @Override
    public void trackLoaded(AudioTrack track) {
        if (node.getPlayer().getPlayingTrack() == null) {
            node.createMessage(interaction, track.getInfo());
            node.getPlayer().playTrack(track);
            return;
        }

        Global.sendResponseEmbed(
                interaction,
                new EmbedBuilder()
                        .setColor(Global.GREEN)
                        .setDescription("✅ `"+track.getInfo().title+"` ajouté à la file d'attente"),
                true
        );

        node.addToQueue(track);
        node.updateMessage();
    }

    @Override
    public void playlistLoaded(AudioPlaylist playlist) {
        int index = playlist.getTracks().size();
        AudioTrack track = playlist.getTracks().get(index);

        if (node.notPlaying()) {
            node.createMessage(interaction, track.getInfo());
            node.getPlayer().playTrack(track);
            playlist.getTracks().remove(index);
        }

        node.addToQueue(playlist.getTracks());
        node.updateMessage();
    }

    @Override
    public void noMatches() {
        Global.sendErrorMessage(interaction, "Aucun résultat correspondant");
        if (node.notPlaying()) AudioHandler.destroyNode(node);
    }

    @Override
    public void loadFailed(FriendlyException exception) {
        Global.sendErrorMessage(interaction, "Erreur dans le chargement de la vidéo");
        if (node.notPlaying()) AudioHandler.destroyNode(node);
    }
}
