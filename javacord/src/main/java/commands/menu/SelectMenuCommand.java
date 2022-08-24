package commands.menu;

import framework.Command;
import framework.Parameter;
import org.javacord.api.event.interaction.SlashCommandCreateEvent;
import org.javacord.api.interaction.SlashCommandInteraction;
import org.javacord.api.listener.interaction.SlashCommandCreateListener;

@Parameter(name = "titre", description = "Le titre du menu de rôles", isRequired = false)
@Command(name = "liste", description = "Créer un menu de rôles sous forme de liste déroulante")
public class SelectMenuCommand implements SlashCommandCreateListener {
    @Override
    public void onSlashCommandCreate(SlashCommandCreateEvent event) {
        SlashCommandInteraction interaction = event.getSlashCommandInteraction();
        if (interaction.getOptionByName("liste").isEmpty()) return;

        MenuComandGroup.createListener(interaction, "select");
    }
}
