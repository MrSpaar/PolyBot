package commands.infos;

import framework.Group;
import framework.GroupType;
import org.javacord.api.listener.GloballyAttachableListener;

public class InfoGroup extends Group {
    public String getName() {
        return "info";
    }

    @Override
    public GroupType getType() {
        return GroupType.SUB_COMMANDS;
    }

    @Override
    public boolean isGlobal() {
        return false;
    }

    public GloballyAttachableListener[] getListeners() {
        return new GloballyAttachableListener[]{
                new RoleInfoCommand(),
                new ServerInfoCommand(),
                new UserInfoCommand()
        };
    }
}
