package commands.search;

import framework.Group;
import framework.GroupType;
import org.javacord.api.listener.GloballyAttachableListener;

public class SearchGroup extends Group {
    @Override
    public String getName() {
        return "recherche";
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
        return new GloballyAttachableListener[]{
                new AnimeCommand(),
                new TwitchCommand(),
                new WeatherCommand(),
                new WikipediaCommand()
        };
    }
}
