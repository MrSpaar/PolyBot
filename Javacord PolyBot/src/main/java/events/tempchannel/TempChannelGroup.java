package events.tempchannel;

import framework.Group;
import framework.GroupType;
import org.javacord.api.listener.GloballyAttachableListener;

public class TempChannelGroup extends Group {
    @Override
    public String getName() {
        return "tempchannel";
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
                new ChannelJoinEvent(),
                new ChannelLeaveEvent(),
        };
    }
}
