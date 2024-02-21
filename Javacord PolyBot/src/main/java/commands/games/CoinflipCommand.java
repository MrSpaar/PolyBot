package commands.games;

import framework.Command;
import framework.Parameter;
import org.javacord.api.entity.message.embed.EmbedBuilder;
import org.javacord.api.event.interaction.SlashCommandCreateEvent;
import org.javacord.api.interaction.SlashCommandInteraction;
import org.javacord.api.listener.interaction.SlashCommandCreateListener;
import ressources.Global;

@Parameter(name = "face", description = "La face sur laquelle tu paries")
@Command(name = "coinflip", description = "Jouer à pile ou face")
public class CoinflipCommand implements SlashCommandCreateListener {
    @Override
    public void onSlashCommandCreate(SlashCommandCreateEvent event) {
        SlashCommandInteraction interaction = event.getSlashCommandInteraction();
        if (!interaction.getCommandName().equals("coinflip")) return;

        if (interaction.getOptionStringValueByName("face").isEmpty()) {
            Global.sendErrorMessage(interaction, "Tu n'as pas spécifié de face");
            return;
        }

        String face = interaction.getOptionStringValueByName("face").get().toLowerCase();
        if (!face.equals("face") && !face.equals("pile")) {
            Global.sendErrorMessage(interaction, "Tu dois entrer `pile` ou `face`");
            return;
        }

        String[] faces = {"Pile", "Face"};
        String pick  = faces[Global.randInt(1)];
        face = face.substring(0, 1).toUpperCase() + face.substring(1);

        if (face.equals(pick)) {
            Global.sendResponseEmbed(
                    interaction,
                    new EmbedBuilder()
                            .setColor(Global.YELLOW)
                            .setDescription("\uD83E\uDE99 " + face + " ! Tu as gagné"),
                    false
            );
            return;
        }

        Global.sendResponseEmbed(
                interaction,
                new EmbedBuilder()
                        .setColor(Global.RED)
                        .setDescription("❌ " + pick + " ! Tu as perdu")
        );
    }
}
