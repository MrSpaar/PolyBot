import commands.levels.LevelsGroup;
import commands.menu.MenuComandGroup;
import commands.music.MusicGroup;
import commands.games.GamesGroup;
import commands.infos.InfoGroup;
import commands.misc.MiscGroup;
import commands.moderation.ModerationGroup;
import commands.search.SearchGroup;
import commands.setup.SetupGroup;
import database.Database;
import events.menu.MenuEventGroup;
import events.xp.XpGroup;
import framework.Register;
import events.logs.LogsGroup;
import org.javacord.api.DiscordApiBuilder;
import org.javacord.api.entity.activity.ActivityType;
import ressources.Global;
import events.tempchannel.TempChannelGroup;

public class Main {
    public static void main(String[] args) {
        new DiscordApiBuilder()
            .setToken(Global.ENV.get("DISCORD_TOKEN"))
            .setAllIntents()
            .login().thenAccept(api -> {
                    new SearchGroup();
                    new ModerationGroup();
                    new MiscGroup();
                    new GamesGroup();
                    new InfoGroup();
                    new MusicGroup();
                    new LevelsGroup();
                    new SetupGroup();
                    new MenuComandGroup();

                    new XpGroup();
                    new LogsGroup();
                    new MenuEventGroup();
                    new TempChannelGroup();

                    Database.initialize();
                    Register.setServers(752921557214429316L, 634339847108165632L, 339045627478540288L);
                    Register.build(api);

                    api.updateActivity(ActivityType.PLAYING, "vous observer");
                    System.out.println("Bot is ready!");
            });
    }
}
