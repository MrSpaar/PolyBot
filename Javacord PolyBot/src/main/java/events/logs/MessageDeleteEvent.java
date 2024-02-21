package events.logs;

import database.Database;
import org.javacord.api.entity.Attachment;
import org.javacord.api.entity.channel.ServerTextChannel;
import org.javacord.api.entity.message.Message;
import org.javacord.api.entity.message.embed.EmbedBuilder;
import org.javacord.api.entity.server.Server;
import org.javacord.api.listener.message.MessageDeleteListener;
import ressources.Global;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;

public class MessageDeleteEvent implements MessageDeleteListener {
    @Override
    public void onMessageDelete(org.javacord.api.event.message.MessageDeleteEvent event) {
        if (event.getServer().isEmpty() || event.getServerTextChannel().isEmpty()) return;
        Server server = event.getServer().get();

        if (event.getMessageAuthor().isEmpty()) return;
        if (event.getMessage().isEmpty()) return;
        if (event.getMessage().get().getAuthor().isBotUser()) return;
        if (event.getServerTextChannel().get().getName().contains("test")) return;

        Message message = event.getMessage().get();
        ServerTextChannel channel = Database.getLogsChannel(server);
        if (channel == null) return;

        Duration delta = Duration.between(Instant.now(), message.getCreationTimestamp());
        boolean hasMentions = !message.getMentionedRoles().isEmpty() || !message.getMentionedUsers().isEmpty();
        boolean hasContent = !message.getContent().isEmpty();
        boolean noAttachments = message.getAttachments().isEmpty();

        EmbedBuilder embed = new EmbedBuilder();
        StringBuilder description = new StringBuilder();

        if (delta.getSeconds() <= 20 && hasMentions) {
            embed.setColor(Global.RED);
            description.append("<:ping:768097026402942976> ");
        } else if (hasContent && noAttachments) {
            embed.setColor(Global.LIGHT_GRAY);
            description.append("\uD83D\uDDD1️ ");
        } else {
            embed.setColor(Global.GOLD);
            description.append("\uD83D\uDDD1️ ");
        }

        description.append("Message de <@").append(message.getAuthor().getId()).append("> supprimé dans <#").append(message.getChannel().getId()).append(">");
        if (hasContent) description.append("\n\n> ").append(message.getContent());

        embed.setDescription(description.toString());
        channel.sendMessage(embed);

        ArrayList<EmbedBuilder> builders = new ArrayList<>();

        message.getAttachments().stream().filter(Attachment::isImage).forEach(attachement ->
            builders.add(
                    new EmbedBuilder()
                            .setColor(Global.GREEN)
                            .setImage(attachement.getUrl().toString())
            )
        );

        if (!builders.isEmpty())
            channel.sendMessage(builders);
    }
}
