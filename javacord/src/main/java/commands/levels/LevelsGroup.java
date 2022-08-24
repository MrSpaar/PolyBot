package commands.levels;

import database.MemberEntry;
import database.XpEntry;
import framework.Group;
import framework.GroupType;
import org.javacord.api.entity.message.embed.EmbedBuilder;
import org.javacord.api.entity.server.Server;
import org.javacord.api.listener.GloballyAttachableListener;
import ressources.Global;

import java.text.NumberFormat;
import java.util.Locale;

public class LevelsGroup extends Group {
    public static final NumberFormat fmt = NumberFormat.getCompactNumberInstance(Locale.US, NumberFormat.Style.SHORT);

    public String getName() {
        return "levels";
    }

    @Override
    public GroupType getType() {
        return GroupType.COMMANDS;
    }

    @Override
    public boolean isGlobal() {
        return false;
    }

    public GloballyAttachableListener[] getListeners() {
        return new GloballyAttachableListener[] {
                new RankCommand(),
                new LeaderboardCommand()
        };
    }

    public static String getProgressBar(XpEntry entry, int length) {
        if (entry.getXp() == 0)
            return "⬛".repeat(length);

        int currentLevel = entry.getLevel();
        int nextLevel = entry.getLevel() + 1;

        double nextLevelCap = 5 * currentLevel*currentLevel + (50*currentLevel) + 100;
        double nextLevelTotalXp = 5.0/6 * nextLevel * (2 * nextLevel*nextLevel + 27*nextLevel + 91);

        double completed = nextLevelCap - (nextLevelTotalXp - entry.getXp());
        double progress = (completed/nextLevelCap)*length;

        int intProgress = Math.max(1, (int) progress);

        return "\uD83D\uDFE9".repeat(intProgress) + "⬛".repeat(length-intProgress) + " " + fmt.format(completed) + " / " + fmt.format(nextLevelCap);
    }

    public static void processEntry(MemberEntry entry, Server server, StringBuilder names, StringBuilder levels, StringBuilder progress) {
        XpEntry xpEntry = entry.getGuilds().get(0);

        server.getMemberById(entry.getId()).ifPresent(user -> {
            names.append(user.getDisplayName(server)).append("\n");
            progress.append(LevelsGroup.getProgressBar(xpEntry, 5)).append("\n");
            levels.append(xpEntry.getLevel()).append(" (")
                    .append(LevelsGroup.fmt.format(xpEntry.getXp())).append(")").append("\n");
        });
    }

    public static EmbedBuilder buildEmbed(Server server, StringBuilder names, StringBuilder levels, StringBuilder progress, int page) {
        EmbedBuilder builder =  new EmbedBuilder()
                .setColor(Global.BLUE)
                .setFooter("Page " + page)
                .addField("Nom", names.toString(), true)
                .addField("Niveau", levels.toString(), true)
                .addField("Progression", progress.toString(), true);

        server.getIcon().ifPresentOrElse(
                icon -> builder.setAuthor("Classement du serveur", "", icon),
                () -> builder.setAuthor("Classement du serveur")
        );

        return builder;
    }
}