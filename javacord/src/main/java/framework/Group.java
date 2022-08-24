package framework;

import org.javacord.api.listener.GloballyAttachableListener;

public abstract class Group {
    public Group() {
        Register.add(this);
    }

    public abstract String getName();

    public abstract GroupType getType();

    public abstract boolean isGlobal();

    public abstract GloballyAttachableListener[] getListeners();
}
