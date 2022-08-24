package commands.menu;

import framework.Command;
import framework.Parameter;
import org.javacord.api.event.interaction.SlashCommandCreateEvent;
import org.javacord.api.interaction.SlashCommandInteraction;
import org.javacord.api.listener.interaction.SlashCommandCreateListener;

@Parameter(name = "titre", description = "Le titre du menu de rôles", isRequired = false)
@Command(name = "bouton", description = "Créer un menu de rôles sous forme de boutons")
public class ButtonMenuCommand implements SlashCommandCreateListener {
    @Override
    public void onSlashCommandCreate(SlashCommandCreateEvent event) {
        SlashCommandInteraction interaction = event.getSlashCommandInteraction();
        if (interaction.getOptionByName("bouton").isEmpty()) return;

        MenuComandGroup.createListener(interaction, "button");
    }
}
