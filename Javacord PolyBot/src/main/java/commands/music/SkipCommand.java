package commands.music;

import audio.AudioHandler;
import audio.ServerNode;
import framework.Command;
import org.javacord.api.entity.message.embed.EmbedBuilder;
import org.javacord.api.entity.server.Server;
import org.javacord.api.event.interaction.SlashCommandCreateEvent;
import org.javacord.api.interaction.SlashCommandInteraction;
import org.javacord.api.listener.interaction.SlashCommandCreateListener;
import ressources.Global;

@Command(name = "skip", description = "Passer la vidéo en cours de lecture")
public class SkipCommand implements SlashCommandCreateListener {
    @Override
    public void onSlashCommandCreate(SlashCommandCreateEvent event) {
        SlashCommandInteraction interaction = event.getSlashCommandInteraction();
        if (!interaction.getCommandName().equals("skip")) return;

        if (interaction.getServer().isEmpty()) {
            Global.sendErrorMessage(interaction, "Cette commande n'est utilisable que dans un serveur");
            return;
        }

        Server server = interaction.getServer().get();

        if (interaction.getUser().getConnectedVoiceChannel(server).isEmpty()) {
            Global.sendErrorMessage(interaction, "Tu dois être connecté à un salon vocal");
            return;
        }

        ServerNode node = AudioHandler.getNode(interaction.getUser().getConnectedVoiceChannel(server).get());

        if (node == null || node.notPlaying()) {
            Global.sendErrorMessage(interaction, "Je ne joue aucune vidéo en ce moment");
            return;
        }

        node.skipCurrentTrack();
        Global.sendResponseEmbed(
                interaction,
                new EmbedBuilder()
                        .setColor(Global.GREEN)
                        .setDescription("✅ La vidéo a été skip"),
                true
        );
    }
}
