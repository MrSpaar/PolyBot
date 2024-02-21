package commands.games;

import framework.Command;
import org.javacord.api.entity.message.embed.EmbedBuilder;
import org.javacord.api.event.interaction.SlashCommandCreateEvent;
import org.javacord.api.interaction.SlashCommandInteraction;
import org.javacord.api.interaction.callback.InteractionOriginalResponseUpdater;
import org.javacord.api.listener.interaction.SlashCommandCreateListener;
import ressources.Global;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

@Command(name = "pendu", description = "Jouer au pendu")
public class HangmanCommand implements SlashCommandCreateListener {
    @Override
    public void onSlashCommandCreate(SlashCommandCreateEvent event) {
        SlashCommandInteraction interaction = event.getSlashCommandInteraction();
        if (!interaction.getCommandName().equals("pendu")) return;

        String word = "";

        try {
            File file = new File("wordlist.txt");
            Scanner scanner = new Scanner(file);

            for (int i = 0; i< Global.randInt(16235); i++)
                word = scanner.nextLine();
        } catch (FileNotFoundException e) {
            Global.sendErrorMessage(interaction, "Une erreur est survenue");
            return;
        }

        EmbedBuilder embed = new EmbedBuilder()
                .setTitle("Partie de pendu")
                .addField("Mot : ", "```" + "-".repeat(word.length()) + "```")
                .addField("Erreurs", "```\u200b```")
                .setFooter("Vies : 5")
                .setColor(Global.randColor());

        InteractionOriginalResponseUpdater updater = interaction.createImmediateResponder()
                .addEmbed(embed)
                .respond()
                .join();

        interaction.getApi().addListener(new HangmanListener(interaction, updater, word));
    }
}
