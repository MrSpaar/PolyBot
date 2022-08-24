package events.menu;

import org.javacord.api.entity.permission.Role;
import org.javacord.api.entity.server.Server;
import org.javacord.api.event.interaction.SelectMenuChooseEvent;
import org.javacord.api.interaction.SelectMenuInteraction;
import org.javacord.api.listener.interaction.SelectMenuChooseListener;
import ressources.Global;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class SelectChooseEvent implements SelectMenuChooseListener {
    @Override
    public void onSelectMenuChoose(SelectMenuChooseEvent event) {
        SelectMenuInteraction interaction = event.getSelectMenuInteraction();
        if (interaction.getServer().isEmpty()) return;

        Server server = interaction.getServer().get();

        Optional<Role> toAdd = server.getRoleById(interaction.getChosenOptions().get(0).getValue());
        if (toAdd.isEmpty()) {
            MenuEventGroup.sendResponse(event.getInteraction(), "❌ Impossible de trouver un rôle correspondant", Global.RED);
            return;
        }

        ArrayList<Role> menuRoles = new ArrayList<>();
        List<Role> userRoles = server.getRoles(interaction.getUser());

        interaction.getPossibleOptions().forEach(option -> {
            Optional<Role> optional = server.getRoleById(option.getValue());
            optional.ifPresent(menuRoles::add);
        });

        if (!Collections.disjoint(menuRoles, userRoles)) {
            MenuEventGroup.sendResponse(event.getInteraction(), "❌ Tu as déjà un des rôles de cette liste", Global.RED);
            return;
        }

        MenuEventGroup.addRole(event.getInteraction(), server, toAdd.get());
    }
}
