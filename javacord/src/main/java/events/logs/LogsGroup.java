package events.logs;

import framework.Group;
import framework.GroupType;
import org.javacord.api.listener.GloballyAttachableListener;

public class LogsGroup extends Group {
    @Override
    public String getName() {
        return "logs";
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
                new InviteCreateEvent(),
                new MemberBanEvent(),
                new MemberUnbanEvent(),
                new MemberJoinEvent(),
                new MemberLeaveEvent(),
                new RoleRemoveEvent(),
                new RoleAddEvent(),
                new MessageDeleteEvent(),
                new NicknameUpdateEvent(),
        };
    }
}
