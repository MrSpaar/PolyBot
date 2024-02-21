package commands.setup;

import database.Database;
import framework.Command;
import framework.Parameter;
import org.javacord.api.entity.message.embed.EmbedBuilder;
import org.javacord.api.entity.permission.PermissionType;
import org.javacord.api.entity.permission.Role;
import org.javacord.api.event.interaction.SlashCommandCreateEvent;
import org.javacord.api.interaction.SlashCommandInteraction;
import org.javacord.api.interaction.SlashCommandOptionType;
import org.javacord.api.listener.interaction.SlashCommandCreateListener;
import ressources.Global;

import java.util.Optional;

@Parameter(name = "role", description = "Le rôle attribué aux nouveaux membres", type = SlashCommandOptionType.ROLE, isRequired = false)
@Command(name = "nouveau", description = "Définir le rôle ajouté aux nouveaux membres")
public class SetNewcomerCommand implements SlashCommandCreateListener {
    @Override
    public void onSlashCommandCreate(SlashCommandCreateEvent event) {
        SlashCommandInteraction interaction = event.getSlashCommandInteraction();
        if (interaction.getOptionByName("nouveau").isEmpty()) return;

        if (interaction.getServer().isEmpty()) {
            Global.sendErrorMessage(interaction, "Cette commande n'est utilisable que dans un serveur");
            return;
        }

        if (!interaction.getServer().get().hasPermission(interaction.getUser(), PermissionType.MANAGE_ROLES)) {
            Global.sendErrorMessage(interaction, "Tu n'as pas la permission de modifier ce rôle");
            return;
        }

        long id;
        EmbedBuilder builder = new EmbedBuilder();

        Optional<Role> role = interaction.getOptionByName("nouveau").get().getOptionRoleValueByName("role");

        if (role.isEmpty()) {
            id = 0;
            builder.setColor(Global.ORANGE).setDescription("❌ Plus aucun rôle ne sera ajouté automatiquement aux nouveaux");
        } else {
            id = role.get().getId();
            builder.setColor(Global.GOLD).setDescription("✅ " + role.get().getMentionTag() + "sera automatiquement ajouté aux nouveaux");
        }

        Database.updateSettings(interaction.getServer().get(), "newcomerRoleId", id);
        Global.sendResponseEmbed(interaction, builder);
    }
}
