package events.menu;

import org.javacord.api.entity.permission.Role;
import org.javacord.api.entity.server.Server;
import org.javacord.api.event.interaction.ButtonClickEvent;
import org.javacord.api.interaction.ButtonInteraction;
import org.javacord.api.listener.interaction.ButtonClickListener;
import ressources.Global;

import java.util.Optional;

public class ButtonPressEvent implements ButtonClickListener {
    @Override
    public void onButtonClick(ButtonClickEvent event) {
        ButtonInteraction interaction = event.getButtonInteraction();
        if (interaction.getServer().isEmpty()) return;

        Server server = interaction.getServer().get();
        Optional<Role> role = server.getRoleById(interaction.getCustomId());

        if (role.isEmpty()) {
            MenuEventGroup.sendResponse(event.getInteraction(), "❌ Impossible de trouver un rôle correspondant", Global.RED);
            return;
        }

        MenuEventGroup.addRole(event.getInteraction(), server, role.get());
    }
}
