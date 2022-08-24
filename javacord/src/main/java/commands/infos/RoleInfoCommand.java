package commands.infos;

import framework.Command;
import framework.Parameter;
import org.javacord.api.entity.message.embed.EmbedBuilder;
import org.javacord.api.entity.permission.PermissionType;
import org.javacord.api.entity.permission.Role;
import org.javacord.api.entity.server.Server;
import org.javacord.api.event.interaction.SlashCommandCreateEvent;
import org.javacord.api.interaction.SlashCommandInteraction;
import org.javacord.api.interaction.SlashCommandOptionType;
import org.javacord.api.listener.interaction.SlashCommandCreateListener;
import ressources.Global;

import java.awt.*;

@Command(name = "role", description = "Afficher des informations sur un rôle")
@Parameter(name = "mention", description = "Le rôle dont tu veux les informations", type = SlashCommandOptionType.ROLE)
public class RoleInfoCommand implements SlashCommandCreateListener {
    @Override
    public void onSlashCommandCreate(SlashCommandCreateEvent event) {
        SlashCommandInteraction interaction = event.getSlashCommandInteraction();
        if (interaction.getOptionByName("role").isEmpty()) return;

        if (interaction.getServer().isEmpty()) {
            Global.sendErrorMessage(interaction, "Cette commande n'est utilisable que dans un serveur");
            return;
        }

        if (interaction.getOptionByName("role").get().getOptionRoleValueByName("mention").isEmpty()) {
            Global.sendErrorMessage(interaction, "Tu n'as pas spécifié de rôle");
            return;
        }

        Server server = interaction.getServer().get();
        Role role = interaction.getOptionByName("role").get().getOptionRoleValueByName("mention").get();
        StringBuilder description = new StringBuilder();

        description.append("⏱️ Créé <").append(role.getCreationTimestamp().getEpochSecond()).append(":R>\n");

        if (role.getColor().isPresent()) {
            Color color = role.getColor().get();
            String hexColor = "#" + Integer.toHexString(color.getRed())
                                  + Integer.toHexString(color.getGreen())
                                  + Integer.toHexString(color.getBlue());

            description.append("\uD83C\uDF08 Couleur : `").append(hexColor).append("`\n");
        }

        description.append("\uD83D\uDD14 ").append(role.isMentionable() ? "Mentionnable": "Non mentionnable");
        description.append(role.isDisplayedSeparately() ? " et affiché séparemment": "");
        description.append("\n\n⛔ Permissions : ");

        if (role.getAllowedPermissions().isEmpty()) {
            description.append("*Aucune permissions*");
        } else {
            StringBuilder permissions = new StringBuilder();

            for (PermissionType permission: role.getAllowedPermissions()) {
                switch (permission) {
                    case ADMINISTRATOR -> permissions.append("\n    - Administrateur");
                    case MANAGE_SERVER -> permissions.append("\n    - Gérer le serveur");
                    case MANAGE_WEBHOOKS -> permissions.append("\n    - Gérer les webhooks");
                    case MANAGE_THREADS -> permissions.append("\n    - Gérer les threads");
                    case MANAGE_NICKNAMES -> permissions.append("\n    - Gérer les pseudos");
                    case MANAGE_EMOJIS -> permissions.append("\n    - Gérer les emojis");
                    case MANAGE_MESSAGES -> permissions.append("\n    - Gérer les messages");
                    case MANAGE_CHANNELS -> permissions.append("\n    - Gérer les salons");
                    case MANAGE_ROLES -> permissions.append("\n    - Gérer les rôles");
                    case VIEW_SERVER_INSIGHTS -> permissions.append("\n    - Voir les analyses du serveur");
                    case VIEW_AUDIT_LOG -> permissions.append("\n    - Voir les events.logs du serveur");
                    case USE_VOICE_ACTIVITY -> permissions.append("\n    - Voir les activités de voix");
                    case VIEW_CHANNEL -> permissions.append("\n    - Voir les salons");
                    case BAN_MEMBERS -> permissions.append("\n    - Bannir des membres");
                    case KICK_MEMBERS -> permissions.append("\n    - Expulser des membres");
                    case MODERATE_MEMBERS -> permissions.append("\n    - Exclure temporairement des membres");
                    case MUTE_MEMBERS -> permissions.append("\n    - Rendre des membres muets");
                    case MOVE_MEMBERS -> permissions.append("\n    - Bouger des membres des vocaux");
                    case DEAFEN_MEMBERS -> permissions.append("\n    - Rendre des membres sourd");
                    case ADD_REACTIONS -> permissions.append("\n    - Ajouter des réactions");
                    case CHANGE_NICKNAME -> permissions.append("\n    - Changer de pseudo");
                    case CREATE_INSTANT_INVITE -> permissions.append("\n    - Créer des invitations");
                    case MENTION_EVERYONE -> permissions.append("\n    - Mentionner tous les rôles");
                    case ATTACH_FILE -> permissions.append("\n    - Envoyer des fichiers");
                    case SEND_TTS_MESSAGES -> permissions.append("\n    - Envoyer des TTS");
                    case EMBED_LINKS -> permissions.append("\n    - Envoyer des intégrations");
                    case START_EMBEDDED_ACTIVITIES -> permissions.append("\n    - Créer des activités");
                    case CREATE_PRIVATE_THREADS -> permissions.append("\n    - Créer des threads privés");
                    case CREATE_PUBLIC_THREADS -> permissions.append("\n    - Créer des threads publiques");
                    case SEND_MESSAGES_IN_THREADS -> permissions.append("\n    - Envoyer des messages dans un thread");
                    case READ_MESSAGE_HISTORY -> permissions.append("\n    - Lire l'historique de messages");
                    case USE_APPLICATION_COMMANDS -> permissions.append("\n     - Utiliser des slash commands");
                    case USE_EXTERNAL_STICKERS -> permissions.append("\n    - Utiliser des stickers externes");
                    case USE_EXTERNAL_EMOJIS -> permissions.append("\n    - Utiliser des emojis externes");
                    case SEND_MESSAGES -> permissions.append("\n    - Envoyer des messages");
                    case PRIORITY_SPEAKER -> permissions.append("\n    - Parler en priorité");
                    case STREAM -> permissions.append("\n    - Faire un stream");
                    case SPEAK -> permissions.append("\n    - Parler en vocal");
                    case REQUEST_TO_SPEAK -> permissions.append("\n     - Demander la parole");
                    case CONNECT -> permissions.append("\n    - Se connecter à un vocal");
                }
            }

            description.append(permissions);
        }

        EmbedBuilder embed = new EmbedBuilder()
                .setColor(role.getColor().get())
                .setDescription(description.toString());

        if (server.getIcon().isPresent())
            embed.setAuthor(role.getName() + " - " + role.getIdAsString(), "", server.getIcon().get());
        else
            embed.setAuthor(role.getName() + " - " + role.getIdAsString());

        Global.sendResponseEmbed(interaction, embed);
    }
}
