package commands.setup;

import database.Database;
import framework.Command;
import framework.Parameter;
import org.javacord.api.entity.channel.ServerChannel;
import org.javacord.api.entity.message.embed.EmbedBuilder;
import org.javacord.api.entity.permission.PermissionType;
import org.javacord.api.event.interaction.SlashCommandCreateEvent;
import org.javacord.api.interaction.SlashCommandInteraction;
import org.javacord.api.interaction.SlashCommandOptionType;
import org.javacord.api.listener.interaction.SlashCommandCreateListener;
import ressources.Global;

import java.util.Optional;

@Parameter(name = "salon", description = "Le salon où seront envoyées les logs", type = SlashCommandOptionType.CHANNEL, isRequired = false)
@Command(name = "logs", description = "Définir le salon où seront envoyées les logs")
public class SetLogsCommand implements SlashCommandCreateListener {
    @Override
    public void onSlashCommandCreate(SlashCommandCreateEvent event) {
        SlashCommandInteraction interaction = event.getSlashCommandInteraction();
        if (interaction.getOptionByName("logs").isEmpty()) return;

        if (interaction.getServer().isEmpty()) {
            Global.sendErrorMessage(interaction, "Cette commande n'est utilisable que dans un serveur");
            return;
        }

        if (!interaction.getServer().get().hasPermission(interaction.getUser(), PermissionType.MANAGE_SERVER)) {
            Global.sendErrorMessage(interaction, "Tu n'as pas la permission de modifier le salon des logs");
            return;
        }

        long id;
        EmbedBuilder builder = new EmbedBuilder();

        Optional<ServerChannel> channel = interaction.getOptionByName("logs").get().getOptionChannelValueByName("salon");

        if (channel.isEmpty()) {
            id = 0;
            builder.setColor(Global.ORANGE).setDescription("❌ Plus aucun log ne sera envoyé");
        } else {
            id = channel.get().getId();
            builder.setColor(Global.GOLD).setDescription("✅ Les logs seront envoyés dans <#" + channel.get().getId() + ">");
        }

        Database.updateSettings(interaction.getServer().get(), "logsChannelId", id);
        Global.sendResponseEmbed(interaction, builder);
    }
}
