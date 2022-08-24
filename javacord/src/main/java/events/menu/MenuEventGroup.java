package events.menu;

import framework.Group;
import framework.GroupType;
import org.javacord.api.entity.message.MessageFlag;
import org.javacord.api.entity.message.embed.EmbedBuilder;
import org.javacord.api.entity.permission.Role;
import org.javacord.api.entity.server.Server;
import org.javacord.api.interaction.Interaction;
import org.javacord.api.listener.GloballyAttachableListener;
import ressources.Global;

import java.awt.*;

public class MenuEventGroup extends Group {
    @Override
    public String getName() {
        return "";
    }

    @Override
    public GroupType getType() {
        return GroupType.EVENTS;
    }

    @Override
    public boolean isGlobal() {
        return true;
    }

    @Override
    public GloballyAttachableListener[] getListeners() {
        return new GloballyAttachableListener[]{
                new ButtonPressEvent(),
                new SelectChooseEvent()
        };
    }

    public static void sendResponse(Interaction interaction, String message, Color color) {
        interaction.createImmediateResponder()
                .setFlags(MessageFlag.EPHEMERAL)
                .addEmbed(new EmbedBuilder()
                        .setColor(color)
                        .setDescription(message)
                )
                .respond();
    }

    public static void addRole(Interaction interaction, Server server, Role role) {
        server.addRoleToUser(interaction.getUser(), role);
        sendResponse(interaction, "✅ Rôle " + role.getMentionTag() + " ajouté", Global.GREEN);
    }
}
