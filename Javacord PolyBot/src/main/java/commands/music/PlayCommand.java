package commands.music;

import audio.AudioHandler;
import audio.ServerNode;
import framework.Parameter;
import framework.Command;
import org.javacord.api.entity.channel.ServerVoiceChannel;
import org.javacord.api.entity.permission.PermissionType;
import org.javacord.api.entity.server.Server;
import org.javacord.api.event.interaction.SlashCommandCreateEvent;
import org.javacord.api.interaction.SlashCommandInteraction;
import org.javacord.api.listener.interaction.SlashCommandCreateListener;
import ressources.Global;

@Parameter(name = "video", description = "La vidéo que tu recherches")
@Command(name = "play", description = "Ecouter une vidéo youtube")
public class PlayCommand implements SlashCommandCreateListener {
    @Override
    public void onSlashCommandCreate(SlashCommandCreateEvent event) {
        SlashCommandInteraction interaction = event.getSlashCommandInteraction();
        if (!interaction.getCommandName().equals("play")) return;

        if (interaction.getServer().isEmpty()) {
            Global.sendErrorMessage(interaction, "Cette commande n'est utilisable que dans un serveur");
            return;
        }

        if (interaction.getOptionStringValueByName("video").isEmpty()) {
            Global.sendErrorMessage(interaction, "Tu n'as pas spécifié de vidéo à écouter");
            return;
        }

        Server server = interaction.getServer().get();
        String query = interaction.getOptionStringValueByName("video").get();

        if (interaction.getUser().getConnectedVoiceChannel(server).isEmpty()) {
            Global.sendErrorMessage(interaction, "Tu dois être connecté à un salon vocal");
            return;
        }

        ServerVoiceChannel channel = interaction.getUser().getConnectedVoiceChannel(server).get();

        if (!channel.canYouSee() || !channel.canYouConnect()) {
            Global.sendErrorMessage(interaction, "Impossible de me connecter à ton salon");
            return;
        }

        if (!channel.hasPermission(interaction.getApi().getYourself(), PermissionType.SPEAK)) {
            Global.sendErrorMessage(interaction, "Je ne peux pas parler dans ton salon");
            return;
        }

        ServerNode node = AudioHandler.getOrCreateNode(channel);
        node.loadItem(interaction, query);
    }
}
