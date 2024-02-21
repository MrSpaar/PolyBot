package commands.games;

import framework.Group;
import framework.GroupType;
import org.javacord.api.listener.GloballyAttachableListener;

public class GamesGroup extends Group {
    @Override
    public String getName() {
        return "jeux";
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
                new CoinflipCommand(),
                new HangmanCommand(),
                new RollCommand()
        };
    }
}
