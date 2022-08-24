package commands.levels;

import database.Database;
import framework.Command;
import org.javacord.api.entity.server.Server;
import org.javacord.api.event.interaction.SlashCommandCreateEvent;
import org.javacord.api.interaction.SlashCommandInteraction;
import org.javacord.api.listener.interaction.SlashCommandCreateListener;
import ressources.Global;

@Command(name = "classement", description = "Afficher le classement du serveur")
public class LeaderboardCommand implements SlashCommandCreateListener {
    @Override
    public void onSlashCommandCreate(SlashCommandCreateEvent event) {
        SlashCommandInteraction interaction = event.getSlashCommandInteraction();
        if (!interaction.getCommandName().equals("classement")) return;

        if (interaction.getServer().isEmpty()) {
            Global.sendErrorMessage(interaction, "Cette commande n'est utilisable que dans un serveur");
            return;
        }

        Server server = interaction.getServer().get();
        StringBuilder names = new StringBuilder();
        StringBuilder levels = new StringBuilder();
        StringBuilder progress = new StringBuilder();

        Database.getLeaderboard(interaction.getServer().get()).limit(10).forEach(entry ->
            LevelsGroup.processEntry(entry, server, names, levels, progress)
        );

        interaction.createImmediateResponder()
                .addEmbed(LevelsGroup.buildEmbed(server, names, levels, progress, 1))
                .respond()
                .thenAccept(updater -> updater.update().thenAccept(message ->
                    message.addReactions("◀️", "▶️")
                ));
    }
}
