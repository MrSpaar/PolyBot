package commands.misc;

import framework.Command;
import framework.Parameter;
import org.javacord.api.entity.emoji.KnownCustomEmoji;
import org.javacord.api.entity.message.embed.EmbedBuilder;
import org.javacord.api.event.interaction.SlashCommandCreateEvent;
import org.javacord.api.interaction.SlashCommandInteraction;
import org.javacord.api.listener.interaction.SlashCommandCreateListener;
import ressources.Global;

import java.util.Optional;

@Command(name = "emoji", description = "Récupérer l'image d'origine d'un emoji")
@Parameter(name = "emoji", description = "L'émoji dont tu veux récupérer l'image")
public class EmojiCommand implements SlashCommandCreateListener {
    @Override
    public void onSlashCommandCreate(SlashCommandCreateEvent event) {
        SlashCommandInteraction interaction = event.getSlashCommandInteraction();
        if (!interaction.getCommandName().equals("emoji")) return;

        Optional<String> arg = interaction.getOptionStringValueByName("emoji");
        if (arg.isEmpty()) {
            Global.sendErrorMessage(interaction, "Aucun emoji fourni");
            return;
        }

        Optional<KnownCustomEmoji> emoji = interaction.getApi().getCustomEmojiById(arg.get().replaceAll("\\D+", ""));
        if (emoji.isEmpty()) {
            Global.sendErrorMessage(interaction, "Image d'origine introuvable");
            return;
        }

        Global.sendResponseEmbed(
                interaction,
                new EmbedBuilder()
                        .setImage(emoji.get().getImage())
                        .setColor(Global.BLUE)
        );
    }
}
