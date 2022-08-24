package events.xp;

import database.Database;
import database.MemberEntry;
import database.XpEntry;
import org.javacord.api.entity.channel.ServerTextChannel;
import org.javacord.api.entity.message.embed.EmbedBuilder;
import org.javacord.api.entity.server.Server;
import org.javacord.api.entity.user.User;
import org.javacord.api.event.message.MessageCreateEvent;
import org.javacord.api.listener.message.MessageCreateListener;
import ressources.Global;

public class addXpEvent implements MessageCreateListener {
    @Override
    public void onMessageCreate(MessageCreateEvent event) {
        if (event.getServer().isEmpty()) return;
        if (event.getMessageAuthor().isBotUser() || event.getMessageAuthor().asUser().isEmpty()) return;

        Server server = event.getServer().get();
        User user = event.getMessageAuthor().asUser().get();

        if (XpCooldown.isOnCooldown(server, user)) return;

        MemberEntry data = Database.getUserServerEntry(server, user);
        if (data.getGuilds().size() == 0) return;

        XpEntry entry = data.getGuilds().get(0);
        int nextLevel = entry.getLevel() + 1;
        double nextLevelCap = 5.0/6 * nextLevel * (2 * nextLevel*nextLevel + 27*nextLevel + 91);

        int amount = Global.randInt(15, 25);
        boolean shouldLevelUp = (entry.getXp()+amount) > nextLevelCap;

        Database.addXpToUser(server, user, amount, shouldLevelUp);
        if (!shouldLevelUp) return;

        ServerTextChannel channel = Database.getAnnounceChannel(server);
        if (channel == null) return;

        channel.sendMessage(new EmbedBuilder()
                .setColor(Global.GOLD)
                .setDescription("\uD83C\uDD99 " + user.getMentionTag() + "vient de monter niveau **" + nextLevel + "** !")
        );
    }
}
