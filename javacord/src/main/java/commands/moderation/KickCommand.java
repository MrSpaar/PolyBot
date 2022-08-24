package commands.moderation;

import framework.Command;
import framework.Parameter;
import org.javacord.api.entity.message.embed.EmbedBuilder;
import org.javacord.api.entity.permission.PermissionType;
import org.javacord.api.entity.server.Server;
import org.javacord.api.entity.user.User;
import org.javacord.api.event.interaction.SlashCommandCreateEvent;
import org.javacord.api.interaction.SlashCommandInteraction;
import org.javacord.api.interaction.SlashCommandOptionType;
import org.javacord.api.listener.interaction.SlashCommandCreateListener;
import ressources.Global;

@Command(name = "kick", description = "Exclure un membre du serveur")
@Parameter(name = "membre", description = "Le membre à exclure", type = SlashCommandOptionType.USER)
@Parameter(name = "raison", description = "La raison de l'exclusion", isRequired = false)
public class KickCommand implements SlashCommandCreateListener {
    @Override
    public void onSlashCommandCreate(SlashCommandCreateEvent event) {
        SlashCommandInteraction interaction = event.getSlashCommandInteraction();
        if (!interaction.getCommandName().equals("kick")) return;

        if (interaction.getServer().isEmpty()) {
            Global.sendErrorMessage(interaction, "Cette commande n'est utilisable que dans un serveur");
            return;
        }

        if (!interaction.getServer().get().hasPermission(interaction.getUser(), PermissionType.KICK_MEMBERS)) {
            Global.sendErrorMessage(interaction, "Tu n'as pas la permission d'exclure des membres");
            return;
        }

        if (interaction.getOptionUserValueByName("membre").isEmpty()) {
            Global.sendErrorMessage(interaction, "Tu n'as pas spécifié le membre à exclure");
            return;
        }

        Server server = interaction.getServer().get();
        User user = interaction.getOptionUserValueByName("membre").get();

        if (!server.canYouKickUser(user)) {
            Global.sendErrorMessage(interaction, "Je n'ai pas la permission de kick " + user.getMentionTag());
            return;
        }

        server.kickUser(user);
        String reason = interaction.getOptionStringValueByName("raison").orElse("Pas de raison");

        Global.sendResponseEmbed(
                interaction,
                new EmbedBuilder()
                        .setColor(Global.GREEN)
                        .setDescription("✅ " + user.getMentionTag() + "a été exclu du serveur\n❔ Raison : " + reason)
        );
    }
}
