package commands.menu;

import org.javacord.api.entity.message.Message;
import org.javacord.api.entity.message.component.ActionRow;
import org.javacord.api.event.message.MessageCreateEvent;
import org.javacord.api.interaction.SlashCommandInteraction;
import org.javacord.api.interaction.callback.InteractionOriginalResponseUpdater;
import org.javacord.api.listener.message.MessageCreateListener;

import java.util.ArrayList;

public class RoleMessageCreateEvent implements MessageCreateListener {
    final String type;
    final SlashCommandInteraction interaction;
    final InteractionOriginalResponseUpdater updater;

    public RoleMessageCreateEvent(InteractionOriginalResponseUpdater updater, SlashCommandInteraction interaction, String type) {
        this.type = type;
        this.updater = updater;
        this.interaction = interaction;
    }

    @Override
    public void onMessageCreate(MessageCreateEvent event) {
        if (event.getServer().isEmpty()) return;
        Message message = event.getMessage();

        if (message.getAuthor().getId() != interaction.getUser().getId()) return;
        if (message.getMentionedRoles().isEmpty()) return;

        ActionRow actionRow = null;
        String optionName = "";

        switch (type) {
            case "select" -> {
                optionName = "liste";
                actionRow = MenuComandGroup.createSelectMenu("Choisis un rôle", message.getMentionedRoles());
            }
            case "button" -> {
                optionName = "bouton";
                actionRow = MenuComandGroup.createButtonMenu(message.getMentionedRoles());
            }
            case "emoji" -> {
                ArrayList<String> emojis = new ArrayList<>();
                String[] elems = message.getContent().split(" ");

                for (int i = 0; i < elems.length; i++) {
                    if (i % 2 == 0) emojis.add(elems[i]);
                }

                optionName = "emoji";
                actionRow = MenuComandGroup.createEmojiMenu(message.getMentionedRoles(), emojis);
            }
        }

        message.delete();
        updater.delete();
        event.getApi().removeListener(this);

        interaction.getChannel().get().sendMessage(
                interaction.getOptionByName(optionName).get()
                        .getOptionStringValueByName("titre")
                        .orElse("Menu de rôles"),
                actionRow
        );
    }
}
