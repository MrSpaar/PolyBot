package commands.menu;

import framework.Group;
import framework.GroupType;
import org.javacord.api.entity.message.component.*;
import org.javacord.api.entity.permission.Role;
import org.javacord.api.interaction.SlashCommandInteraction;
import org.javacord.api.listener.GloballyAttachableListener;
import ressources.Global;

import java.util.ArrayList;
import java.util.List;

public class MenuComandGroup extends Group {
    @Override
    public String getName() {
        return "menu";
    }

    @Override
    public GroupType getType() {
        return GroupType.SUB_COMMANDS;
    }

    @Override
    public boolean isGlobal() {
        return false;
    }

    @Override
    public GloballyAttachableListener[] getListeners() {
        return new GloballyAttachableListener[] {
                new  ButtonMenuCommand(),
                new EmojiMenuCommand(),
                new SelectMenuCommand()
        };
    }

    public static void createListener(SlashCommandInteraction interaction, String type) {
        if (interaction.getServer().isEmpty() || interaction.getChannel().isEmpty()) {
            Global.sendErrorMessage(interaction, "Cette commande n'est utilisable que dans un serveur");
            return;
        }

        interaction.createImmediateResponder()
                .setContent("Entre les rôles du menu")
                .respond()
                .thenAccept(updater ->
                    interaction.getApi().addListener(new RoleMessageCreateEvent(updater, interaction, type))
                );
    }

    public static ActionRow createSelectMenu(String placeholder, List<Role> roles) {
        ArrayList<SelectMenuOption> options = new ArrayList<>();
        for (Role role: roles)
            options.add(SelectMenuOption.create(role.getName(), role.getIdAsString()));

        ActionRowBuilder builder = new ActionRowBuilder();
        builder.addComponents(
                SelectMenu.create("menu", placeholder, 1, 1, options)
        );

        return builder.build();
    }

    public static ActionRow createButtonMenu(List<Role> roles) {
        ActionRowBuilder builder = new ActionRowBuilder();

        for (Role role: roles)
            builder.addComponents(
                    Button.success(role.getIdAsString(), role.getName())
            );

        return builder.build();
    }

    public static ActionRow createEmojiMenu(List<Role> roles, List<String> emojis) {
        ActionRowBuilder builder = new ActionRowBuilder();

        for (int i=0; i<roles.size(); i++) {
            Role role = roles.get(i);
            builder.addComponents(
                    Button.success(role.getIdAsString(), role.getName(), emojis.get(i))
            );
        }

        return builder.build();
    }
}
