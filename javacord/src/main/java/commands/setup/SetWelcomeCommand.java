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

@Parameter(name = "message", description = "Le message envoyé à chaque nouveau membre", isRequired = false)
@Parameter(name = "salon", description = "Le salon où seront envoyés les messages de bienvenue", type = SlashCommandOptionType.CHANNEL, isRequired = false)
@Command(name = "bienvenue", description = "Définir où et quoi sera envoyé aux nouveaux membres")
public class SetWelcomeCommand implements SlashCommandCreateListener {
    @Override
    public void onSlashCommandCreate(SlashCommandCreateEvent event) {
        SlashCommandInteraction interaction = event.getSlashCommandInteraction();
        if (interaction.getOptionByName("bienvenue").isEmpty()) return;

        if (interaction.getServer().isEmpty()) {
            Global.sendErrorMessage(interaction, "Cette commande n'est utilisable que dans un serveur");
            return;
        }

        if (!interaction.getServer().get().hasPermission(interaction.getUser(), PermissionType.MANAGE_MESSAGES)) {
            Global.sendErrorMessage(interaction, "Tu n'as pas la permission de modifier le message de bienvenue");
            return;
        }

        long id;
        EmbedBuilder builder = new EmbedBuilder();

        Optional<ServerChannel> channel = interaction.getOptionByName("bienvenue").get().getOptionChannelValueByName("salon");
        Optional<String> message = interaction.getOptionByName("bienvenue").get().getOptionStringValueByName("message");

        if (channel.isEmpty()) {
            id = 0;
            builder.setColor(Global.ORANGE).setDescription("❌ Plus aucun message de bienvenue ne sera envoyé");
        } else {
            id = channel.get().getId();
            builder.setColor(Global.GOLD).setDescription("✅ Les messages de bienvenue seront envoyés dans <#" + channel.get().getId() + ">");
        }

        Database.updateWelcomeMessage(interaction.getServer().get(), id, message.orElse(""));
        Global.sendResponseEmbed(interaction, builder);
    }
}
