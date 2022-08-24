package commands.misc;

import framework.Group;
import framework.GroupType;
import org.javacord.api.listener.GloballyAttachableListener;

public class MiscGroup extends Group {
    @Override
    public String getName() {
        return "divers";
    }

    @Override
    public GroupType getType() {
        return GroupType.COMMANDS;
    }

    @Override
    public boolean isGlobal() {
        return true;
    }

    @Override
    public GloballyAttachableListener[] getListeners() {
        return new GloballyAttachableListener[] {
                new EmojiCommand(),
                new PollCommand(),
                new ProfilePictureCommand(),
        };
    }
}
