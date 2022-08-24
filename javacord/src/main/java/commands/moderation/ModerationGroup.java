package commands.moderation;

import framework.Group;
import framework.GroupType;
import org.javacord.api.listener.GloballyAttachableListener;

public class ModerationGroup extends Group {
    @Override
    public String getName() {
        return "moderation";
    }

    @Override
    public GroupType getType() {
        return GroupType.COMMANDS;
    }

    @Override
    public boolean isGlobal() {
        return false;
    }

    @Override
    public GloballyAttachableListener[] getListeners() {
        return new GloballyAttachableListener[]{
                new BanCommand(),
                new ClearCommand(),
                new KickCommand(),
                new UnbanCommand()
        };
    }
}
