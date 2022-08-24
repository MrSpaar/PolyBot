package commands.games;

import framework.Command;
import framework.Parameter;
import org.javacord.api.entity.message.embed.EmbedBuilder;
import org.javacord.api.event.interaction.SlashCommandCreateEvent;
import org.javacord.api.interaction.SlashCommandInteraction;
import org.javacord.api.listener.interaction.SlashCommandCreateListener;
import ressources.Global;

@Parameter(name = "texte", description = "Texte sous la forme `2d20+...` (lancer deux dés de 20 faces + ...)")
@Command(name = "roll", description = "Faire un lancé de dés")
public class RollCommand implements SlashCommandCreateListener {
    @Override
    public void onSlashCommandCreate(SlashCommandCreateEvent event) {
        SlashCommandInteraction interaction = event.getSlashCommandInteraction();
        if (!interaction.getCommandName().equals("roll")) return;

        if (interaction.getOptionStringValueByName("texte").isEmpty()) {
            Global.sendErrorMessage(interaction, "Tu n'as pas spécifié de texte");
            return;
        }

        String[] items = interaction.getOptionStringValueByName("texte").get().split("\\+");

        if (items.length == 1 & toNumeric(items[0]) == 0) {
            Global.sendErrorMessage(interaction, "Le format du texte est invalide (ex: `2d20+2+3d5`)");
            return;
        }

        int total = 0;
        StringBuilder description = new StringBuilder("\uD83C\uDFB2 Résultat du lancé : ");

        for (int i=0; i<items.length; i++) {
            int val = toNumeric(items[i]);

            if (val == 0) {
                String[] split = items[i].split("d");
                if (!(split.length == 2)) continue;

                int n = toNumeric(split[0]);
                int faces = toNumeric(split[1]);

                if (n == 0 || faces == 0) continue;

                for (int j=0; j<n; j++) {
                    int value = Global.randInt(faces);
                    total += value;
                    description.append(value);

                    if (j != n-1) description.append(" + ");
                }
            } else {
                total += val;
                description.append(val);
            }

            if (i != items.length-1) description.append(" + ");
        }

        description.append(" = ").append(total);

        Global.sendResponseEmbed(
                interaction,
                new EmbedBuilder()
                        .setColor(Global.GOLD)
                        .setDescription(description.toString())
        );
    }

    private int toNumeric(String expr) {
        try {
            return Integer.parseInt(expr);
        } catch (NumberFormatException e) {
            return 0;
        }
    }
}
