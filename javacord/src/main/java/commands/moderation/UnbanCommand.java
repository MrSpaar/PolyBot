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

@Command(name = "unban", description = "Débannir un utilisateur du serveur")
@Parameter(name = "ID", description = "L'ID de l'utilisateur à débannir", type = SlashCommandOptionType.LONG)
@Parameter(name = "raison", description = "La raison du débannissement", isRequired = false)
public class UnbanCommand implements SlashCommandCreateListener {
    @Override
    public void onSlashCommandCreate(SlashCommandCreateEvent event) {
        SlashCommandInteraction interaction = event.getSlashCommandInteraction();
        if (!interaction.getCommandName().equals("unban")) return;

        if (interaction.getServer().isEmpty()) {
            Global.sendErrorMessage(interaction, "Cette commande n'est utilisable que dans un serveur");
            return;
        }

        if (!interaction.getServer().get().hasPermission(interaction.getUser(), PermissionType.BAN_MEMBERS)) {
            Global.sendErrorMessage(interaction, "Tu n'as pas la permission d'unban des membres");
            return;
        }

        if (interaction.getOptionLongValueByName("ID").isEmpty()) {
            Global.sendErrorMessage(interaction, "Tu n'as pas spécifié l'ID du membre à débannir");
            return;
        }

        Server server = interaction.getServer().get();
        Long userID = interaction.getOptionLongValueByName("ID").get();

        if (!server.canYouBanUsers()) {
            Global.sendErrorMessage(interaction, "Je n'ai pas la permission de débannir des utilisateurs");
            return;
        }

        User user = interaction.getApi().getUserById(userID).join();
        server.unbanUser(user);

        Global.sendResponseEmbed(
                interaction,
                new EmbedBuilder()
                        .setColor(Global.GREEN)
                        .setDescription("✅ " + user.getMentionTag() + "a été débanni")
        );
    }
}
