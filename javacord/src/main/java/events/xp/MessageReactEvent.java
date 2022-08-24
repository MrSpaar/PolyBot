package events.xp;

import commands.levels.LevelsGroup;
import database.Database;
import database.MemberEntry;
import org.javacord.api.entity.message.Message;
import org.javacord.api.entity.message.embed.Embed;
import org.javacord.api.entity.server.Server;
import org.javacord.api.entity.user.User;
import org.javacord.api.event.message.reaction.ReactionAddEvent;
import org.javacord.api.listener.message.reaction.ReactionAddListener;

import java.util.ArrayList;

public class MessageReactEvent implements ReactionAddListener {
    @Override
    public void onReactionAdd(ReactionAddEvent event) {
        if (event.getServer().isEmpty() || event.getUser().isEmpty()) return;
        if (event.getReaction().isEmpty()) return;
        if (event.getEmoji().asUnicodeEmoji().isEmpty()) return;

        User user = event.getUser().get();
        Server server = event.getServer().get();

        if (user.isBot()) return;
        if (event.getApi().getCachedMessageById(event.getMessageId()).isEmpty()) return;

        String emoji = event.getEmoji().asUnicodeEmoji().get();
        Message message = event.getApi().getCachedMessageById(event.getMessageId()).get();

        if (message.getEmbeds().size() != 1) return;
        Embed embed = message.getEmbeds().get(0);

        if (embed.getAuthor().isEmpty() || embed.getFooter().isEmpty()) return;
        if (embed.getFooter().get().getText().isEmpty()) return;
        if (!embed.getAuthor().get().getName().equals("Classement du serveur")) return;

        ArrayList<MemberEntry> entries = new ArrayList<>();
        Database.getLeaderboard(server).forEach(entries::add);

        int increment = emoji.equals("◀️") ? -1: 1;
        int currentPage = Integer.parseInt(
                embed.getFooter().get().getText().get().replace("Page ", "")
        );

        int totalPages = entries.size()/10 + Math.min(entries.size()%10, 1);

        int nextPage = (currentPage + increment) % totalPages;
        nextPage = nextPage == 0 ? totalPages: nextPage;

        int a = nextPage == 1 ? 1: nextPage*10 - 9;
        int b = nextPage == 1 ? 10: nextPage*10 - 1;

        StringBuilder names  = new StringBuilder();
        StringBuilder levels = new StringBuilder();
        StringBuilder progress = new StringBuilder();

        entries.subList(a, b).forEach(entry ->
            LevelsGroup.processEntry(entry, server, names, levels, progress)
        );

        message.edit(LevelsGroup.buildEmbed(server, names, levels, progress, nextPage));
        event.getReaction().get().removeUser(user);
    }
}
