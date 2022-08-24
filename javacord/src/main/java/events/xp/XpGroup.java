package events.xp;

import framework.Group;
import framework.GroupType;
import org.javacord.api.listener.GloballyAttachableListener;

public class XpGroup extends Group {
    @Override
    public String getName() {
        return "xp";
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
        return new GloballyAttachableListener[] {
                new addXpEvent(),
                new MessageReactEvent()
        };
    }
}
