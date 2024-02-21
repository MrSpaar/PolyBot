package commands.moderation;

import framework.Command;
import framework.Parameter;
import org.javacord.api.entity.channel.TextChannel;
import org.javacord.api.entity.message.embed.EmbedBuilder;
import org.javacord.api.entity.permission.PermissionType;
import org.javacord.api.event.interaction.SlashCommandCreateEvent;
import org.javacord.api.interaction.SlashCommandInteraction;
import org.javacord.api.interaction.SlashCommandOptionType;
import org.javacord.api.listener.interaction.SlashCommandCreateListener;
import ressources.Global;

@Command(name = "clear", description = "Supprimer plusieurs messages en une fois")
@Parameter(name = "n", description = "Le nombre de messages à supprimer", type = SlashCommandOptionType.DECIMAL)
public class ClearCommand implements SlashCommandCreateListener {
    @Override
    public void onSlashCommandCreate(SlashCommandCreateEvent event) {
        SlashCommandInteraction interaction = event.getSlashCommandInteraction();
        if (!interaction.getCommandName().equals("clear")) return;

        if (interaction.getServer().isEmpty()) {
            Global.sendErrorMessage(interaction, "Cette commande n'est utilisable que dans un serveur");
            return;
        }

        if (!interaction.getServer().get().hasPermission(interaction.getUser(), PermissionType.MANAGE_MESSAGES)) {
            Global.sendErrorMessage(interaction, "Tu n'as pas la permission de supprimer des messages");
            return;
        }

        if (interaction.getChannel().isEmpty()) {
            Global.sendErrorMessage(interaction, "Salon introuvable");
            return;
        }

        if (interaction.getOptionDecimalValueByName("n").isEmpty()) {
            Global.sendErrorMessage(interaction, "Tu n'as pas spécifier le nombre de messages à supprimer");
            return;
        }

        TextChannel channel = interaction.getChannel().get();
        Double n = interaction.getOptionDecimalValueByName("n").get();

        channel.getMessages(n.intValue()).thenAccept(messages -> {
            if (messages.size() == 0) {
                Global.sendErrorMessage(interaction, "Aucun message à supprimer");
                return;
            }

            if (messages.getNewestMessage().isEmpty()) {
                Global.sendErrorMessage(interaction, "Aucun message à supprimer");
                return;
            }

            if (!messages.getNewestMessage().get().canYouDelete()) {
                Global.sendErrorMessage(interaction, "Je n'ai pas la permission de supprimer des messages");
                return;
            }

            messages.deleteAll();

            Global.sendResponseEmbed(
                    interaction,
                    new EmbedBuilder()
                            .setColor(Global.GREEN)
                            .setDescription("✅ " + messages.size() + " messages supprimés"),
                    true
            );
        });
    }
}
