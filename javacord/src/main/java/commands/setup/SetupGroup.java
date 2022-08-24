package commands.setup;

import framework.Group;
import framework.GroupType;
import org.javacord.api.listener.GloballyAttachableListener;

public class SetupGroup extends Group {
    @Override
    public String getName() {
        return "config";
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
        return new GloballyAttachableListener[]{
                new SetAnnounceCommand(),
                new SetLogsCommand(),
                new SetNewcomerCommand(),
                new SetWelcomeCommand()
        };
    }
}
