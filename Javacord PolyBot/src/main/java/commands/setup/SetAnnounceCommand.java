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

@Parameter(name = "salon", description = "Le salon où seront envoyées les annonces", type = SlashCommandOptionType.CHANNEL, isRequired = false)
@Command(name = "annonce", description = "Définir le salon où seront envoyées les annonces")
public class SetAnnounceCommand implements SlashCommandCreateListener {
    @Override
    public void onSlashCommandCreate(SlashCommandCreateEvent event) {
        SlashCommandInteraction interaction = event.getSlashCommandInteraction();
        if (interaction.getOptionByName("annonce").isEmpty()) return;

        if (interaction.getServer().isEmpty()) {
            Global.sendErrorMessage(interaction, "Cette commande n'est utilisable que dans un serveur");
            return;
        }

        if (!interaction.getServer().get().hasPermission(interaction.getUser(), PermissionType.MANAGE_MESSAGES)) {
            Global.sendErrorMessage(interaction, "Tu n'as pas la permission de modifier le salon des annonces");
            return;
        }

        long id;
        EmbedBuilder builder = new EmbedBuilder();

        Optional<ServerChannel> channel = interaction.getOptionByName("annonce").get().getOptionChannelValueByName("salon");

        if (channel.isEmpty()) {
            id = 0;
            builder.setColor(Global.ORANGE).setDescription("❌ Plus aucun message de level up ne sera envoyé");
        } else {
            id = channel.get().getId();
            builder.setColor(Global.GOLD).setDescription("✅ Les messages de level up seront envoyés dans <#" + channel.get().getId() + ">");
        }

        Database.updateSettings(interaction.getServer().get(), "announceChannelId", id);
        Global.sendResponseEmbed(interaction, builder);
    }
}
