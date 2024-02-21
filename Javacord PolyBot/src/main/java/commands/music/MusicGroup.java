package commands.music;

import framework.Group;
import framework.GroupType;
import org.javacord.api.listener.GloballyAttachableListener;

public class MusicGroup extends Group {
    @Override
    public String getName() {
        return "audio";
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
                new PlayCommand(),
                new SkipCommand()
        };
    }
}
