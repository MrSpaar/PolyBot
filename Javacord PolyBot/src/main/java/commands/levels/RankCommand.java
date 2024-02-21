package commands.levels;

import database.Database;
import database.MemberEntry;
import database.XpEntry;
import framework.Command;
import framework.Parameter;
import org.javacord.api.entity.message.embed.EmbedBuilder;
import org.javacord.api.entity.user.User;
import org.javacord.api.event.interaction.SlashCommandCreateEvent;
import org.javacord.api.interaction.SlashCommandInteraction;
import org.javacord.api.listener.interaction.SlashCommandCreateListener;
import ressources.Global;

@Parameter(name = "mention", description = "Le membre dont tu veux voir le niveau", isRequired = false)
@Command(name = "rang", description = "Afficher le niveau d'un membre du serveur")
public class RankCommand implements SlashCommandCreateListener {
    @Override
    public void onSlashCommandCreate(SlashCommandCreateEvent event) {
        SlashCommandInteraction interaction = event.getSlashCommandInteraction();
        if (!interaction.getCommandName().equals("rang")) return;

        if (interaction.getServer().isEmpty()) {
            Global.sendErrorMessage(interaction, "Cette commande n'est utilisable que dans un serveur");
            return;
        }

        User user = interaction.getOptionUserValueByName("mention").orElse(interaction.getUser());

        MemberEntry data = Database.getUserServerEntry(interaction.getServer().get(), user);
        if (data == null || data.getGuilds().size() == 0) {
            Global.sendErrorMessage(interaction, interaction.getUser().getMentionTag() + " n'es pas enregistré dans le classement");
            return;
        }

        XpEntry entry = data.getGuilds().get(0);

        Global.sendResponseEmbed(
                interaction,
                new EmbedBuilder()
                        .setColor(Global.BLUE)
                        .addField("Niveau " + entry.getLevel(), LevelsGroup.getProgressBar(entry, 13))
                        .setAuthor("Progression de " + user.getDisplayName(interaction.getServer().get()), "", user.getAvatar())
        );
    }
}
