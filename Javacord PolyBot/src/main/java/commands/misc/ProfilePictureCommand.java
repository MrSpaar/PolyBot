package commands.misc;

import framework.Command;
import framework.Parameter;
import org.javacord.api.entity.message.embed.EmbedBuilder;
import org.javacord.api.entity.user.User;
import org.javacord.api.event.interaction.SlashCommandCreateEvent;
import org.javacord.api.interaction.SlashCommandInteraction;
import org.javacord.api.interaction.SlashCommandOptionType;
import org.javacord.api.listener.interaction.SlashCommandCreateListener;
import ressources.Global;

@Command(name = "pfp", description = "Récupérer l'image de profil d'un membre")
@Parameter(name = "membre", description = "Le membre dont tu veux l'image de profil", type = SlashCommandOptionType.USER, isRequired = false)
public class ProfilePictureCommand implements SlashCommandCreateListener {
    @Override
    public void onSlashCommandCreate(SlashCommandCreateEvent event) {
        SlashCommandInteraction interaction = event.getSlashCommandInteraction();
        if (!interaction.getCommandName().equals("pfp")) return;

        User user = interaction.getOptionUserValueByName("membre").orElse(interaction.getUser());

        Global.sendResponseEmbed(
                interaction,
                new EmbedBuilder()
                        .setImage(user.getAvatar())
                        .setColor(Global.BLUE)
        );
    }
}
